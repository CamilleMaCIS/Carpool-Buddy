package com.example.carpool_buddy;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VehicleInfoAdapter extends RecyclerView.Adapter<VehicleInfoViewHolder>{
    private ArrayList<String> ownerNames;
    private ArrayList<String> seatsLeft;
    private ArrayList<String> vehicleType;

    private ArrayList<Vehicle> vehicles;

    //constructor
    public VehicleInfoAdapter(ArrayList<String> names, ArrayList<String> seats, ArrayList<String> types, ArrayList<Vehicle> vehicles){
        this.ownerNames = names;
        this.seatsLeft = seats;
        this.vehicleType = types;
        this.vehicles = vehicles;
    }

    @NonNull
    @Override
    public VehicleInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_row_view, parent, false);

        //create ViewHolder object
        VehicleInfoViewHolder viewHolder = new VehicleInfoViewHolder(view);
        return viewHolder;
    }

    //int position is the index for the ArrayList
    @Override
    public void onBindViewHolder(@NonNull VehicleInfoViewHolder holder, int position) {
        holder.ownerNameTextView.setText(ownerNames.get(position));
        holder.seatsLeftTextView.setText(seatsLeft.get(position));
        holder.vehicleTypeTextView.setText(vehicleType.get(position));

        Vehicle currentVehicle = vehicles.get(position);

        //task 9 on document, adding intent to ViewHolder which will take user to VehicleProfileActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), VehicleProfileActivity.class);
                //passing the Vehicle Object information to VehicleProfileActivity
                intent.putExtra("vehicleID", currentVehicle.getVehicleId());
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ownerNames.size();
    }
}
