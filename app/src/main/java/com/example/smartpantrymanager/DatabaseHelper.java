package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartPantry.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Store the user's pantry ingredients.
        db.execSQL(
                "CREATE TABLE pantry_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "quantity REAL NOT NULL, " +
                        "unit TEXT NOT NULL, " +
                        "expiry_date TEXT)"
        );

        // Store recipe names and preparation instructions.
        // The ingredients column is retained for compatibility
        // with the original database structure.
        db.execSQL(
                "CREATE TABLE recipes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "ingredients TEXT NOT NULL, " +
                        "instructions TEXT NOT NULL)"
        );

        createRecipeIngredientsTable(db);

        // Insert the initial recipe collection.
        RecipeSeeder.seedRecipes(db);

}

    private void createRecipeIngredientsTable(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS recipe_ingredients (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "recipe_id INTEGER NOT NULL, " +
                        "ingredient_name TEXT NOT NULL, " +
                        "required_quantity REAL NOT NULL, " +
                        "unit TEXT NOT NULL, " +
                        "FOREIGN KEY(recipe_id) REFERENCES recipes(id) " +
                        "ON DELETE CASCADE)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        // Version 2 already has pantry_items and recipes.
        // Add the new table without deleting existing records.
        if (oldVersion < 3) {
            createRecipeIngredientsTable(db);
        }
    }

    // Add a pantry ingredient.
    public long addPantryItem(PantryItem item) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry_date", item.getExpiryDate());

        return db.insert("pantry_items", null, values);
    }

    // Retrieve all pantry ingredients.
    public ArrayList<PantryItem> getAllPantryItems() {

        ArrayList<PantryItem> pantryItems = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT id, name, quantity, unit, expiry_date " +
                        "FROM pantry_items ORDER BY name",
                null)) {

            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id"));

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name"));

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("quantity"));

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow("unit"));

                String expiryDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("expiry_date"));

                pantryItems.add(new PantryItem(
                        id, name, quantity, unit, expiryDate));
            }
        }

        return pantryItems;
    }

    // Update an existing pantry ingredient.
    public int updatePantryItem(PantryItem item) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry_date", item.getExpiryDate());

        return db.update(
                "pantry_items",
                values,
                "id = ?",
                new String[]{String.valueOf(item.getId())}
        );
    }

    // Delete a pantry ingredient.
    public int deletePantryItem(int id) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                "pantry_items",
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }
    // Insert a recipe and its required ingredients.
    public long addRecipe(Recipe recipe) {

        SQLiteDatabase db = getWritableDatabase();
        long recipeId = -1;

        db.beginTransaction();

        try {
            ContentValues recipeValues = new ContentValues();
            recipeValues.put("name", recipe.getName());

            // Keep a readable ingredient summary for compatibility.
            StringBuilder ingredientSummary = new StringBuilder();

            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                if (ingredientSummary.length() > 0) {
                    ingredientSummary.append(", ");
                }
                ingredientSummary.append(ingredient.getName());
            }

            recipeValues.put("ingredients", ingredientSummary.toString());
            recipeValues.put("instructions", recipe.getInstructions());

            recipeId = db.insert("recipes", null, recipeValues);

            if (recipeId == -1) {
                return -1;
            }

            for (RecipeIngredient ingredient : recipe.getIngredients()) {

                ContentValues ingredientValues = new ContentValues();
                ingredientValues.put("recipe_id", recipeId);
                ingredientValues.put("ingredient_name", ingredient.getName());
                ingredientValues.put(
                        "required_quantity",
                        ingredient.getRequiredQuantity()
                );
                ingredientValues.put("unit", ingredient.getUnit());

                long ingredientId = db.insert(
                        "recipe_ingredients",
                        null,
                        ingredientValues
                );

                if (ingredientId == -1) {
                    return -1;
                }
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        return recipeId;
    }

    // Retrieve all recipes and their required ingredients.
    public ArrayList<Recipe> getAllRecipes() {

        ArrayList<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor recipeCursor = db.rawQuery(
                "SELECT id, name, instructions FROM recipes ORDER BY name",
                null)) {

            while (recipeCursor.moveToNext()) {

                int recipeId = recipeCursor.getInt(
                        recipeCursor.getColumnIndexOrThrow("id"));

                String recipeName = recipeCursor.getString(
                        recipeCursor.getColumnIndexOrThrow("name"));

                String instructions = recipeCursor.getString(
                        recipeCursor.getColumnIndexOrThrow("instructions"));

                ArrayList<RecipeIngredient> ingredients = new ArrayList<>();

                try (Cursor ingredientCursor = db.rawQuery(
                        "SELECT ingredient_name, required_quantity, unit " +
                                "FROM recipe_ingredients WHERE recipe_id = ? " +
                                "ORDER BY id",
                        new String[]{String.valueOf(recipeId)})) {

                    while (ingredientCursor.moveToNext()) {

                        String ingredientName = ingredientCursor.getString(
                                ingredientCursor.getColumnIndexOrThrow(
                                        "ingredient_name"));

                        double quantity = ingredientCursor.getDouble(
                                ingredientCursor.getColumnIndexOrThrow(
                                        "required_quantity"));

                        String unit = ingredientCursor.getString(
                                ingredientCursor.getColumnIndexOrThrow("unit"));

                        ingredients.add(new RecipeIngredient(
                                ingredientName, quantity, unit));
                    }
                }

                recipes.add(new Recipe(
                        recipeId, recipeName, instructions, ingredients));
            }
        }

        return recipes;
    }
    // Ensure recipes exist in the database.
    public void seedRecipesIfNeeded() {

        SQLiteDatabase db = getWritableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM recipes", null)) {

            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                RecipeSeeder.seedRecipes(db);
            }
        }
    }
    // Return recipes that can be made with the current pantry.
    public ArrayList<Recipe> getMatchingRecipes() {

        ArrayList<Recipe> recipes = getAllRecipes();
        ArrayList<PantryItem> pantryItems = getAllPantryItems();

        return new ArrayList<>(
                RecipeMatch.getMatchingRecipes(recipes, pantryItems)
        );
    }
}
