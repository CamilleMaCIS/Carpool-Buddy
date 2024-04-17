package com.example.carpool_buddy;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OwnedVehicleViewHolder extends RecyclerView.ViewHolder {
    protected TextView modelsTextView;
    protected TextView isOpensTextView;
    protected TextView seatsNumTextView;

    protected TextView vehTypeTextView;
    public OwnedVehicleViewHolder(@NonNull View itemView) {
        super(itemView);

        modelsTextView = itemView.findViewById(R.id.modelTextView);
        isOpensTextView = itemView.findViewById(R.id.isOpenTextView);
        seatsNumTextView = itemView.findViewById(R.id.seatsLeftNumberTextView);
        vehTypeTextView = itemView.findViewById(R.id.vehicleTypeTextView);
    }
}
