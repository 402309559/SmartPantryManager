package com.example.smartpantrymanager;

import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PantryListActivity extends AppCompatActivity {

    private RecyclerView pantryRecyclerView;
    private Button addButton;

    private DatabaseHelper databaseHelper;
    private PantryAdapter pantryAdapter;
    private ArrayList<PantryItem> pantryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        pantryRecyclerView = findViewById(R.id.pantryRecyclerView);
        addButton = findViewById(R.id.addButton);

        databaseHelper = new DatabaseHelper(this);

        // Ensure the initial recipes exist.
        databaseHelper.seedRecipesIfNeeded();

        pantryItems = databaseHelper.getAllPantryItems();

        pantryAdapter = new PantryAdapter(pantryItems, new PantryAdapter.OnItemActionListener() {
            @Override
            public void onEdit(PantryItem item) {
                showIngredientDialog(item);
            }

            @Override
            public void onDelete(PantryItem item) {
                confirmDelete(item);
            }
        }
        );

        pantryRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        pantryRecyclerView.setAdapter(pantryAdapter);

        addButton.setOnClickListener(v -> showIngredientDialog(null));
    }

    private void showIngredientDialog(PantryItem existingItem) {
        boolean isEditing = existingItem != null;

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (20 * getResources()
                .getDisplayMetrics().density);

        form.setPadding(padding, padding / 2, padding, 0);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Ingredient name");
        nameInput.setSingleLine(true);
        form.addView(nameInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        EditText quantityInput = new EditText(this);
        quantityInput.setHint("Quantity");
        quantityInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        form.addView(quantityInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText unitInput = new EditText(this);
        unitInput.setHint("Unit (e.g kg, litres");
        unitInput.setSingleLine(true);
        form.addView(unitInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        EditText expiryInput = new EditText(this);
        expiryInput.setHint("Expiry date (optional: YYYY-MM-DD)");
        expiryInput.setSingleLine(true);
        form.addView(expiryInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Populate the form when editing an existing ingredient
        if (isEditing) {
            nameInput.setText(existingItem.getName());
            quantityInput.setText(
                    String.valueOf(existingItem.getQuantity()));
            unitInput.setText(existingItem.getUnit());

            String expiry = existingItem.getExpiryDate();
            if (expiry != null) {
                expiryInput.setText(expiry);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Ingredient")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(ignore ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                                    String name = nameInput.getText()
                                            .toString().trim();
                                    String quantityText = quantityInput.getText()
                                            .toString().trim();
                                    String unit = unitInput.getText()
                                            .toString().trim();
                                    String expiry = expiryInput.getText()
                                            .toString().trim();

                                    if (name.isEmpty()
                                            || quantityText.isEmpty()
                                            || unit.isEmpty()) {
                                        Toast.makeText(this, "Enter a name, quantity & unit.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    double quantity;
                                    try {
                                        quantity = Double.parseDouble(quantityText);
                                    } catch (NumberFormatException e) {
                                        Toast.makeText(this, "Enter a valid quantity.",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    if (!Double.isFinite(quantity)
                                            || quantity <= 0) {
                                        Toast.makeText(this, "Quantity must be greater than zero.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String expiryDate = expiry.isEmpty()
                                            ? null : expiry;

                                    long result;

                                    if (isEditing) {
                                        // Preserve the ID of the existing database record.
                                        PantryItem updatedItem = new PantryItem(
                                                existingItem.getId(),
                                                name,
                                                quantity,
                                                unit,
                                                expiryDate
                                        );

                                        result = databaseHelper.updatePantryItem(updatedItem);

                                    } else {
                                        // Insert a new ingredient into the database.
                                        PantryItem newItem = new PantryItem(
                                                name,
                                                quantity,
                                                unit,
                                                expiryDate
                                        );

                                        result = databaseHelper.addPantryItem(newItem);
                                    }

                                    if (result > 0) {
                                        refreshPantryItems();

                                        Toast.makeText(
                                                this,
                                                isEditing
                                                        ? "Ingredient updated."
                                                        : "Ingredient added successfully.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        dialog.dismiss();

                                    } else {
                                        Toast.makeText(
                                                this,
                                                isEditing
                                                        ? "Could not update ingredient."
                                                        : "Could not save ingredient.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        ));

        dialog.show();
    }

    private LinearLayout.LayoutParams createFieldParams() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Delete " + item.getName()
                        + " from your pantry?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {

                    int result = databaseHelper.deletePantryItem(
                            item.getId());

                    if (result > 0) {
                        refreshPantryItems();

                        Toast.makeText(this,
                                "Ingredient deleted.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Could not delete ingredient.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void refreshPantryItems() {
        pantryItems.clear();
        pantryItems.addAll(databaseHelper.getAllPantryItems());
        pantryAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && pantryItems != null && pantryAdapter != null) {
            refreshPantryItems();
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}