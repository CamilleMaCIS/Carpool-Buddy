package com.example.carpool_buddy;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OwnedVehicleAdapter extends RecyclerView.Adapter<OwnedVehicleViewHolder>{
    private ArrayList<String> models;
    private ArrayList<String> isOpen;
    private ArrayList<Integer> seatsLeft;

    private ArrayList<String> vehicleTypes;
    private ArrayList<Vehicle> vehicles;

    //constructor
    public OwnedVehicleAdapter(ArrayList<String> models, ArrayList<String> isOpen, ArrayList<Integer> seatsLeft, ArrayList<String> vehicleTypes, ArrayList<Vehicle> vehicles){
        this.models = models;
        this.isOpen = isOpen;
        this.seatsLeft = seatsLeft;
        this.vehicleTypes = vehicleTypes;
        this.vehicles = vehicles;
    }

    @NonNull
    @Override
    public OwnedVehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.owned_vehicle_row_view, parent, false);

        //create ViewHolder object
        OwnedVehicleViewHolder viewHolder = new OwnedVehicleViewHolder(view);
        return viewHolder;
    }

    //int position is the index for the ArrayList
    @Override
    public void onBindViewHolder(@NonNull OwnedVehicleViewHolder holder, int position) {
        //sets the text for the vehicle model based on vehicle info
        holder.modelsTextView.setText(models.get(position));
        //sets the text for vehicle open/close status based on vehicle info
        holder.isOpensTextView.setText(isOpen.get(position));
        //sets the text for vehicle type based on vehicle info, e.g. could be "Car", "Electric Car", "Bicycle", etc
        holder.vehTypeTextView.setText(vehicleTypes.get(position));
        //sets the text for seats left based on vehicle info
        //Note that seatsLeft is holds Integer elements. setText() only takes in Strings, so must convert the Integer types to String type
        holder.seatsNumTextView.setText(String.valueOf(seatsLeft.get(position)));
        Log.d("HOLDER IS SET", isOpen.get(position).toString());

        //this will be used to pass the Vehicle object and it's information to VehicleProfileActivity
        Vehicle currentVehicle = vehicles.get(position);

        //task 9 on document, adding intent to ViewHolder which will take user to VehicleProfileActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), VehicleProfileActivity.class);
                //passing the vehicle Object information to VehicleProfileActivity
                intent.putExtra("vehicleID", currentVehicle.getVehicleId());
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }
}
