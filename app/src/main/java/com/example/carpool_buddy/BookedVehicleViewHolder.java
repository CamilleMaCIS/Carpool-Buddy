package com.example.carpool_buddy;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BookedVehicleViewHolder extends RecyclerView.ViewHolder {
    protected TextView ownersTextView;
    protected TextView modelsTextView;
    protected TextView bookingsNumTextView;
    protected TextView vehicleTypeTextView;

    public BookedVehicleViewHolder(@NonNull View itemView) {
        super(itemView);
        ownersTextView = itemView.findViewById(R.id.ownerTextView);
        //in OwnedVehicleViewHolder and owned_vehicle_row_view.xml, there is already a id called modelTextView
        //in order to differentiate the two TextViews, I added "booked" in front of this "modelTextView"
        modelsTextView = itemView.findViewById(R.id.bookedModelTextView);
        bookingsNumTextView = itemView.findViewById(R.id.bookingsNumberTextView);
        //same goes for bookedSeatsLeftNumberTextView
        //in OwnedVehicleViewHolder and owned_vehicle_row_view.xml, there is already a id called seatsLeftNumberTextView
        //in order to differentiate the two TextViews, I added "booked" in front of this "seatsLeftNumberTextView"
        vehicleTypeTextView = itemView.findViewById(R.id.bookedVehicleTypeTextView);
    }
}
