package com.example.carpool_buddy;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class User{
    //properties
    private String userID;
    private String firstName;
    private String lastName;
    private String usersEmail;
    private String usersType;
    private ArrayList<Vehicle> ownedVehicles;

    //Instead of adding duplicate Vehicle objects every time a user books multiple seats,
    //the keys will store an integer value: count of bookings
    private ArrayList<BookedVehicle> bookedVehicles;

    //public empty no-argument constructor is needed for FireStore to work
    public User(){
    }

    public User(String firstName, String lastName, String email, String userType, String uid){
        this.firstName = firstName;
        this.lastName = lastName;
        this.usersEmail = email;
        this.usersType = userType;
        this.userID = uid;
        this.ownedVehicles = new ArrayList<>();
        this.bookedVehicles = new ArrayList<>();
    }

    public ArrayList<Vehicle> getOwnedVehicles() {
        return ownedVehicles;
    }

    public ArrayList<BookedVehicle> getBookedVehicles() {
        return bookedVehicles;
    }

    //This method adds the vehicle to user's list of owned vehicles.
    public Vehicle userAddedVehicle(Vehicle vehicle){
        ownedVehicles.add(vehicle);
        return vehicle;
    }

    //This method adds the vehicle to user's list of booked vehicles. multiple bookings are allowed
    public void userBookedVehicle(Vehicle vehicle){
        //checks if the user has already booked the same vehicle.
        if(hasUserBookedVehicleIndex(vehicle) >= 0){
            //if yes, then instead of creating a new BookedVehicle object
            //the bookingsCount property increases by one
            bookedVehicles.get(hasUserBookedVehicleIndex(vehicle)).addedBooking();
            Log.d("Added Book Count", "Bookings Count ++ success");
        }
        else{
            BookedVehicle bookedVehicle = new BookedVehicle(vehicle);
            bookedVehicles.add(bookedVehicle);
            Log.d("Added New Booking", "Bookings Count = 1");
        }
    }

    //returns the index of the found vehicle
    //returns -1 if not found
    public int hasUserBookedVehicleIndex(Vehicle vehicle){
        // if user hasn't booked any vehicle, and bookedVehicles list is empty,
        // obviously they didn't book the passed argument vehicle
        if (bookedVehicles.isEmpty()){
            return -1;
        }
        for (int i = 0; i < bookedVehicles.size(); i++){
            // If the BookedVehicle object and the Vehicle object have the SAME ID, then they are the same vehicle
            if (bookedVehicles.get(i).getBookedVehicleID().equals(vehicle.getVehicleId())){
                // returns the index of where the BookedVehicle object appears in the ArrayList bookedVehicles
                return i;
            }
        }
        //if vehicle doesn't appear in the list of bookedVehicles at all, then that means user has never booked this vehicle before
        return -1;
    }

    public boolean userCancelledVehicle(Vehicle vehicle){
        //if bookedVehicles doesn't contain any vehicles, AS SAFETY MEASURE, EXIT THE METHOD
        if (bookedVehicles.isEmpty()){
            return false;
        }
        for (BookedVehicle bookedVehicle : bookedVehicles){
            if (bookedVehicle.getBookedVehicleID().equals(vehicle.getVehicleId())){
                //using custom equals() method from Vehicle class
                Log.d("VEHICLE", "Vehicle exists in user's booking list");
                //the bookingsCount property decreases by one
                bookedVehicle.cancelledBooking();
                Log.d("BOOKING COUNT ", "After cancel, booking count = " + Integer.toString(bookedVehicle.getBookingsCount()));
                //this will delete only the FIRST occurrence of the vehicle
                //this way the enhanced for loop will be okay with handling deleted elements
                //also, this ensures the user doesn't cancel ALL their bookings
                if (bookedVehicle.getBookingsCount() == 0){
                    bookedVehicles.remove(bookedVehicle);
                    break;
                }
                break;
            }
        }
        return true;
    }

    //if the current user is a rider of a particular vehicle, not the owner
    //and the owner of that vehicle CLOSED the ride
    //then ALL bookings of that vehicle must be removed from the current user's bookedVehicles arraylist
    public void ownerOfBookedVehicleClosedRide(Vehicle vehicle){
        for (int i = 0; i < bookedVehicles.size(); i++){
            //loop through the current user's bookedVehicles list to find ALL COPIES of the vehicle
            if(bookedVehicles.get(i).equals(vehicle)){
                bookedVehicles.remove(i);
                //to ensure that when the vehicle gets removed there will be no errors in the iteration, index i must reduce by 1
                i--;
            }
        }
    }

    //if the current user IS the owner, and the current user closed their vehicle
    public void userClosedRide(Vehicle vehicle){
        //index will locate the vehicle in the owner's list of ownedVehicles
        int index = 0;
        for (int i = 0; i < ownedVehicles.size(); i++){
            //if vehicle at index i = vehicle passed as an argument to userClosedRide(), then found the index of the vehicle
            //note that this equals() method is a custom equals() method from the Vehicle class
            if (ownedVehicles.get(i).equals(vehicle)){
                index = i;
            }
        }
        //vehicleClosed() is a method of the Vehicle class, which will remove all riders, set the seats left to max capacity, and set isOpen to false
        this.ownedVehicles.get(index).vehicleClosed();
    }
    //if the current user IS the owner, and the current user re-opened their vehicle
    public void userOpenedRide(Vehicle vehicle){
        //index will locate the vehicle in the owner's list of ownedVehicles
        int index = 0;
        for (int i = 0; i < ownedVehicles.size(); i++){
            //if vehicle at index i = vehicle passed as an argument to userClosedRide(), then found the index of the vehicle
            //note that this equals() method is a custom equals() method from the Vehicle class
            if (ownedVehicles.get(i).equals(vehicle)){
                index = i;
            }
        }
        this.ownedVehicles.get(index).vehicleOpened();
    }

    //updates ownedVehicle's riderUIDs list, if a user books their ride
    public void updateOwnedVehiclesBookRide(Vehicle vehicle, String addedRiderUID){
        int index = 0;
        for (int i = 0; i < ownedVehicles.size(); i++){
            //if vehicle at index i = vehicle passed as an argument to updateOwnedVehicles, then found the index of the vehicle
            if (ownedVehicles.get(i).equals(vehicle)){
                index = i;
            }
        }
        this.ownedVehicles.get(index).bookedSeat(addedRiderUID);
    }

    //updates ownedVehicle's riderUIDs list, if a rider cancels their ride
    public void updateOwnedVehiclesCancelRide(Vehicle vehicle, String addedRiderUID){
        int index = 0;
        for (int i = 0; i < ownedVehicles.size(); i++){
            //if vehicle at index i = vehicle passed as an argument to updateOwnedVehicles, then found the index of the vehicle
            if (ownedVehicles.get(i).equals(vehicle)){
                index = i;
            }
        }
        this.ownedVehicles.get(index).cancelledSeat(addedRiderUID);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsersType() {
        return usersType;
    }

    public String getUserID() {
        return userID;
    }

    public String getUsersEmail() {
        return usersEmail;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String vehicleInfoToString(){
        String str = "";
        if (bookedVehicles.isEmpty()){
            return "empty";
        }
        for (BookedVehicle veh : bookedVehicles){
            str += veh.getBookedVehicleID() + " ";
        }
        return str;
    }
}
