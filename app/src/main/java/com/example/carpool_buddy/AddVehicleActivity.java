package com.example.carpool_buddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.UUID;

public class AddVehicleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //properties
    private FirebaseFirestore firestoreRef;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private EditText maxCapEditText;
    private String vehicleType;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);
        //get the user
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        firestoreRef = FirebaseFirestore.getInstance();

        //getting the Spinner view for the vehicle type
        Spinner spinner = findViewById(R.id.vehicle_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.vehicles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        getUserInfo(list -> Log.d("SUCCESS", "got the user"));
    }

    private interface callBack{
        void onCallBack(User user);
    }
    //getting the user info and updating their owned vehicles using the users collection, find the document with the same user ID
    private void getUserInfo(callBack firestoreCallback){
        DocumentReference docRef = firestoreRef.collection("users").document(mUser.getUid());
        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        User user = documentSnapshot.toObject(User.class);
                        firestoreCallback.onCallBack(user);
                        this.user = user;
                        Log.d("Retrieve Data", user.getFirstName());
                        }
                    else {
                        Log.d("Retrieve Data", "Error getting user info: ", task.getException());
                    }
                });
    }

    public boolean formValid(){
        int maxCap = Integer.parseInt(maxCapEditText.getText().toString());
        //check that max capacity is an integer larger than 0
        if (maxCap > 0 && maxCap <= 100){
            return true;
        }
        else{
            Toast.makeText(AddVehicleActivity.this, "Max capacity must be a valid integer", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void addNewVehicle(View v){
        //getting the EditText views for max cap input, model input, base price input
        maxCapEditText = findViewById(R.id.input_max_capacity);
        EditText modelEditText = findViewById(R.id.input_model);
        EditText basePriceEditText = findViewById(R.id.input_base_price);

        String maxCap = maxCapEditText.getText().toString();
        String model = modelEditText.getText().toString();
        String basePrice = basePriceEditText.getText().toString();

        if (TextUtils.isEmpty(maxCap)) {
            maxCapEditText.setError("Max capacity is required");
            return;
        }
        if (TextUtils.isEmpty(model)) {
            modelEditText.setError("Vehicle model is required");
            return;
        }
        if (TextUtils.isEmpty(basePrice)) {
            basePriceEditText.setError("If no base price, enter 0.00");
            return;
        }
//Log.d("UPDATE USER", "User's owned vehicles successfully updated in database");
        if (formValid()){
            //order of arguments: String owner, String model, int capacity, String vehicleID, ArrayList<String> ridersUIDs, boolean open, String vehicleType, double basePrice
            //default arraylist riders when a new vehicle created is empty
            ArrayList<String> ridersUIDs = new ArrayList<>();
            //generate a new random vehicle ID
            String vehicleID = UUID.randomUUID().toString();
            //String owner is the name of the user who created this vehicle
            String owner = user.getFirstName() + " " + user.getLastName();
            String ownerID = user.getUserID();
            //default vehicle when created is open
            Vehicle vehicle = new Vehicle(owner, ownerID, model, Integer.parseInt(maxCap), vehicleID, ridersUIDs, true, this.vehicleType, Double.parseDouble(basePrice));
            System.out.println(vehicle.getVehicleId());
            System.out.println(vehicle.getOwner());

            //adding to firestore database
            firestoreRef.collection("vehicles").document(vehicleID).set(vehicle)
                    .addOnSuccessListener(new OnSuccessListener<Void>(){
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("ADD VEHICLE", "Document vehicle successfully added to database");
                    //added the vehicle to ArrayList of user's vehicles
                    user.userAddedVehicle(vehicle);
                    Log.d("ADD TO ARRAYLIST", "Vehicle added to user's arraylist of owned vehicle, but haven't updated the database yet");
                    //update the user's documentation
                    firestoreRef.collection("users").document(user.getUserID()).set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>(){
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("UPDATE USER", "Successfully updated database");
                                    Intent intent = new Intent(getApplicationContext(), VehiclesInfoActivity.class);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("UPDATE USER", "Failed to update user's owned vehicles list", e);
                                }
                            });
                    user.userAddedVehicle(vehicle);
                    Intent intent = new Intent(getApplicationContext(), VehiclesInfoActivity.class);
                    startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("ADD VEHICLE", "Failed to write document to database", e);
                    Toast.makeText(getApplicationContext(), "Add vehicle failed.", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public void selectImage(View v){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();
            ImageView imageView = findViewById(R.id.vehicleImageView);
            imageView.setImageURI(selectedImage);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //setting String vehicleType to whatever the user chooses the vehicle type is
        vehicleType = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), vehicleType + " selected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(parent.getContext(), "Please select an option", Toast.LENGTH_SHORT).show();
    }


    public void cancelAdd(View v){
        Intent intent = new Intent(AddVehicleActivity.this, VehiclesInfoActivity.class);
        startActivity(intent);
    }
}