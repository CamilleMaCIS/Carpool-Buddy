package com.example.carpool_buddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class YourVehiclesActivity extends AppCompatActivity {
    //properties
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private FirebaseFirestore database;

    private ArrayList<Vehicle> ownedVehicles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_vehicles);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        database = FirebaseFirestore.getInstance();
        getAndPopulateData(user -> Log.d("USER RETRIEVED", user.getFirstName()));
    }

    private interface callBack{
        void onCallBack(User user);
    }
    //getting the user info and updating their owned vehicles using the users collection, find the document with the same user ID
    private void getAndPopulateData(callBack firestoreCallback){
        DocumentReference docRef = database.collection("users").document(mUser.getUid());
        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        User user = documentSnapshot.toObject(User.class);
                        firestoreCallback.onCallBack(user);
                        Log.d("CALLBACK", "Got user");
                        if (user != null){
                            Log.d("GOT USER VEHICLES", user.getOwnedVehicles().toString());
                            this.ownedVehicles = user.getOwnedVehicles();

                            ArrayList<String> models = new ArrayList<>();
                            ArrayList<String> isOpen = new ArrayList<>();
                            ArrayList<String> vehicleTypes = new ArrayList<>();
                            ArrayList<Integer> seatsLeft = new ArrayList<>();
                            for (Vehicle veh : ownedVehicles){
                                models.add(veh.getModel());
                                seatsLeft.add(veh.getSeatsLeft());
                                vehicleTypes.add(veh.getVehicleType());
                                Log.d("OPEN STATUS", veh.isOpen() + "");
                                if (veh.isOpen()){
                                    isOpen.add("Open");
                                }
                                else{
                                    isOpen.add("Closed");
                                }
                            }
                            Log.d("MODELS", models.isEmpty() + "");

                            //ownedRecyclerView shows all vehicles the current user owns
                            RecyclerView ownedRecyclerView = (RecyclerView) findViewById(R.id.ownedVehiclesRecycler);
                            //constructor in OwnedVehicleAdapter is:  OwnedVehicleAdapter(ArrayList<String> models, ArrayList<String> isOpen, ArrayList<Integer> seatsLeft, ArrayList<String> vehicleTypes, ArrayList<Vehicle> vehicles)
                            //models is ArrayList of the Vehicle model attribute, isOpen shows the Vehicle open/closed attribute so the owner can re-open the vehicles, seatsLeft is ArrayList of the Vehicle seatsLeft attribute
                            OwnedVehicleAdapter ownedAdapter = new OwnedVehicleAdapter(models, isOpen, seatsLeft, vehicleTypes, ownedVehicles);
                            ownedRecyclerView.setAdapter(ownedAdapter);
                            ownedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                        }
                        else{
                            Log.d("Document Null", "Retrieved user is null");
                        }
                    }
                    else {
                        Log.d("Retrieve Data", "Error getting user info: ", task.getException());
                    }
                });
    }

    //this method will be used if the owner added a new Vehicle, and it doesn't show up in RecyclerView because
    //the RecyclerView hasn't been updated with new information from the database yet
    //this will clear the current RecyclerView and reset it with the updated ownedVehicles ArrayList
    public void refresh(View v){
        ownedVehicles.clear();
        getAndPopulateData(list -> Log.d("SUCCESS", ownedVehicles.toString()));
    }

    //when user clicks on "Add A Ride"
    public void goToAddVehicle(View v){
        Intent intent = new Intent(YourVehiclesActivity.this, AddVehicleActivity.class);
        startActivity(intent);
    }
}