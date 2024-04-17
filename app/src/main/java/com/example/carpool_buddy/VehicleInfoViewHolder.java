package com.example.carpool_buddy;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VehicleInfoViewHolder extends RecyclerView.ViewHolder {

    protected TextView ownerNameTextView;
    protected TextView seatsLeftTextView;
    protected TextView vehicleTypeTextView;
    public VehicleInfoViewHolder(@NonNull View itemView) {
        super(itemView);

        ownerNameTextView = itemView.findViewById(R.id.ownerNameTextView);
        seatsLeftTextView = itemView.findViewById(R.id.seatsLeftTextViewVehicleRow);
        vehicleTypeTextView = itemView.findViewById(R.id.vehicleTypeTextViewVehicleRow);
    }
}
