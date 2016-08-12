package com.pavel.marchenko.optimistic.locking;


import com.pavel.marchenko.optimistic.locking.domain.ForProxy;
import com.pavel.marchenko.optimistic.locking.domain.ForProxyImpl;
import com.pavel.marchenko.optimistic.locking.entity.Car;
import com.pavel.marchenko.optimistic.locking.entity.Item;
import com.pavel.marchenko.optimistic.locking.repository.AbstractOptimisticLockRepository;
import com.pavel.marchenko.optimistic.locking.repository.CarRepository;
import com.pavel.marchenko.optimistic.locking.repository.ItemRepositoryImpl;
import com.pavel.marchenko.optimistic.locking.repository.OptimisticLockRepository;
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
import java.util.List;

@SpringBootApplication
public class DemoApplication {


	@Autowired
	OptimisticLockRepository<Car,Long> carOptimisticLockRepository;

	@Autowired
	OptimisticLockRepository<Item,Long> itemOptimisticLockRepository;

	@Autowired
	CarRepository carRepository;

	@Autowired
	ApplicationContext context;


	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);



//		ForProxyImpl proxy  = (ForProxyImpl) Enhancer.create(ForProxyImpl.class ,new MyInvocationHandler(ForProxyImpl.class));
//
//			proxy.one();
//			proxy.two();



	}

		@Bean
	public CommandLineRunner demo(OptimisticLockRepository<Car,Long> carOptimisticLockRepository) {
		return (args) -> {

//			InvocationHandler carHandler = (Object proxy, Method method, Object[] args1) -> {
//				if(method.isAnnotationPresent(OptimisticLockingHandler.class)){
//					System.out.println("begin");
//					method.invoke(carOptimisticLockRepository, args1[0]);
//					System.out.println("end");
//				}else {
//					method.invoke(carOptimisticLockRepository, args1[0]);
//				}
//				return proxy;
//			};
//
//			OptimisticLockRepository<Car,Long> carProxy = (OptimisticLockRepository<Car,Long>) Proxy.newProxyInstance(
//					carOptimisticLockRepository.getClass().getClassLoader(),
//					new Class[] { OptimisticLockRepository.class },
//					carHandler);


			OptimisticLockRepository<Item,Long> itemLongOptimisticLockRepository =
					(OptimisticLockRepository<Item,Long>) Enhancer.create(
							itemOptimisticLockRepository.getClass() ,
							new MyInvocationHandler(context.getBean(itemOptimisticLockRepository.getClass()))
					);



			Car car2 = new Car("mercedes","bla bla mercedes");
			car2.setItems(new ArrayList<>());

			Item item1 = new Item("ITEM 1");
			Item item2 = new Item("ITEM 2");

			item1 = itemLongOptimisticLockRepository.persist(item1);
			item2 = itemLongOptimisticLockRepository.persist(item2);

			car2 = carOptimisticLockRepository.persist(car2);
			System.out.println(car2);
			car2.getItems().add(item1);
			car2.getItems().add(item2);

			car2.setDescription("bla");
			car2 = carOptimisticLockRepository.persist(car2);
			System.out.println(car2);
			car2.setDescription("bla2");
			car2 = carOptimisticLockRepository.persist(car2);
			System.out.println(car2);




		};
	}



	static class MyInvocationHandler implements MethodInterceptor {

		private Class<?> clazz;

		private OptimisticLockRepository<?,?> repository;

		public MyInvocationHandler(OptimisticLockRepository<?,?> repository) {
			this.repository=repository;
		}

		public MyInvocationHandler(Class<?> clazz) {
			this.clazz=clazz;
		}

		/**
		 * @param obj "this", the enhanced object
		 * @param method intercepted Method
		 * @param args argument array; primitive types are wrapped
		 * @param proxy used to invoke super (non-intercepted method); may be called
		 * as many times as needed
		 */
		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			System.out.println("before");
		     Object object = proxy.invoke(repository, args);
			System.out.println("after");
			return object;
		}

	}



}
