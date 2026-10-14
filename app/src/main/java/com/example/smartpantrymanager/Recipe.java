package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;

public class Recipe {

    private final int id;
    private final String name;
    private final String instructions;
    private final List<RecipeIngredient> ingredients;

    public Recipe(
            int id,
            String name,
            String instructions,
            List<RecipeIngredient> ingredients) {

        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.ingredients = ingredients == null
                ? new ArrayList<>()
                : new ArrayList<>(ingredients);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInstructions() {
        return instructions;
    }

    public List<RecipeIngredient> getIngredients() {
        return new ArrayList<>(ingredients);
    }
}