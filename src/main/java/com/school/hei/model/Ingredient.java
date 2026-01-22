package com.school.hei.model;

import com.school.hei.type.CategoryEnum;
import com.school.hei.type.MovementTypeEnum;
import com.school.hei.type.UnitType;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList;

    public Ingredient(String name, Double price, CategoryEnum category, List<StockMovement> stockMovementList) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockMovementList = stockMovementList;
    }

    public Ingredient(String name, Double price, CategoryEnum category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Ingredient() {}

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Id must be a positive integer");
        }
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

    public Double getPrice() {
        return price;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    public void setPrice(Double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price must not negative");
        }
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public StockValue getStockValueAt(Instant t) {
        Double total = 0.0;

        UnitType type = stockMovementList.getFirst().getValue().getUnit();
        for(StockMovement stockMovement : stockMovementList) {
            if (stockMovement.getCreationDatetime().isAfter(t)) {
                Double qty = stockMovement.getValue().getQuantity();

                if (stockMovement.getType() == MovementTypeEnum.IN) {
                    total += qty;
                }
                else {
                    total -= qty;
                }
            }
        }
        return new StockValue(total, type);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(price, that.price) && category == that.category && Objects.equals(stockMovementList, that.stockMovementList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, category, stockMovementList);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", stockMovementList=" + stockMovementList +
                '}';
    }
}