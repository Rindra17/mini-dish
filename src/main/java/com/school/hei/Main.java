package com.school.hei;

import com.school.hei.model.Dish;
import com.school.hei.model.Ingredient;
import com.school.hei.repository.DataRetriever;
import com.school.hei.type.DishTypeEnum;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

public class Main {
    public static void main() {
        DataRetriever dr = new DataRetriever();

        Ingredient ing1 = dr.findIngredientById(1);
        Ingredient ing2 = dr.findIngredientById(2);
        Ingredient ing3 = dr.findIngredientById(3);
        Ingredient ing4 = dr.findIngredientById(4);
        Ingredient ing5 = dr.findIngredientById(5);
        Instant t = Instant.parse("2024-01-06T12:00:00Z");
        System.out.println(ing1.getStockValueAt(t));
        System.out.println(ing2.getStockValueAt(t));
        System.out.println(ing3.getStockValueAt(t));
        System.out.println(ing4.getStockValueAt(t));
        System.out.println(ing5.getStockValueAt(t));
    }
}
