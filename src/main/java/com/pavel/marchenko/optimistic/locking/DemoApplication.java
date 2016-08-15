package com.pavel.marchenko.optimistic.locking;



import com.pavel.marchenko.optimistic.locking.domain.entity.Car;
import com.pavel.marchenko.optimistic.locking.domain.entity.Item;
import com.pavel.marchenko.optimistic.locking.repository.CarRepository;
import com.pavel.marchenko.optimistic.locking.repository.ItemRepository;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;

import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
public class DemoApplication {


	@Autowired
	ItemRepository itemRepository;

	@Autowired
	CarRepository carRepository;

	@Autowired
	ApplicationContext context;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

		@Bean
	public CommandLineRunner demo() {
		return (args) -> {

			System.out.println(context.getBean(ItemRepository.class).getClass());

			SecureRandom random = new SecureRandom();


			Car car2 = new Car("mercedes","bla bla mercedes");
			car2.setItems(new ArrayList<>());

			Item item1 = new Item("ITEM 1");
			Item item2 = new Item("ITEM 2");

			item1 = itemRepository.save(item1);
			item2 = itemRepository.save(item2);

			car2 = carRepository.save(car2);
			car2.getItems().add(item1);
			car2.getItems().add(item2);

			car2.setDescription(new BigInteger(130, random).toString(32));
			carRepository.save(car2);
			car2.setDescription(new BigInteger(130, random).toString(32));
			car2 = carRepository.save(car2);
			car2.setDescription(new BigInteger(130, random).toString(32));
			car2 = carRepository.save(car2);

			ExecutorService executor = Executors.newFixedThreadPool(15);
			for (int i = 0 ; i<15 ; i++ ){
				executor.submit(()->{
						System.out.println("_");
						Car car3 = carRepository.findOne(3L);
						car3.setDescription(new BigInteger(130, random).toString(32));
						carRepository.save(car3);
				});
			}
		};
	}


}
