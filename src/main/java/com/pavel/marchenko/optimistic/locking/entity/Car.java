package com.pavel.marchenko.optimistic.locking.entity;



import javax.persistence.*;
import java.util.List;

/**
 * Created by pyshankov on 24.06.16.
 */

@Entity
public class Car extends AbstractOptimisticLockEntity<Long> {

    @Column(name = "model",unique = true, nullable = false)
    private String model;
    private String description;

    @OneToMany(cascade = {CascadeType.ALL} )
//    @OneToMany
    @JoinColumn(name="car_id")
    private List<Item> items;

    public Car(){};

    public Car( String model, String des){
        this.model=model;
        this.description=des;
    };

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Car{" +
                "model='" + model + '\'' +
                ", description='" + description + '\'' +
                ", items=" + items + '\''+
                 ", version=" + version+ '\''+
                ", id = "+id+ '\''+
                '}';
    }
}
