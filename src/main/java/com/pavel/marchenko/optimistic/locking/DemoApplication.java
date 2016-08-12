package com.pavel.marchenko.optimistic.locking;



import com.pavel.marchenko.optimistic.locking.domain.OptimisticLockingHandler;
import com.pavel.marchenko.optimistic.locking.entity.Car;
import com.pavel.marchenko.optimistic.locking.entity.Item;
import com.pavel.marchenko.optimistic.locking.entity.OptimisticLockEntity;
import com.pavel.marchenko.optimistic.locking.exception.OptimisticLockException;
import com.pavel.marchenko.optimistic.locking.repository.CarRepository;
import com.pavel.marchenko.optimistic.locking.repository.ItemRepository;
import com.pavel.marchenko.optimistic.locking.repository.OptimisticLockRepository;
import java.lang.reflect.Field;
import java.util.concurrent.*;
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

@SpringBootApplication
public class DemoApplication {


	@Autowired
	OptimisticLockRepository<Car,Long> carOptimisticLockRepository;

	@Autowired
	OptimisticLockRepository<Item,Long> itemOptimisticLockRepository;

	@Autowired
	ApplicationContext context;


	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);



	}

		@Bean
	public CommandLineRunner demo(OptimisticLockRepository<Car,Long> carOptimisticLockRepository) {
		return (args) -> {

			ItemRepository itemRepositoryProxy = (ItemRepository) Enhancer.create(
					ItemRepository.class,
					new OptimisticLockRepositoryInvocationHandler(context.getBean(ItemRepository.class))
			);

			CarRepository carRepositoryProxy = (CarRepository) Enhancer.create(
					CarRepository.class,
					new OptimisticLockRepositoryInvocationHandler(context.getBean(CarRepository.class))
			);



			Car car2 = new Car("mercedes","bla bla mercedes");
			car2.setItems(new ArrayList<>());

			Item item1 = new Item("ITEM 1");
			Item item2 = new Item("ITEM 2");

			item1 = itemRepositoryProxy.save(item1);
			item2 = itemRepositoryProxy.save(item2);

			car2 = carRepositoryProxy.save(car2);
			System.out.println(car2);
			car2.getItems().add(item1);
			car2.getItems().add(item2);

			car2.setDescription("bla");
			car2 = carRepositoryProxy.save(car2);
			System.out.println(car2);
			car2.setDescription("bla2");
			car2 = carRepositoryProxy.save(car2);
			System.out.println(car2);
			car2.setDescription("ssss");
			carRepositoryProxy.save(car2);

			ExecutorService executor = Executors.newFixedThreadPool(15);

			for (int i = 0 ; i<15 ; i++ ){
				executor.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						System.out.println("_");
						Car car3 = carRepositoryProxy.findOne(3L);
						car3.setDescription("dsddsd");
						carRepositoryProxy.save(car3);
						return null;
					}
				});
			}
		};
	}

	static class OptimisticLockRepositoryInvocationHandler implements  MethodInterceptor{

		CrudRepository<? extends OptimisticLockEntity<Long>,Long> repository;

		public OptimisticLockRepositoryInvocationHandler(CrudRepository<? extends OptimisticLockEntity<Long>, Long> repository) {
			this.repository = repository;
		}

		@Override
		public Object intercept(Object o, Method method, Object[] objects, MethodProxy proxy) throws Throwable {

			boolean isAbleToSave = false;
				//TODO: wrap it to the transaction
			if(method.isAnnotationPresent(OptimisticLockingHandler.class)) {

				if (objects[0] instanceof OptimisticLockEntity<?>) {
						persist:
						while(!isAbleToSave) {
							try {
								checkVersion(objects[0]);
								isAbleToSave=true;
							} catch (OptimisticLockException e) {
								System.out.println("some optimistic lock trouble!");
								objects[0] = repository.findOne(((OptimisticLockEntity<Long> ) objects[0]).getEntityId());
									// TODO: merge entity with previously updated entity
								continue persist;
							}
						}
							// TODO: prePersist(objects[0]);

					Object object = proxy.invoke(repository, objects);
					System.out.println("successfully saved");
					return object;
				} else
					throw new IllegalStateException("Entity must implement OptimisticLockEntity interface");
			}
			else
				return proxy.invoke(repository, objects);
		}

		private void checkVersion(Object o){
			OptimisticLockEntity<Long> entity = (OptimisticLockEntity<Long>) o;
			if(entity.getEntityId()==null)
				return ;
			OptimisticLockEntity<Long> lockEntity = repository.findOne(entity.getEntityId());
			if(!entity.getVersion().equals(lockEntity.getVersion()))
				throw new OptimisticLockException("entity have been previously updated!");
		}

		private void prePersist(Object s) {
			OptimisticLockEntity<Long> entity = (OptimisticLockEntity<Long>) s;

			if(entity.getVersion()==null || entity.getEntityId()==null ) return;
			Field[] fields = s.getClass().getDeclaredFields();
			for (Field f : fields ){
				f.setAccessible(true);
				try {
					if(f.get(s) instanceof Iterable ){
						Iterable iterable = (Iterable) f.get(s);
						iterable.forEach(p -> {
							if(p instanceof OptimisticLockEntity) {
								OptimisticLockEntity ap = ((OptimisticLockEntity) p);
//                            ap.setVersion(entity.getVersion());
							}
						});
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

	}


}
