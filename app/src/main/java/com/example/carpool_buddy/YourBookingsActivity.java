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

public class YourBookingsActivity extends AppCompatActivity {
    //properties
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private FirebaseFirestore database;

    private ArrayList<BookedVehicle> bookedVehicles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_bookings);

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
                            Log.d("GOT USER VEHICLES", user.getBookedVehicles().toString());
                            this.bookedVehicles = user.getBookedVehicles();

                            ArrayList<String> owners = new ArrayList<>();
                            ArrayList<String> models = new ArrayList<>();
                            ArrayList<Integer> bookingCounts = new ArrayList<>();
                            ArrayList<String> vehicleTypes = new ArrayList<>();
                            ArrayList<String> vehicleIDs = new ArrayList<>();
                            for (BookedVehicle bookedVeh : bookedVehicles){
                                owners.add(bookedVeh.getOwner());
                                models.add(bookedVeh.getModel());
                                bookingCounts.add(bookedVeh.getBookingsCount());
                                vehicleTypes.add(bookedVeh.getVehicleType());
                                vehicleIDs.add(bookedVeh.getBookedVehicleID());
                            }
                            Log.d("OWNERS", owners.toString());
                            Log.d("MODELS", models.toString());
                            Log.d("BOOKINGCOUNTS", bookingCounts.toString());
                            Log.d("TYPES", vehicleTypes.toString());
                            Log.d("IDs", vehicleIDs.toString());

                            //ownedRecyclerView shows all vehicles the current user owns
                            RecyclerView bookedRecyclerView = (RecyclerView) findViewById(R.id.bookedVehiclesRecycler);
                            //constructor in BookedVehicleAdapter is:  BookedVehicleAdapter(ArrayList<String> owners, ArrayList<String> models, ArrayList<Integer> bookingCounts, ArrayList<String> vehicleTypes, ArrayList<String> vehicleIDs)
                            //owners is ArrayList of all BookedVehicle vehicle owners, models is ArrayList of the BookedVehicle model attribute,
                            BookedVehicleAdapter bookedAdapter = new BookedVehicleAdapter(owners, models, bookingCounts, vehicleTypes, vehicleIDs);
                            bookedRecyclerView.setAdapter(bookedAdapter);
                            bookedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        bookedVehicles.clear();
        getAndPopulateData(list -> Log.d("SUCCESS", bookedVehicles.toString()));
    }
}