package com.pavel.marchenko.optimistic.locking.exception;

/**
 * Created by pyshankov on 10.08.16.
 */
public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }
}
