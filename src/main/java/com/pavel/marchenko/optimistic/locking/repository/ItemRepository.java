package com.pavel.marchenko.optimistic.locking.repository;


import com.pavel.marchenko.optimistic.locking.domain.OptimisticLockingHandler;
import com.pavel.marchenko.optimistic.locking.entity.Item;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by pyshankov on 05.07.16.
 */
public interface ItemRepository extends CrudRepository<Item,Long> {
    @OptimisticLockingHandler
    Item save(Item item);
}

