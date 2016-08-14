package com.pavel.marchenko.optimistic.locking;

import com.pavel.marchenko.optimistic.locking.domain.entity.Car;
import com.pavel.marchenko.optimistic.locking.domain.entity.Item;
import com.pavel.marchenko.optimistic.locking.repository.OptimisticLockRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {



	@Autowired
	OptimisticLockRepository<Car,Long> carOptimisticLockRepository;

	@Autowired
	OptimisticLockRepository<Item,Long> itemOptimisticLockRepository;


	@Test
	public void testOptimisticLockingException() {


		Car car2 = new Car("mercedes","bla bla mercedes");

		Item item1 = new Item("ITEM 1");
		Item item2 = new Item("ITEM 2");

		item1 = itemOptimisticLockRepository.persist(item1);
		item2 = itemOptimisticLockRepository.persist(item2);

		car2 = carOptimisticLockRepository.persist(car2);

		car2.getItems().add(item1);
		car2.getItems().add(item2);


	}

}
