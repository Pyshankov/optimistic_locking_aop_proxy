package com.pavel.marchenko.optimistic.locking;



import com.pavel.marchenko.optimistic.locking.domain.OptimisticLockingHandler;
import com.pavel.marchenko.optimistic.locking.domain.entity.Car;
import com.pavel.marchenko.optimistic.locking.domain.entity.Item;
import com.pavel.marchenko.optimistic.locking.domain.entity.OptimisticLockEntity;
import com.pavel.marchenko.optimistic.locking.exception.OptimisticLockException;
import com.pavel.marchenko.optimistic.locking.repository.CarRepository;
import com.pavel.marchenko.optimistic.locking.repository.ItemRepository;
import com.pavel.marchenko.optimistic.locking.repository.OptimisticLockRepository;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.*;

import com.pavel.marchenko.optimistic.locking.repository.OptimisticLockRepositoryInvocationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.Version;

@SpringBootApplication
public class DemoApplication {


	@Autowired
	OptimisticLockRepository<Car,Long> carOptimisticLockRepository;

	@Autowired
	OptimisticLockRepository<Item,Long> itemOptimisticLockRepository;

	@Autowired
	ApplicationContext context;

	@Autowired
	PlatformTransactionManager transactionTemplate;


	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);



	}

		@Bean
	public CommandLineRunner demo(OptimisticLockRepository<Car,Long> carOptimisticLockRepository) {
		return (args) -> {

			System.out.println(context.getBean(ItemRepository.class).getClass());

			SecureRandom random = new SecureRandom();

			ItemRepository itemRepositoryProxy = (ItemRepository) Enhancer.create(
					ItemRepository.class,
					new OptimisticLockRepositoryInvocationHandler(context.getBean(ItemRepository.class),transactionTemplate)
			);

			CarRepository carRepositoryProxy = (CarRepository) Enhancer.create(
					CarRepository.class,
					new OptimisticLockRepositoryInvocationHandler(context.getBean(CarRepository.class),transactionTemplate)
			);



			Car car2 = new Car("mercedes","bla bla mercedes");
			car2.setItems(new ArrayList<>());

			Item item1 = new Item("ITEM 1");
			Item item2 = new Item("ITEM 2");

			item1 = itemRepositoryProxy.save(item1);
			item2 = itemRepositoryProxy.save(item2);

			car2 = carRepositoryProxy.save(car2);
			car2.getItems().add(item1);
			car2.getItems().add(item2);

			car2.setDescription(new BigInteger(130, random).toString(32));
			carRepositoryProxy.save(car2);
			car2.setDescription(new BigInteger(130, random).toString(32));
			car2 = carRepositoryProxy.save(car2);
			car2.setDescription(new BigInteger(130, random).toString(32));
			car2 = carRepositoryProxy.save(car2);

			ExecutorService executor = Executors.newFixedThreadPool(15);
			for (int i = 0 ; i<15 ; i++ ){
				executor.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						System.out.println("_");
						Car car3 = carRepositoryProxy.findOne(3L);
						car3.setDescription(new BigInteger(130, random).toString(32));
						carRepositoryProxy.save(car3);
						return null;
					}
				});
			}
		};
	}


}
