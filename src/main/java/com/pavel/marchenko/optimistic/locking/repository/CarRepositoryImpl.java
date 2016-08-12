package com.pavel.marchenko.optimistic.locking.repository;

import com.pavel.marchenko.optimistic.locking.entity.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by pyshankov on 10.08.16.
 */
@Component
public class CarRepositoryImpl extends  AbstractOptimisticLockRepository<Car,Long> {

    @Autowired
    CarRepository carRepository;

    @Override
    protected <S extends Car> S saveEntity(S s) {
        return (S) carRepository.save(s);
    }

    @Override
    protected <S extends Car> S getEntity(Long id) {
        return (S) carRepository.findOne(id);
    }
}
