package com.example.carpool_buddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //properties
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private Spinner sUserType;
    private EditText enterEmail;
    private EditText enterPassword;
    private EditText enterFirstName;
    private EditText enterLastName;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        enterEmail = findViewById(R.id.editTextEmailAddress);
        enterPassword = findViewById(R.id.editTextPassword);
        enterFirstName = findViewById(R.id.editFirstName);
        enterLastName = findViewById(R.id.editLastName);

        //getting the Spinner view for the role the user chooses
        Spinner spinner = findViewById(R.id.spinner_role);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void signUp(View v){
        String email = enterEmail.getText().toString();
        String password = enterPassword.getText().toString();
        String firstName = enterFirstName.getText().toString();
        String lastName = enterLastName.getText().toString();
        if (TextUtils.isEmpty(email)) {
            enterEmail.setError("Email is required");
            return;
        }
        if (!email.contains("@")) {
            enterEmail.setError("Invalid email");
            return;
        }
        if (!email.contains("cis")) {
            enterEmail.setError("CIS emails only");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            enterPassword.setError("Password is required");
            return;
        }
        if (TextUtils.isEmpty(firstName)) {
            enterFirstName.setError("First name is required");
            return;
        }
        if (TextUtils.isEmpty(lastName)) {
            enterLastName.setError("Last name is required");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task){
                //if call to database is successful
                if (task.isSuccessful()){
                    Toast.makeText(SignUpActivity.this, "Account created.", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    assert user != null;
                    String uid = user.getUid();
                    //creates new document for new user info
                    // User(String firstName, String lastName, String email, String userType, String uid)
                    User newUser = new User(firstName, lastName, email, userType, uid);
                    //adding the user info to firestore database
                    firestore.collection("users").document(uid).set(newUser)
                            .addOnSuccessListener(new OnSuccessListener<Void>(){
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("ADD USER", "User name is: " + newUser.getFirstName() + " " + newUser.getLastName() + ". User email is: " + newUser.getUsersEmail() + ". User type is: " + newUser.getUsersType() + ". User's ID is: " + newUser.getUserID());
                                    Intent intent = new Intent(getApplicationContext(), VehiclesInfoActivity.class);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("ADD USER", "Failed to add new user document to database", e);
                                    Toast.makeText(getApplicationContext(), "Cannot connect to the database.", Toast.LENGTH_SHORT).show();
                                }
                            });

                    Log.d("SIGN UP", "Successfully signed up a new user");
                    Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                    startActivity(intent);
                }
                //if call to database fails
                else{
                    Log.w("SIGN UP", "Sign up failed", task.getException());
                    Toast.makeText(getApplicationContext(), "Sign Up failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void backToLogin(View v){
        Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //setting String userType to whatever the user chooses their role is
        userType = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), userType + " selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(parent.getContext(), "Please select an option", Toast.LENGTH_SHORT).show();
    }
}