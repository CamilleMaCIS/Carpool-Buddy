package com.example.carpool_buddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {
    //properties
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private TextView nameTextView;

    private FirebaseFirestore database;

    //behaviours
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        database = FirebaseFirestore.getInstance();
        nameTextView = (TextView) findViewById(R.id.userFirstName);
        getUserInfo(user -> Log.d("USER RETRIEVED", user.getFirstName()));
    }

    private interface callBack{
        void onCallBack(User user);
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
                        if (user != null){
                            //this will change "Hello, Name" to "Hello, Camille", making UserProfileActivity seem more customised for each individual user
                            nameTextView.setText(user.getFirstName());
                            Log.d("TEXT SET", user.getFirstName());
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
    public void signOut(View v){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(UserProfileActivity.this, AuthActivity.class));
        Toast.makeText(UserProfileActivity.this, "Logout successful", Toast.LENGTH_SHORT).show();
    }

    public void seeVehicles(View v){
        Intent intent = new Intent(UserProfileActivity.this, VehiclesInfoActivity.class);
        startActivity(intent);
    }

    public void seeBookedVehicles(View v){
        Intent intent = new Intent(UserProfileActivity.this, YourBookingsActivity.class);
        startActivity(intent);
    }
    public void seeOwnedVehicles(View v){
        Intent intent = new Intent(UserProfileActivity.this, YourVehiclesActivity.class);
        startActivity(intent);
    }
}