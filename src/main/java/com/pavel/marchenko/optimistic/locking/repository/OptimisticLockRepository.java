package com.pavel.marchenko.optimistic.locking.repository;

import com.pavel.marchenko.optimistic.locking.domain.OptimisticLockingHandler;
import com.pavel.marchenko.optimistic.locking.domain.entity.OptimisticLockEntity;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Created by pyshankov on 10.08.16.
 */
@NoRepositoryBean
public interface OptimisticLockRepository<T extends OptimisticLockEntity,ID extends Serializable> {
    @OptimisticLockingHandler
    <S extends T> S persist(S var1);
}

