package com.pavel.marchenko.optimistic.locking.repository;

import com.pavel.marchenko.optimistic.locking.domain.OptimisticLockingHandler;
import com.pavel.marchenko.optimistic.locking.domain.entity.OptimisticLockEntity;
import com.pavel.marchenko.optimistic.locking.exception.OptimisticLockException;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.Version;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by pyshankov on 14.08.2016.
 */
public class OptimisticLockRepositoryInvocationHandler implements MethodInterceptor {

    private final CrudRepository<? extends OptimisticLockEntity<Long>,Long> repository;

    private final PlatformTransactionManager txManager;

    public OptimisticLockRepositoryInvocationHandler(
            CrudRepository<? extends OptimisticLockEntity<Long>, Long> repository,
            PlatformTransactionManager transactionTemplate) {
        this.repository = repository;
        this.txManager=transactionTemplate;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy proxy) throws Throwable {

        boolean isAbleToSave = false;
        if(method.isAnnotationPresent(OptimisticLockingHandler.class)) {
            Object object = null;
            if (objects[0] instanceof OptimisticLockEntity<?>) {
                persist:
                while(!isAbleToSave) {
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setName("transaction");
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    TransactionStatus status = txManager.getTransaction(def);
                    //begin transaction
                    try {
                        checkVersion(objects[0]);
                        isAbleToSave=true;
                        object = proxy.invoke(repository, objects);
                        System.out.println("successfully saved :"+objects[0]);
                    } catch (OptimisticLockException e) {
                        //rollback
                        txManager.rollback(status);
                        System.out.println("some optimistic lock trouble!");
                        Object old = objects[0];
                        objects[0] = repository.findOne(((OptimisticLockEntity<Long>)objects[0]).getEntityId());
                        prePersist(objects[0],old);
                        continue persist;
                    }
                    //commit
                    txManager.commit(status);


                }
                return object;
            } else
                throw new IllegalStateException("Entity must implement OptimisticLockEntity interface");
        }
        else
            return proxy.invoke(repository, objects);
    }

    private void checkVersion(Object o){
        OptimisticLockEntity<Long> entity = (OptimisticLockEntity<Long>) o;
        if(entity.getEntityId()==null)
            return ;
        OptimisticLockEntity<Long> lockEntity = repository.findOne(entity.getEntityId());
        if(!entity.getVersion().equals(lockEntity.getVersion()))
            throw new OptimisticLockException("entity have been previously updated!");
    }

    private void prePersist(Object s,Object from) {
        OptimisticLockEntity<Long> entity = (OptimisticLockEntity<Long>) s;
        if(entity.getVersion()==null || entity.getEntityId()==null ) return;
        Field[] fields = s.getClass().getDeclaredFields();
        for (Field f : fields ){
            f.setAccessible(true);
            if(
                    !Map.class.isAssignableFrom(f.getType()) &&
                    !Iterable.class.isAssignableFrom(f.getType()) &&
                    !f.isAnnotationPresent(Version.class)
              )
                try {
                    f.set(s,f.get(from));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
        }
    }

}
