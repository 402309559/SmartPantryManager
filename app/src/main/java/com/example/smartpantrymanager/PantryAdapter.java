package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/* Connects pantry ingredient data to the RecyclerView. Takes each PantryItem and
displays its details in pantry_item.xml */

public class PantryAdapter
        extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface OnItemActionListener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    //This will hold the pantry ingredients being displayed.
    private final List<PantryItem> pantryItems;
    private final OnItemActionListener listener;
    /*The constructor receives pantry items from the Activity.
     pantryItems the ingredients to display*/
    public PantryAdapter(List<PantryItem> pantryItems, OnItemActionListener listener) {
        this.pantryItems = pantryItems;
        this.listener = listener;
    }

    /* Creates a new row by loading the pantry_item.xml layout.
     RecyclerView calls this when it needs a new row view. */

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        // Convert the XML layout into a View object.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pantry_item, parent, false);

        //Wrap view in a ViewHolder
        return new PantryViewHolder(view);
    }

    // Fills a row with the data for the ingredient at the selected position.
    @Override
    public void onBindViewHolder(
            @NonNull PantryViewHolder holder, int position) {
        // Get the ingredient for this row
        PantryItem item = pantryItems.get(position);

        //Display the ingredient name
        holder.itemName.setText(item.getName());

        // Display its quantity & unit
        holder.itemQuantity.setText(
                "Quantity: " + item.getQuantity() + " " +
item.getUnit());

        // Retrieve the expiry date
        String expiryDate = item.getExpiryDate();

        //Hide the expiry label if the ingredient has no expiry date
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            holder.itemExpiry.setVisibility(View.GONE);
        } else {
            // Otherwise, display the expiry date
            holder.itemExpiry.setVisibility(View.VISIBLE);
            holder.itemExpiry.setText("Expiry: " + expiryDate);
        }

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(item);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    /* Returns the number of items in the ingredient list,
    RecyclerView uses this to determine the amount of rows required*/
    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    /*This holds the views in one pantry row,
    ViewHolder avoids repeatedly searching for the layout for each view*/

    static class PantryViewHolder extends RecyclerView.ViewHolder
    {

        //Text fields defined in pantry_item.xmlTextView itemName;
        TextView itemName;
        TextView itemQuantity;
        TextView itemExpiry;
        Button editButton;
        Button deleteButton;
        PantryViewHolder(@NonNull View itemView) {
            super(itemView);

            // Connect the Java variables to the XML TextViews
            itemName = itemView.findViewById(R.id.itemName);
            itemQuantity= itemView.findViewById(R.id.itemQuantity);
            itemExpiry = itemView.findViewById(R.id.itemExpiry);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
