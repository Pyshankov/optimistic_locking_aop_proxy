package com.pavel.marchenko.optimistic.locking.domain.entity;

import java.io.Serializable;

/**
 * Created by pyshankov on 10.08.16.
 */
public interface OptimisticLockEntity<ID> extends Serializable {

    ID getEntityId();

    Integer getVersion();

    void setVersion(Integer version);

}
