package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;

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

        pantryItems = databaseHelper.getAllPantryItems();

        pantryAdapter = new PantryAdapter(pantryItems);

        pantryRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        pantryRecyclerView.setAdapter(pantryAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null) {
            pantryItems.clear();
            pantryItems.addAll(databaseHelper.getAllPantryItems());

            if (pantryAdapter != null) {
                pantryAdapter.notifyDataSetChanged();
            }
        }
    }
}
