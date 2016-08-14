package com.pavel.marchenko.optimistic.locking.repository;

import com.pavel.marchenko.optimistic.locking.domain.entity.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * Created by pyshankov on 10.08.16.
 */
@Component
public class ItemRepositoryImpl extends AbstractOptimisticLockRepository<Item,Long> {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    protected <S extends Item> S saveEntity(S s) {
        return (S) itemRepository.save(s);
    }

    @Override
    protected <S extends Item> S getEntity(Long aLong) {
        return (S) itemRepository.findOne(aLong);
    }
}
