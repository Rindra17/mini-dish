package com.school.hei.model;

import com.school.hei.type.DishTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dish {
    private Integer id;
    private Double price;
    private String name;
    private DishTypeEnum dishType;
    private List<DishIngredient> dishIngredients;

    public Dish() {
    }

    public Dish(Double price, String name, DishTypeEnum dishType, List<DishIngredient> dishIngredients) {
        this.price = price;
        this.name = name;
        this.dishType = dishType;
        this.dishIngredients = dishIngredients;
    }

    public Dish(Integer id, Double price, String name, DishTypeEnum dishType, List<DishIngredient> dishIngredients) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.dishType = dishType;
        this.dishIngredients = dishIngredients;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be empty or null");
        }
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> newDishIngredients) {
        if (this.dishIngredients == null) {
            this.dishIngredients = new ArrayList<>();
        }
        else {
            this.dishIngredients.clear();
        }

        if (newDishIngredients != null) {
            for (DishIngredient di : newDishIngredients) {
                if (di != null) {
                    di.setDish(this);
                    this.dishIngredients.add(di);
                }
            }
        }
    }

    public Double getDishCost() {
        if (dishIngredients == null || dishIngredients.isEmpty()) {
            return 0.0;
        }

        return dishIngredients.stream()
                .mapToDouble(DishIngredient::getCost)
                .sum();
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getGrossMargin() {
        if (price == null) {
            throw new RuntimeException("Price ist null");
        }

        return (this.price - getDishCost());
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", price=" + price +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", dishIngredients=" + dishIngredients +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id) && Objects.equals(price, dish.price) && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(dishIngredients, dish.dishIngredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, name, dishType, dishIngredients);
    }
}
