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
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

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

	static class OptimisticLockRepositoryInvocationHandler implements  MethodInterceptor{

		private final CrudRepository<? extends OptimisticLockEntity<Long>,Long> repository;

		private final PlatformTransactionManager txManager;

		public OptimisticLockRepositoryInvocationHandler(
				CrudRepository<? extends OptimisticLockEntity<Long>, Long> repository,
				PlatformTransactionManager transactionTemplate) {
			this.repository = repository;
			this.txManager=transactionTemplate;
		}

		@Override
		public Object intercept(Object o, Method method, Object[] objects, MethodProxy proxy) throws Throwable {

			boolean isAbleToSave = false;
			if(method.isAnnotationPresent(OptimisticLockingHandler.class)) {
				Object object = null;
				if (objects[0] instanceof OptimisticLockEntity<?>) {
						persist:
						while(!isAbleToSave) {
							DefaultTransactionDefinition def = new DefaultTransactionDefinition();
							def.setName("transaction");
							def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
							TransactionStatus status = txManager.getTransaction(def);
							//begin transaction
							try {
								checkVersion(objects[0]);
								isAbleToSave=true;
								object = proxy.invoke(repository, objects);
								System.out.println("successfully saved :"+objects[0]);
							} catch (OptimisticLockException e) {
								//rollback
								txManager.rollback(status);
								System.out.println("some optimistic lock trouble!");
								Object old = objects[0];
								objects[0] = repository.findOne(((OptimisticLockEntity<Long>)objects[0]).getEntityId());
								prePersist(objects[0],old);
								continue persist;
							}
							//commit
							txManager.commit(status);


						}
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

		private void prePersist(Object s,Object from) {
			OptimisticLockEntity<Long> entity = (OptimisticLockEntity<Long>) s;
			if(entity.getVersion()==null || entity.getEntityId()==null ) return;
			Field[] fields = s.getClass().getDeclaredFields();
			for (Field f : fields ){
				f.setAccessible(true);
				if(
					!Map.class.isAssignableFrom(f.getType()) &&
					!Iterable.class.isAssignableFrom(f.getType()) &&
					!f.isAnnotationPresent(Version.class)
					)
					try {
						f.set(s,f.get(from));
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
			}
		}

	}


}
