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

        System.out.println("===> TEST 1 : findDishById avec un plat ayant un prix (Salade fraîche) <===");

        try {
            Dish saladeFraiche = dataRetriever.findDishById(1);
            System.out.println("Plat trouvé : " + saladeFraiche.getName());
            System.out.println("Prix de vente : " + saladeFraiche.getPrice());
            System.out.println("Nombre d'ingrédients : " + saladeFraiche.getIngredients().size());

            double ingredientsCost = saladeFraiche.getIngredients().stream()
                    .mapToDouble(Ingredient::getPrice)
                    .sum();
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

        System.out.println("\n===> TEST 2 : findDishById avec un plat SANS prix (Riz aux légumes) <===");

        try {
            Dish rizLegumes = dataRetriever.findDishById(4);
            System.out.println("Plat trouvé : " + rizLegumes.getName());
            System.out.println("Prix de vente : " + rizLegumes.getPrice());
            System.out.println("Nombre d'ingrédients : " + rizLegumes.getIngredients().size());

            try {
                double grossMargin = rizLegumes.getGrossMargin();
                System.out.println("❌ ERREUR : La marge a été calculée alors que le prix est null : " + grossMargin);
            } catch (RuntimeException e) {
                System.out.println("Exception levée comme attendu : " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }

        System.out.println("\n===> TEST 3 : CRÉATION d'un nouveau plat AVEC prix <===");

        try {
            Ingredient poulet = dataRetriever.findIngredientByName("Poulet");

            Dish newDish = new Dish(
                    "Poulet rôti",
                    DishTypeEnum.MAIN,
                    new ArrayList<>(Collections.singletonList(poulet))
            );
            newDish.setPrice(8000.0);

            System.out.println("Création du plat : " + newDish.getName());
            System.out.println("Prix défini : " + newDish.getPrice());
            System.out.println("Coût des ingrédients : " + newDish.getDishCost());
            System.out.println("Marge brute attendue : " + (newDish.getPrice() - newDish.getDishCost()));

            Dish savedDish = dataRetriever.saveDish(newDish);

            System.out.println("\nPlat créé avec succès !");
            System.out.println("   ID généré : " + savedDish.getId());
            System.out.println("   Nom : " + savedDish.getName());
            System.out.println("   Prix : " + savedDish.getPrice());
            System.out.println("   Coût : " + savedDish.getDishCost());

            try {
                double margin = savedDish.getGrossMargin();
                System.out.println("   Marge brute : " + margin);
            } catch (RuntimeException e) {
                System.out.println("   ERREUR : " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        System.out.println("\n===> TEST 4 : MISE À JOUR d'un plat existant - Modifier le prix <===");

        try {
            Dish existingDish = dataRetriever.findDishById(1);

            System.out.println("Plat existant : " + existingDish.getName() + " (ID: " + existingDish.getId() + ")");
            System.out.println("Prix actuel : " + existingDish.getPrice());
            System.out.println("Coût actuel : " + existingDish.getDishCost());

            Double ancienPrix = existingDish.getPrice();
            Double nouveauPrix = 2500.0;

            existingDish.setPrice(nouveauPrix);
            System.out.println("\nNouveau prix défini : " + nouveauPrix);

            Dish updatedDish = dataRetriever.saveDish(existingDish);

            System.out.println("\nPlat mis à jour avec succès !");
            System.out.println("   ID : " + updatedDish.getId());
            System.out.println("   Nom : " + updatedDish.getName());
            System.out.println("   Ancien prix : " + ancienPrix);
            System.out.println("   Nouveau prix : " + updatedDish.getPrice());

            if (updatedDish.getPrice().equals(nouveauPrix)) {
                System.out.println("   Le prix a été correctement mis à jour");
            } else {
                System.out.println("   ERREUR : Le prix n'a pas été mis à jour (attendu: " + nouveauPrix + ", obtenu: " + updatedDish.getPrice() + ")");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
