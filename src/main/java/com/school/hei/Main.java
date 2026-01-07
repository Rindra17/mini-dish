package com.school.hei;

import com.school.hei.model.Dish;
import com.school.hei.model.Ingredient;
import com.school.hei.repository.DataRetriever;
import com.school.hei.type.CategoryEnum;
import com.school.hei.type.DishTypeEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main() {
        DataRetriever dataRetriever = new DataRetriever();

// a) Dish findDishById(Integer id) - id = 1
        System.out.println("===> Dish findDishById(Integer id) | id = 1 <===");
        Dish dishA = dataRetriever.findDishById(1);
        System.out.println("id=1 : " + dishA);

        // b) Dish findDishById(Integer id) - id = 999
        System.out.println("\n===> Dish findDishById(Integer id) | id = 999 <===");
        try {
            Dish dishB = dataRetriever.findDishById(999);
            System.out.println("id=1 : " + dishB);
        } catch (RuntimeException e) {
            System.out.println("Dish with id 999 not found. Error: " + e);
        }

        // c) List<Ingredient> findIngredients(int page, int size) - page=2 size=2
        System.out.println(
                "\n===> List<Ingredient> findIngredients(int page, int size) | page=2,size=2 <===");
        List<Ingredient> ingredientListC = dataRetriever.findIngredients(2, 2);
        System.out.println("page=2,size=2 : " + ingredientListC);

        // d) List<Ingredient> findIngredients(int page, int size) - page=3 size=5
        System.out.println(
                "\n===> List<Ingredient> findIngredients(int page, int size) | page=3,size=5 <===");
        List<Ingredient> ingredientListD = dataRetriever.findIngredients(3, 5);
        System.out.println("page=2,size=2 : " + ingredientListD);

        // e) List<Ingredient> findDishesByIngredientName(String IngredientName) - eur
        System.out.println(
                "\n===> List<Ingredient> findDishesByIngredientName(String IngredientName) | eur <===");
        List<Dish> dishesByIngredientNameE = dataRetriever.findDishesByIngredientName("eur");
        System.out.println("dishesByIngredientName : " + dishesByIngredientNameE);

        // f) List<Ingredient> findIngredientsByCriteria(...) - ingredientName=null category=VEGETABLE
        // dishName=null page=1 size=10
        System.out.println(
                "\n===> List<Ingredient> findIngredientsByCriteria(...) | ingredientName=null category=VEGETABLE dishName=null page=1 size=10 <===");
        List<Ingredient> ingredientListF =
                dataRetriever.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10);
        System.out.println("ingredientList : " + ingredientListF);

        // g) List<Ingredient> findIngredientsByCriteria(...) - ingredientName=cho category=null
        // dishName=Sal page=1 size=10
        System.out.println(
                "\n===> List<Ingredient> findIngredientsByCriteria(...) | ingredientName=cho category=null dishName=Sal page=1 size=10 <===");
        List<Ingredient> ingredientListG =
                dataRetriever.findIngredientsByCriteria("cho", null, "Sal", 1, 10);
        System.out.println("ingredientList : " + ingredientListG);

        // h) List<Ingredient> findIngredientsByCriteria(...) - ingredientName=cho category=null
        // dishName=gâteau page=1 size=10
        System.out.println(
                "\n===> List<Ingredient> findIngredientsByCriteria(...) | ingredientName=cho category=null dishName=gâteau page=1 size=10");
        List<Ingredient> ingredientListH =
                dataRetriever.findIngredientsByCriteria("cho", null, "gâteau", 1, 10);
        System.out.println("ingredientList : " + ingredientListH);

        // i) List<Ingredient> createIngredient(...) - fromage and oignon
        System.out.println("\n===> List<Ingredient> createIngredient(...) | fromage and oignon <===");
        Ingredient fromage = new Ingredient("Fromage", 1200.0, CategoryEnum.DAIRY);
        Ingredient oignon = new Ingredient("Oignon", 500.0, CategoryEnum.VEGETABLE);
        try {
            List<Ingredient> createdIngredientsI =
                    dataRetriever.createIngredients(new ArrayList<>(Arrays.asList(fromage, oignon)));
            System.out.println("createdIngredients : " + createdIngredientsI);
        } catch (RuntimeException e) {
            System.out.println("Error while creating ingredients : " + e);
        }

        // j) List<Ingredient> createIngredient(...) - carotte and laitue
        System.out.println("\n===> List<Ingredient> createIngredient(...) | carotte and laitue <===");
        Ingredient carotte = new Ingredient("Carotte", 2000.0, CategoryEnum.VEGETABLE);
        Ingredient laitue = new Ingredient("Laitue", 2000.0, CategoryEnum.VEGETABLE);
        try {
            List<Ingredient> createdIngredientsJ =
                    dataRetriever.createIngredients(new ArrayList<>(Arrays.asList(carotte, laitue)));
            System.out.println("createdIngredients : " + createdIngredientsJ);
        } catch (RuntimeException e) {
            System.out.println("Error while creating ingredients : " + e);

            // k) Dish saveDish(...) - soupe de légumes
            System.out.println(
                    "\n===> Dish saveDish(...) | name=Soupe de légumes dishType=START ingredients=Oignon <===");
            Ingredient oignonIngredient = dataRetriever.findIngredientByName("oignon");
            Dish newDishK =
                    new Dish(
                            "Soupe de légumes",
                            DishTypeEnum.START,
                            new ArrayList<>(Collections.singletonList(oignonIngredient)));
            Dish savedDishK = dataRetriever.saveDish(newDishK);
            System.out.println("savedDish : " + savedDishK);

            // l) Dish saveDish(...) - salade fraîche
            System.out.println(
                    "\n===> Dish saveDish(...) | id=1 name=Salade fraîche dishType=START ingredients=Oignon, Laitue, Tomate, Fromage <===");
            Ingredient laitueIngredient = dataRetriever.findIngredientByName("laitue");
            Ingredient fromageIngredient = dataRetriever.findIngredientByName("fromage");
            Ingredient tomateIngredient = dataRetriever.findIngredientByName("tomate");
            Dish newDishL =
                    new Dish(
                            1,
                            "Salade fraîche",
                            DishTypeEnum.START,
                            new ArrayList<>(
                                    Arrays.asList(
                                            oignonIngredient, laitueIngredient, tomateIngredient, fromageIngredient)));
            Dish savedDishL = dataRetriever.saveDish(newDishL);
            System.out.println("savedDish : " + savedDishL);

            // m) Dish saveDish(...) - Salade de fromage
            System.out.println(
                    "\n===> Dish saveDish(...) | id=1 name=Salade de fromage dishType=START ingredients=Fromage <===");
            Dish newDishM =
                    new Dish(
                            1,
                            "Salade de fromage",
                            DishTypeEnum.START,
                            new ArrayList<>(Collections.singletonList(fromageIngredient)));
            Dish savedDishM = dataRetriever.saveDish(newDishM);
            System.out.println("savedDish : " + savedDishM);
        }
    }
}
