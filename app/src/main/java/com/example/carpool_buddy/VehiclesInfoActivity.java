package com.example.carpool_buddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class VehiclesInfoActivity extends AppCompatActivity{
    //properties
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;

    //all vehicles from the database
    private ArrayList<Vehicle> vehiclesList = new ArrayList<>();

    //firestore database
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles_info);

        //populating the RecyclerView using data retrieved from firestore database
        database = FirebaseFirestore.getInstance();
        getAndPopulateData(list -> Log.d("SUCCESS", vehiclesList.toString()));
    }

    //fixing the asynchronous data loading
    //without this, the recyclerView loads before the data is retrieved from the database using db.collection.get()
    private interface callBack{
        void onCallBack(ArrayList<Vehicle> list);
    }

    private void getAndPopulateData(callBack firestoreCallback){
        database.collection("vehicles").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("Retrieve Data", document.getId() + " => " + document.getData());
                            Vehicle vehicle = document.toObject(Vehicle.class);
                            Log.d("Vehicle", vehicle.getModel());
                            //only if vehicle is open, then show the ride
                            if (vehicle.isOpen()){
                                vehiclesList.add(vehicle);
                            }

                            firestoreCallback.onCallBack(vehiclesList);

                            ArrayList<String> ownerNames = new ArrayList<>();
                            ArrayList<String> seatsLeft = new ArrayList<>();
                            ArrayList<String> vehicleTypes = new ArrayList<>();
                            ArrayList<Vehicle> vehicles = new ArrayList<>();
                            for (Vehicle veh : vehiclesList){
                                ownerNames.add(veh.getOwner());
                                seatsLeft.add(Integer.toString(veh.getSeatsLeft()));
                                vehicleTypes.add(veh.getVehicleType());
                                vehicles.add(veh);
                            }
                            //recyclerView shows all vehicles available
                            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                            VehicleInfoAdapter adapter = new VehicleInfoAdapter(ownerNames, seatsLeft, vehicleTypes, vehicles);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        }
                    } else {
                        Log.d("Retrieve Data", "Error getting documents: ", task.getException());
                    }
                });
    }

    //method is called when user clicks on "Add A Ride"
    public void goToAddVehicle(View v){
        Intent intent = new Intent(VehiclesInfoActivity.this, AddVehicleActivity.class);
        startActivity(intent);
    }

    public void refresh(View v){
        vehiclesList.clear();
        getAndPopulateData(list -> Log.d("SUCCESS", vehiclesList.toString()));
    }
}