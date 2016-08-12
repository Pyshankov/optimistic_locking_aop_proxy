package com.pavel.marchenko.optimistic.locking.repository;

import com.pavel.marchenko.optimistic.locking.entity.OptimisticLockEntity;
import com.pavel.marchenko.optimistic.locking.exception.OptimisticLockException;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by pyshankov on 10.08.16.
 */
@NoRepositoryBean
public abstract class AbstractOptimisticLockRepository<T extends OptimisticLockEntity<ID>,ID extends Serializable> implements OptimisticLockRepository<T,ID> {

    @Override
    public final <S extends T> S persist(S s) {
        //make those calls at the one transaction
        checkVersion(s);
        prePersist(s);
        S entity = saveEntity(s);
        return entity;
    }

    protected abstract <S extends T> S saveEntity(S s);

    protected abstract <S extends T> S getEntity(ID id);

    private <S extends T> void checkVersion(S s){
        if(s.getEntityId()==null) return;
        S savedEntity = getEntity(s.getEntityId());
        if(!savedEntity.getVersion().equals(s.getVersion())) throw new OptimisticLockException("entity have been previously updated!");
    }

    private <S extends T> void prePersist(S s) {
        if(s.getVersion()==null || s.getEntityId()==null ) return;
        Field[] fields = s.getClass().getDeclaredFields();
        for (Field f : fields ){
            f.setAccessible(true);
            try {
                if(f.get(s) instanceof Iterable ){
                    Iterable iterable = (Iterable) f.get(s);
                    iterable.forEach(p -> {
                        if(p instanceof OptimisticLockEntity) {
                            OptimisticLockEntity ap = ((OptimisticLockEntity) p);
//                            ap.setVersion(s.getVersion());
                        }
                    });
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
