package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeMatch {

    private RecipeMatch() {
        // Utility class; no instance is required.
    }

    public static List<Recipe> getMatchingRecipes(
            List<Recipe> recipes,
            List<PantryItem> pantryItems) {

        List<Recipe> matchingRecipes = new ArrayList<>();

        if (recipes == null || pantryItems == null) {
            return matchingRecipes;
        }

        for (Recipe recipe : recipes) {

            // Ignore recipes without required ingredients.
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }

            boolean canMakeRecipe = true;

            for (RecipeIngredient required : recipe.getIngredients()) {

                double availableQuantity = 0;

                String requiredName =
                        normaliseName(required.getName());

                // Check every pantry item for this ingredient.
                for (PantryItem pantryItem : pantryItems) {

                    String pantryName =
                            normaliseName(pantryItem.getName());

                    if (requiredName.equals(pantryName)
                            && unitsMatch(
                            required.getUnit(),
                            pantryItem.getUnit())) {

                        availableQuantity += pantryItem.getQuantity();
                    }
                }

                // Reject the recipe if an ingredient is missing
                // or its available quantity is insufficient.
                if (availableQuantity
                        < required.getRequiredQuantity()) {

                    canMakeRecipe = false;
                    break;
                }
            }

            // Only complete matches are suggested.
            if (canMakeRecipe) {
                matchingRecipes.add(recipe);
            }
        }

        return matchingRecipes;
    }

    // Normalise names and handle common singular/plural differences.
    private static String normaliseName(String name) {

        if (name == null) {
            return "";
        }

        String normalised = name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        if (normalised.endsWith("ies")
                && normalised.length() > 3) {

            normalised = normalised.substring(
                    0, normalised.length() - 3) + "y";

        } else if (normalised.endsWith("oes")
                && normalised.length() > 3) {

            normalised = normalised.substring(
                    0, normalised.length() - 2);

        } else if (normalised.endsWith("s")
                && !normalised.endsWith("ss")
                && normalised.length() > 2) {

            normalised = normalised.substring(
                    0, normalised.length() - 1);
        }

        return normalised;
    }

    // Compare units after normalising common variations.
    private static boolean unitsMatch(
            String requiredUnit,
            String pantryUnit) {

        if (requiredUnit == null || pantryUnit == null) {
            return false;
        }

        return normaliseUnit(requiredUnit)
                .equals(normaliseUnit(pantryUnit));
    }

    private static String normaliseUnit(String unit) {

        if (unit == null) {
            return "";
        }

        String normalised = unit.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        switch (normalised) {
            case "gram":
            case "grams":
                return "g";

            case "kilogram":
            case "kilograms":
                return "kg";

            case "millilitre":
            case "millilitres":
            case "milliliter":
            case "milliliters":
                return "ml";

            case "litre":
            case "litres":
            case "liter":
            case "liters":
                return "l";

            case "piece":
            case "pieces":
            case "item":
            case "items":
                return "item";

            case "clove":
            case "cloves":
                return "clove";

            case "slice":
            case "slices":
                return "slice";

            case "tablespoon":
            case "tablespoons":
                return "tbsp";

            case "teaspoon":
            case "teaspoons":
                return "tsp";

            default:
                return normalised;


        }
    }
}