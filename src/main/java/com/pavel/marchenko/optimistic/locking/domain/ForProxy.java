package com.pavel.marchenko.optimistic.locking.domain;

/**
 * Created by pyshankov on 09.08.16.
 */
public interface ForProxy {

    @OptimisticLockingHandler
    void one();

    void two();

}
