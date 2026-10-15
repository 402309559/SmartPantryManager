package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enables edge to edge display for the welcome screen.
        EdgeToEdge.enable(this);

        // Loads the welcome screen layout.
        setContentView(R.layout.activity_main);

        // Applies the appropriate spacing around the system bars.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return insets;
                });

        // Finds the Get Started button in the welcome screen layout.
        Button getStartedButton = findViewById(R.id.getStartedButton);

        // Opens the pantry list screen when the button is tapped.
        getStartedButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    PantryListActivity.class
            );

            startActivity(intent);
        });
    }
}