package com.example.carpool_buddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class VehicleProfileActivity extends AppCompatActivity {
    //properties
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private Vehicle vehicleInfo;

    private String vehicleInfoID;

    private User user;

    //text views that need to be updated to show Vehicle information
    private TextView vehicleType;
    private TextView vehicleCapacity;
    private TextView vehicleModel;
    private TextView vehiclePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_profile);
        //BEFORE buttons are set up, set all buttons to invisible first
        //this won't create confusion
        //during testing I noticed that sometimes when the user isn't the owner, the "Close Ride" button still shows up briefly, before turning invisible
        //this is because the page loads before the database can retrieve information about the vehicle and the user
        //if I don't set all the buttons to invisible first, then by default the setContentView would load all the buttons at once
        Button bookButton = (Button) findViewById(R.id.bookRideButton);
        bookButton.setVisibility(View.INVISIBLE);
        Button cancelButton = (Button) findViewById(R.id.cancelRideButton);
        cancelButton.setVisibility(View.INVISIBLE);
        Button openButton = (Button) findViewById(R.id.openRideButton);
        openButton.setVisibility(View.INVISIBLE);
        Button closeButton = (Button) findViewById(R.id.closeRideButton);
        closeButton.setVisibility(View.INVISIBLE);
        Button fullButton = (Button) findViewById(R.id.bookRideButtonFULL);
        fullButton.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        database = FirebaseFirestore.getInstance();
        Log.d("PAGE LOADED", "successful");
        //from VehicleInfoAdapter, when user clicks on a holder (a row in RecycleView), the Vehicle object is passed to this activity
        Intent intent= getIntent();
        //using the vehicle ID that was passed from VehiclesInfoAdapter, or Owned
        vehicleInfoID = (String) intent.getSerializableExtra("vehicleID");

        //getUserInfo will happen only after getVehicleInfo is finished
        getVehicleInfo(vehicle -> Log.d("VEHICLE RETRIEVED", vehicle.getVehicleId()));

        Log.d("NO CRASH", "success!");
    }

    private interface callBack{
        void onCallBack(User user);
    }

    private interface vehicleCallBack{
        void onCallBack(Vehicle vehicle);
    }
    //getting the vehicle info
    private void getVehicleInfo(vehicleCallBack firestoreCallback){
        DocumentReference docRef = database.collection("vehicles").document(this.vehicleInfoID);
        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        Vehicle vehicle = documentSnapshot.toObject(Vehicle.class);
                        firestoreCallback.onCallBack(vehicle);
                        if (vehicle == null){
                            Log.d("Vehicle Null", "Retrieved vehicle is null");
                        }
                        else{
                            this.vehicleInfo = vehicle;
                            Log.d("Got VEHICLE", vehicleInfo.getVehicleId());
                            vehicleType = (TextView) findViewById(R.id.vehicleTypeInfo);
                            Log.d("SET TEXT", vehicle.getVehicleType());
                            vehicleType.setText(vehicle.getVehicleType());
                            vehicleCapacity = (TextView) findViewById(R.id.vehicleCapacityInfo);
                            Log.d("SET TEXT", Integer.toString(vehicle.getCapacity()));
                            vehicleCapacity.setText(Integer.toString(vehicle.getCapacity()));
                            vehicleModel = (TextView) findViewById(R.id.vehicleModelInfo);
                            Log.d("SET TEXT", vehicle.getModel());
                            vehicleModel.setText(vehicle.getModel());
                            vehiclePrice = (TextView) findViewById(R.id.vehiclePriceInfo);
                            Log.d("SET TEXT", Double.toString(vehicle.getBasePrice()));
                            vehiclePrice.setText(Double.toString(vehicle.getBasePrice()));
                            //getUserInfo will both get the user information, and also set up the buttons
                            getUserInfo(user -> Log.d("USER RETRIEVED", user.getFirstName()));
                        }
                    }
                    else {
                        Log.d("Error Retrieve", "Error getting user info: ", task.getException());
                    }
                });
    }
    //getting the user info and updating their owned vehicles using the users collection, find the document with the same user ID
    private void getUserInfo(callBack firestoreCallback){
        DocumentReference docRef = database.collection("users").document(mUser.getUid());
        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        User user = documentSnapshot.toObject(User.class);
                        firestoreCallback.onCallBack(user);
                        if (user == null){
                            Log.d("Document Null", "Retrieved user is null");
                        }
                        else{
                            this.user = user;
                            Log.d("Got user from database", user.getFirstName());
                        }
                        //set up the buttons AFTER retrieving user info, because database is asynchronous
                        setUpButton();
                        Log.d("SetUp Button", "successfully set up Activity");
                    }
                    else {
                        Log.d("Retrieve Data", "Error getting user info: ", task.getException());
                    }
                });
    }

    // The method will remove all bookings of this vehicle from
    // all users' bookedVehicles list
    // when the owner closes down the ride
    private void updateRidersWhenRideClosed(callBack firestoreCallback, String riderUID, Vehicle vehicle){
        DocumentReference docRef = database.collection("users").document(riderUID);
        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        User rider = documentSnapshot.toObject(User.class);
                        firestoreCallback.onCallBack(rider);
                        if (rider == null){
                            Log.d("Document Null", "Retrieved user is null");
                        }
                        //if retrieved rider is not null, then there is no NullPointerException
                        else{
                            //ownerOfBookedVehicleClosedRide is a method of the User class
                            //check User.java to see more specific comments on what the method does
                            rider.ownerOfBookedVehicleClosedRide(vehicle);

                            //now update the users collection and the other riders of the vehicle
                            updateUser(rider);
                            Log.d("riderUIDs", "other riders vehicles updated as well");
                        }
                    }
                    else {
                        Log.d("TASK FAILURE", "Error getting rider info: ", task.getException());
                    }
                });
    }

    //riderUID is the user ID of the owner that will be updated
    private void updateOwnerVehicle(callBack firestoreCallback, String ownerUID, Vehicle vehicle, String addUserID, String action){
        DocumentReference docRef = database.collection("users").document(ownerUID);
        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        User owner = documentSnapshot.toObject(User.class);
                        firestoreCallback.onCallBack(owner);
                        if (owner == null){
                            Log.d("Document Null", "Retrieved owner is null");
                        }
                        //if retrieved rider is not null, then there is no NullPointerException
                        else{
                            switch(action) {
                                case "book":
                                    //updateOwnedVehiclesBookRide is a method of the User class, used specifically if the current user is owner of vehicle
                                    //the "another user" has ID of addUserID, which is also this.user.UID
                                    owner.updateOwnedVehiclesBookRide(vehicle, addUserID);
                                    break;
                                case "cancel":
                                    //updateOwnedVehiclesCancelRide is a method of the User class, used specifically if the current user is owner of vehicle
                                    owner.updateOwnedVehiclesCancelRide(vehicle, addUserID);
                                    break;
                            }

                            //now this will update the owner's user documentation in Firestore database
                            updateUser(owner);
                            Log.d("OWNER UPDATED", "The vehicle in owner's ownedVehicle list successfully added a new riderUID");
                        }
                    }
                    else {
                        Log.d("TASK FAILURE", "Error getting owner info: ", task.getException());
                    }
                });
    }

    public void setUpButton(){
        //if current mUser is the owner of the vehicle
        //then by default, the Close Ride button will show. This button will
        //change to Open Ride if the user clicks on the Close Ride
        if (mUser.getUid().equals(vehicleInfo.getOwnerID())){
            Button bookButton = (Button) findViewById(R.id.bookRideButton);
            bookButton.setVisibility(View.INVISIBLE);
            Button cancelButton = (Button) findViewById(R.id.cancelRideButton);
            cancelButton.setVisibility(View.INVISIBLE);

            //if vehicle is already open, then the owner should see only the "Close Ride" button
            if (vehicleInfo.isOpen()){
                Button closeButton = (Button) findViewById(R.id.closeRideButton);
                closeButton.setVisibility(View.VISIBLE);
            }
            //else, the vehicle is already closed, and the owner should see only the "Open Ride" button
            else{
                Button openButton = (Button) findViewById(R.id.openRideButton);
                openButton.setVisibility(View.VISIBLE);
            }
        }
        else{
            //else, the user isn't the owner of the vehicle
            Button openButton = (Button) findViewById(R.id.openRideButton);
            openButton.setVisibility(View.INVISIBLE);
            Button closeButton = (Button) findViewById(R.id.closeRideButton);
            closeButton.setVisibility(View.INVISIBLE);

            //if ride is already fully booked
            if (vehicleInfo.getSeatsLeft() <= 0){
                Log.d("RIDE FULL", "bookButton disabled");
                Button bookButton = (Button) findViewById(R.id.bookRideButton);
                bookButton.setVisibility(View.INVISIBLE);
                Button fullButton = (Button) findViewById(R.id.bookRideButtonFULL);
                fullButton.setVisibility(View.VISIBLE);
            }

            //if user has booked this vehicle at least once before
            //then the cancel ride button will show
            //Remember: if user has booked ride, hasUserBookedVehicle will return the index where the vehicle appears in their
            //bookedVehicles list
            if (user.hasUserBookedVehicleIndex(vehicleInfo) >= 0){
                Button bookButton = (Button) findViewById(R.id.bookRideButton);
                bookButton.setVisibility(View.VISIBLE);
                Button cancelButton = (Button) findViewById(R.id.cancelRideButton);
                cancelButton.setVisibility(View.VISIBLE);
            }
            //else, the cancel ride button won't show at all since user hasn't booked this vehicle before thus they can't cancel
            else{
                Button bookButton = (Button) findViewById(R.id.bookRideButton);
                bookButton.setVisibility(View.VISIBLE);
                Button cancelButton = (Button) findViewById(R.id.cancelRideButton);
                Log.d("INVISIBLE", "Invisible cancel ride button, user has not booked yet");
                cancelButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void bookRide(View v){
        //this will update the current vehicle's riderUIDs list
        vehicleInfo.bookedSeat(user.getUserID());
        //Since I've only changed the instance variables in the class, but I haven't changed the database variables,
        //then I need to update the database as well
        //this will update the database information
        updateVehicles(vehicleInfo);
        Log.d("SEATS LEFT", String.valueOf(vehicleInfo.getSeatsLeft()));

        //this will update the current user's ArrayList of bookedVehicles
        this.user.userBookedVehicle(vehicleInfo);
        Log.d("BOOKS LEFT", this.user.vehicleInfoToString());
        updateUser(this.user);

        //this will update the owned vehicle in the owner's document
        String ownerUID = vehicleInfo.getOwnerID();
        updateOwnerVehicle(new callBack() {
            @Override
            public void onCallBack(User user) {
                Log.d("OWNER", "got owner");
            }
        }, ownerUID, vehicleInfo, this.user.getUserID(),"book");


        Log.d("BOOK RIDE", "ride booked");

        //all backend modifications are done, now need to update the front end
        //by showing the cancel button or disabling the book ride button
        //if vehicle reached max capacity, cannot book anymore
        if (vehicleInfo.getSeatsLeft() <= 0){
            Log.d("RIDE FULL", "bookButton disabled");
            Button bookButton = (Button) findViewById(R.id.bookRideButton);
            bookButton.setVisibility(View.INVISIBLE);
            Button fullButton = (Button) findViewById(R.id.bookRideButtonFULL);
            fullButton.setVisibility(View.VISIBLE);
            fullButton.setEnabled(false);
        }
        Button cancelButton = (Button) findViewById(R.id.cancelRideButton);
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setEnabled(true);
    }

    public void cancelRide(View v){
        //Same as bookRide(), this will update the current vehicle's riderUIDs list
        vehicleInfo.cancelledSeat(user.getUserID());
        //Since I've only changed the instance variables in the class, but I haven't changed the database variables,
        //then I need to update the database as well
        //this will update the database information
        updateVehicles(vehicleInfo);

        //changing info about the current user's document in Firestore, the user who clicked the button
        //userCanceledVehicle is a method of the User class
        this.user.userCancelledVehicle(vehicleInfo);
        Log.d("BOOKS LEFT", this.user.vehicleInfoToString());
        updateUser(this.user);

        //this will update the owned vehicle in the owner's document
        String ownerUID = vehicleInfo.getOwnerID();
        updateOwnerVehicle(new callBack() {
            @Override
            public void onCallBack(User user) {
                Log.d("OWNER", "got owner");
            }
        }, ownerUID, vehicleInfo, this.user.getUserID(),"cancel");


        Log.d("CANCEL RIDE", "ride cancelled");

        //all backend modifications are done, now need to update the front end
        //by hiding the cancel button if there are no more bookings left
        Button bookButton = (Button) findViewById(R.id.bookRideButton);
        Button cancelButton = (Button) findViewById(R.id.cancelRideButton);
        Button fullButton = (Button) findViewById(R.id.bookRideButtonFULL);
        //in cancelRide(), the book button is no longer set to "FULL" and setEnabled is true
        //by cancelling a ride, users should be able to book again since there are seats available
        fullButton.setVisibility(View.INVISIBLE);
        bookButton.setVisibility(View.VISIBLE);
        //if user cancelled all their bookings of this vehicle, they can't cancel any more times
        //thus, the cancelButton will be invisible to them and also the user can't click on the cancelButton anymore
        if (user.hasUserBookedVehicleIndex(vehicleInfo) < 0){
            //if statement checks whether index = -1, which means vehicleInfo is not found in user's bookedVehicles list
            cancelButton.setVisibility(View.INVISIBLE);
            cancelButton.setEnabled(false);
        }
    }

    public void closeRide(View v){
        //This method will close the ride across all users, whether they're the owner or not
        //For the owner, this won't remove the vehicle from their ownedVehicles list, but this will remove the vehicle from any rider's bookedVehicles list

        //Changing info about the owner:
        //closedRide is a method of the User class that will remove the Vehicle object vehicleInfo from the owner's list of ownedVehicles
        Log.d("User",user.getFirstName());
        Log.d("Vehicle", vehicleInfo.getModel());
        //userClosedRide() is a method of the User class
        user.userClosedRide(vehicleInfo);
        //update the owner's user document in Firebase
        updateUser(user);

        //If there are any riders who booked this vehicle, then program must also update their Vehicle objects in their bookedVehicles ArrayList
        //changing info for the riders who booked the vehicle:
        if (!vehicleInfo.getRidersUIDs().isEmpty()){
            for (String riderUID : vehicleInfo.getRidersUIDs()){
                updateRidersWhenRideClosed(new callBack() {
                    @Override
                    public void onCallBack(User user) {
                        Log.d("USER", "got user");
                    }
                }, riderUID, vehicleInfo);
                //final parameter action takes in "cancel", which will tell the method that this user is CANCELLING a vehicle
            }
        }

        //in the vehicles document in Firestore, this will set the vehicle to closed
        vehicleInfo.vehicleClosed();
        updateVehicles(vehicleInfo);

        Log.d("CLOSE RIDE", "ride closed");
        Log.d("CHECK riderUIDs EMPTY", String.valueOf(vehicleInfo.getRidersUIDs().isEmpty()));

        Button closeButton = (Button) findViewById(R.id.closeRideButton);
        closeButton.setVisibility(View.INVISIBLE);
        Button openButton = (Button) findViewById(R.id.openRideButton);
        openButton.setVisibility(View.VISIBLE);
    }

    public void openRide(View v){
        //this will close the ride across all users, whether they're the owner or not
        //changing info about the owner:
        user.userOpenedRide(vehicleInfo);
        //update the owner's user document in Firebase
        updateUser(user);

        //NOTE: Unlike closeRide(), the riderUIDs don't need to be updated. This is because
        //when the owner closed the vehicle, the riders also got their rides cancelled and removed from bookedVehicles list
        //this means the vehicle currently has no riders, therefore it has no riders that need to be updated

        //in the vehicles document in Firestore, this will set the vehicle to opened
        vehicleInfo.vehicleOpened();
        updateVehicles(vehicleInfo);

        Log.d("OPEN RIDE", "ride opened");

        Button openButton = (Button) findViewById(R.id.openRideButton);
        openButton.setVisibility(View.INVISIBLE);
        Button closeButton = (Button) findViewById(R.id.closeRideButton);
        closeButton.setVisibility(View.VISIBLE);
    }

    //two helper methods to update the vehicle and user documents in firestore database collections
    private void updateVehicles(Vehicle updatedVehicle){
        String vehicleID = updatedVehicle.getVehicleId();
        database.collection("vehicles").document(vehicleID).set(updatedVehicle)
                .addOnSuccessListener(new OnSuccessListener<Void>(){
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("VEHICLE SEATS UPDATE", "Vehicle seats updated in database");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("ADD VEHICLE", "Failed to write document to database", e);
                        Toast.makeText(getApplicationContext(), "Failed to update vehicle's seats", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateUser(User updatedUser){
        String userID = updatedUser.getUserID();
        database.collection("users").document(userID).set(updatedUser)
                .addOnSuccessListener(new OnSuccessListener<Void>(){
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("UPDATE USER SUCCESS", "User document successfully updated in database");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("UPDATE USER FAILURE", "Failed to update update user document in database", e);
                        Toast.makeText(getApplicationContext(), "Failure, please check Internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}