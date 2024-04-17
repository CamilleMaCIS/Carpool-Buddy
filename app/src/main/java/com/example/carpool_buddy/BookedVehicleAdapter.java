package com.example.carpool_buddy;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BookedVehicleAdapter extends RecyclerView.Adapter<BookedVehicleViewHolder>{

    private ArrayList<String> owners;

    private ArrayList<String> models;

    private ArrayList<Integer> bookingCounts;
    private ArrayList<String> vehicleTypes;
    private ArrayList<String> vehicleIDs;

    //constructor
    public BookedVehicleAdapter(ArrayList<String> owners, ArrayList<String> models, ArrayList<Integer> bookingCounts, ArrayList<String> vehicleTypes, ArrayList<String> vehicleIDs){
        this.owners = owners;
        this.models = models;
        this.bookingCounts = bookingCounts;
        this.vehicleTypes = vehicleTypes;
        this.vehicleIDs = vehicleIDs;
    }

    @NonNull
    @Override
    public BookedVehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booked_vehicle_row_view, parent, false);

        //create ViewHolder object
        BookedVehicleViewHolder viewHolder = new BookedVehicleViewHolder(view);
        return viewHolder;
    }

    //int position is the index for the ArrayList
    @Override
    public void onBindViewHolder(@NonNull BookedVehicleViewHolder holder, int position) {
        //sets the text for the vehicle owner based on BookedVehicle object info
        holder.ownersTextView.setText(owners.get(position));
        //sets the text for the vehicle model based on BookedVehicle object info
        holder.modelsTextView.setText(models.get(position));
        //sets the text for amount of bookings of this vehicle based on BookedVehicle object info
        //Note that bookingCounts holds Integer elements. setText() only takes in Strings, so must convert the Integer types to String type
        holder.bookingsNumTextView.setText(String.valueOf(bookingCounts.get(position)));
        //sets the text for the vehicle type (e.g. electric car, car, truck...) based on vehicle info
        holder.vehicleTypeTextView.setText(vehicleTypes.get(position));

        //this will be used to pass the Vehicle object and it's information to VehicleProfileActivity
        String currentVehicleID = vehicleIDs.get(position);

        //task 9 on document, adding intent to ViewHolder which will take user to VehicleProfileActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), VehicleProfileActivity.class);
                //passing the vehicle Object information to VehicleProfileActivity
                intent.putExtra("vehicleID", currentVehicleID);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }
}
