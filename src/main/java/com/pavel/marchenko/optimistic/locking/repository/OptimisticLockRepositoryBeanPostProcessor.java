package com.pavel.marchenko.optimistic.locking.repository;


import com.pavel.marchenko.optimistic.locking.domain.entity.OptimisticLockEntity;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.annotation.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Created by pyshankov on 14.08.2016.
 */
@Order
@Component
public class OptimisticLockRepositoryBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    PlatformTransactionManager txManager;

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {

        if(o instanceof ItemRepository || o instanceof CarRepository){
            System.out.println(o.getClass());
//           o =  Enhancer.create(
//                    CrudRepository.class,
//                    new OptimisticLockRepositoryInvocationHandler((CrudRepository<? extends OptimisticLockEntity<Long>, Long>) o,txManager)
//            );
            System.out.println(o);
        }

        return o;
    }
}
