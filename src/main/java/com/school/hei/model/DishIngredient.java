package com.school.hei.model;

import com.school.hei.type.UnitType;

import java.util.Objects;

public class DishIngredient {
    private Integer id;
    private Integer dishId;
    private Integer ingredientId;
    private Double quantity;
    private UnitType unit;

    public DishIngredient() {
    }

    public DishIngredient(Integer dishId, Integer ingredientId, Double quantity, UnitType unit) {
        this.dishId = dishId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Integer getDishId() {
        return dishId;
    }

    public void setDishId(Integer dishId) {
        this.dishId = dishId;
    }

    public Integer getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Integer ingredientId) {
        this.ingredientId = ingredientId;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredient that = (DishIngredient) o;
        return Objects.equals(id, that.id) && Objects.equals(dishId, that.dishId) && Objects.equals(ingredientId, that.ingredientId) && Objects.equals(quantity, that.quantity) && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dishId, ingredientId, quantity, unit);
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "id=" + id +
                ", dishId=" + dishId +
                ", ingredientId=" + ingredientId +
                ", quantity=" + quantity +
                ", unit=" + unit +
                '}';
    }
}
