package com.example.smartpantrymanager;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeSeeder {

    private RecipeSeeder() {
        // Prevent creating instances of this utility class.
    }

    public static void seedRecipes(SQLiteDatabase db) {

        // Avoid inserting the recipes again if they already exist.
        try (android.database.Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM recipes WHERE name = ?",
                new String[]{"Tomato Pasta"})) {

            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                return;
            }
        }

        db.beginTransaction();

        try {
            insertRecipe(db, "Tomato Pasta",
                    "Boil pasta. Cook tomatoes and garlic in a pan. " +
                            "Combine and serve.",
                    ingredient("pasta", 200, "g"),
                    ingredient("tomato", 3, "items"),
                    ingredient("garlic", 2, "cloves"));

            insertRecipe(db, "Vegetable Fried Rice",
                    "Cook rice. Fry carrots and peas, add rice and soy sauce.",
                    ingredient("rice", 200, "g"),
                    ingredient("carrot", 1, "item"),
                    ingredient("peas", 100, "g"),
                    ingredient("soy sauce", 15, "ml"));

            insertRecipe(db, "Cheese Omelette",
                    "Beat eggs, cook in a pan and add cheese before folding.",
                    ingredient("egg", 2, "items"),
                    ingredient("cheese", 30, "g"));

            insertRecipe(db, "Garlic Bread",
                    "Spread garlic butter on bread and toast until golden.",
                    ingredient("bread", 2, "slices"),
                    ingredient("garlic", 1, "clove"),
                    ingredient("butter", 20, "g"));

            insertRecipe(db, "Banana Pancakes",
                    "Mash banana, mix with egg and flour, then fry small pancakes.",
                    ingredient("banana", 1, "item"),
                    ingredient("egg", 1, "item"),
                    ingredient("flour", 100, "g"));

            insertRecipe(db, "Tomato Soup",
                    "Cook tomatoes and onion in stock, then blend until smooth.",
                    ingredient("tomato", 4, "items"),
                    ingredient("onion", 1, "item"),
                    ingredient("vegetable stock", 500, "ml"));

            insertRecipe(db, "Cheese Sandwich",
                    "Place cheese between bread slices and toast if desired.",
                    ingredient("bread", 2, "slices"),
                    ingredient("cheese", 40, "g"));

            insertRecipe(db, "Scrambled Eggs",
                    "Beat eggs and milk. Stir gently in a pan until cooked.",
                    ingredient("egg", 2, "items"),
                    ingredient("milk", 30, "ml"),
                    ingredient("butter", 10, "g"));

            insertRecipe(db, "Carrot Soup",
                    "Boil carrots and onion in stock until soft, then blend.",
                    ingredient("carrot", 3, "items"),
                    ingredient("onion", 1, "item"),
                    ingredient("vegetable stock", 500, "ml"));

            insertRecipe(db, "Peanut Butter Toast",
                    "Toast bread and spread peanut butter over it.",
                    ingredient("bread", 2, "slices"),
                    ingredient("peanut butter", 30, "g"));

            insertRecipe(db, "Apple Oatmeal",
                    "Cook oats in milk and top with chopped apple.",
                    ingredient("oats", 50, "g"),
                    ingredient("milk", 200, "ml"),
                    ingredient("apple", 1, "item"));

            insertRecipe(db, "Mashed Potatoes",
                    "Boil potatoes until soft, mash with butter and milk.",
                    ingredient("potato", 300, "g"),
                    ingredient("butter", 20, "g"),
                    ingredient("milk", 50, "ml"));

            insertRecipe(db, "Simple Garden Salad",
                    "Chop vegetables and toss together with olive oil.",
                    ingredient("lettuce", 100, "g"),
                    ingredient("tomato", 2, "items"),
                    ingredient("cucumber", 1, "item"),
                    ingredient("olive oil", 15, "ml"));

            insertRecipe(db, "Carrot Fritters",
                    "Grate carrots, mix with egg and flour, then pan-fry.",
                    ingredient("carrot", 2, "items"),
                    ingredient("egg", 1, "item"),
                    ingredient("flour", 50, "g"));

            insertRecipe(db, "Mushroom Omelette",
                    "Cook mushrooms, add beaten eggs and cook until set.",
                    ingredient("egg", 2, "items"),
                    ingredient("mushroom", 100, "g"));

            insertRecipe(db, "Lemon Water",
                    "Squeeze lemon into water and stir.",
                    ingredient("lemon", 1, "item"),
                    ingredient("water", 250, "ml"));

            insertRecipe(db, "Yogurt Fruit Bowl",
                    "Slice fruit and serve with yogurt.",
                    ingredient("yogurt", 150, "g"),
                    ingredient("banana", 1, "item"),
                    ingredient("apple", 1, "item"));

            insertRecipe(db, "Onion Fried Potatoes",
                    "Cook diced potatoes and onion in a pan until tender.",
                    ingredient("potato", 250, "g"),
                    ingredient("onion", 1, "item"),
                    ingredient("olive oil", 15, "ml"));

            insertRecipe(db, "Simple Porridge",
                    "Simmer oats in milk until thick, stirring regularly.",
                    ingredient("oats", 50, "g"),
                    ingredient("milk", 250, "ml"));

            insertRecipe(db, "Tomato Cheese Toast",
                    "Top bread with tomato and cheese, then grill until melted.",
                    ingredient("bread", 2, "slices"),
                    ingredient("tomato", 1, "item"),
                    ingredient("cheese", 30, "g"));

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    private static RecipeIngredient ingredient(
            String name, double quantity, String unit) {

        return new RecipeIngredient(name, quantity, unit);
    }

    private static void insertRecipe(
            SQLiteDatabase db,
            String name,
            String instructions,
            RecipeIngredient... ingredients) {

        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);

        StringBuilder summary = new StringBuilder();

        for (RecipeIngredient ingredient : ingredients) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append(ingredient.getName());
        }

        recipeValues.put("ingredients", summary.toString());
        recipeValues.put("instructions", instructions);

        long recipeId = db.insertOrThrow(
                "recipes", null, recipeValues);

        for (RecipeIngredient ingredient : ingredients) {

            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put("recipe_id", recipeId);
            ingredientValues.put(
                    "ingredient_name", ingredient.getName());
            ingredientValues.put(
                    "required_quantity",
                    ingredient.getRequiredQuantity());
            ingredientValues.put("unit", ingredient.getUnit());

            db.insertOrThrow(
                    "recipe_ingredients", null, ingredientValues);
        }
    }
}