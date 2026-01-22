package com.school.hei;

import com.school.hei.model.Dish;
import com.school.hei.model.Ingredient;
import com.school.hei.repository.DataRetriever;
import com.school.hei.type.DishTypeEnum;

import java.util.ArrayList;
import java.util.Collections;

public class Main {
    public static void main() {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("===> Test getDishCost() and getGrossMargin() <===");

        try {
            Dish saladeFraiche = dataRetriever.findDishById(1);
            System.out.println("Plat trouvé : " + saladeFraiche.getName());
            System.out.println("Prix de vente : " + saladeFraiche.getPrice());

            double ingredientsCost = saladeFraiche.getDishCost();
            System.out.println("Coût des ingrédients : " + ingredientsCost);

            try {
                double grossMargin = saladeFraiche.getGrossMargin();
                System.out.println("Marge brute : " + grossMargin);
                System.out.println("   Calcul : " + saladeFraiche.getPrice() + " - " + ingredientsCost + " = " + grossMargin);
            } catch (RuntimeException e) {
                System.out.println("Erreur lors du calcul de la marge : " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
