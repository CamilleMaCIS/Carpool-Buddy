package com.example.carpool_buddy;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public class Vehicle implements Serializable {
    //properties
    private String owner;
    private String ownerID;
    private String model;
    private int capacity;
    private int seatsLeft;
    private String vehicleId;
    private ArrayList<String> ridersUIDs;
    private boolean open;
    private String vehicleType;
    private double basePrice;

    //empty constructor
    public Vehicle(){
        //public no-argument constructor needed for FireStore
    }
    //constructor with all properties
    public Vehicle(String owner, String ownerID, String model, int capacity, String vehicleId, ArrayList<String> ridersUIDs, boolean open, String vehicleType, double basePrice){
        this.basePrice = basePrice;
        this.capacity = capacity;
        //seats left will reduce by 1 if someone books a seat
        //seats left will increase by 1 if they cancel their seat
        this.seatsLeft = capacity;
        this.model = model;
        this.open = open;
        this.owner = owner;
        this.ownerID = ownerID;
        this.ridersUIDs = ridersUIDs;
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
    }
    //getters
    public double getBasePrice() {
        return basePrice;
    }
    public int getCapacity() {
        return capacity;
    }
    public int bookedSeat(String userID){
        if (ridersUIDs != null){
            ridersUIDs.add(userID);
            Log.d("RidersUID", "ArrayList ridersUIDs updated");
        }
        else{
            Log.w("RidersUID", "Failed to update ArrayList ridersUIDs");
        }
        //since a user has booked a seat, then the seats left will be reduced by one
        seatsLeft = seatsLeft - 1;
        //check that seatsLeft have decreased by 1, and never becomes negative (can't have negative seats left)
        Log.d("SEATS LEFT", String.valueOf(seatsLeft));
        return seatsLeft;
    }
    public int cancelledSeat(String userID){
        if (ridersUIDs != null){
            ridersUIDs.remove(userID);
            Log.d("RidersUID", "ArrayList ridersUIDs updated");
        }
        else{
            Log.w("RidersUID", "Failed to update ArrayList ridersUIDs");
        }
        //since a user has cancelled a seat, then the seats left will increase by one
        seatsLeft = seatsLeft + 1;
        //check that seatsLeft never goes beyond the max capacity, a.k.a. user's CANNOT cancel more seats than they have booked
        Log.d("SEATS LEFT", String.valueOf(seatsLeft));
        return seatsLeft;
    }
    public String getModel() {
        return model;
    }
    public boolean isOpen() {
        return open;
    }

    public void vehicleOpened(){
        this.open = true;
    }
    public void vehicleClosed(){
        this.open = false;
        //when vehicle is closed, this Vehicle is no longer open to any booked users
        //therefore the ridersUID will be cleared if any riders existed, and there will be no riders left
        //check that ridersUIDs isn't empty first before clearing it
        if (!ridersUIDs.isEmpty()){
            this.ridersUIDs.clear();
        }
        //since there are no more riders, seats left will return to max capacity
        this.seatsLeft = this.capacity;
    }

    public String getOwner() {
        return owner;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public int getSeatsLeft() {
        return seatsLeft;
    }

    public ArrayList<String> getRidersUIDs() {
        return ridersUIDs;
    }
    public String getVehicleId() {
        return vehicleId;
    }
    public String getVehicleType() {
        return vehicleType;
    }

    public boolean equals(Vehicle vehicle2) {
        //if two vehicles have the same vehicle ID and owner
        return (this.vehicleId.equals(vehicle2.getVehicleId()) && this.ownerID.equals(vehicle2.getOwnerID()));
    }
}
