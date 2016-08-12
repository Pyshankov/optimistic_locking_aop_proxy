package com.pavel.marchenko.optimistic.locking.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by pyshankov on 05.07.16.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractOptimisticLockEntity<ID> implements OptimisticLockEntity<ID> {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id", unique = true, nullable = false)
    protected ID id;

    @Version
    protected Integer version;

    @Transient
    protected Integer lastVisibleVersion;

    @PostLoad
    private void postLoad(){
        lastVisibleVersion=this.version;
    }

    public Integer getVersion() {
        return version;
    }

    public Integer getLastVisibleVersion() {
        return lastVisibleVersion;
    }

    @Override
    public ID getEntityId() {
        return id;
    }

    @Override
    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setLastVisibleVersion(Integer lastVisibleVersion) {
        this.lastVisibleVersion = lastVisibleVersion;
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }


}
