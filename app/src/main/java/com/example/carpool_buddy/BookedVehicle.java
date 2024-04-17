package com.example.carpool_buddy;

import java.io.Serializable;
import java.util.ArrayList;

/** I made a separate class just for the Vehicle Objects that will show up in a User's bookedVehicles ArrayList
 * /This is because I encountered a problem when using regular vehicle
 * */
public class BookedVehicle  implements Serializable {
    //properties
    private String owner;
    private String ownerID;
    private String model;
    private String bookedVehicleID;
    private String vehicleType;
    private int bookingsCount;

    //empty constructor
    public BookedVehicle(){
        //public no-argument constructor needed for FireStore
    }

    //this constructor is used in User method userBookedVehicle()
    public BookedVehicle(Vehicle vehicle){
        this.owner = vehicle.getOwner();
        this.ownerID = vehicle.getOwnerID();
        this.model = vehicle.getModel();
        this.bookedVehicleID = vehicle.getVehicleId();
        this.vehicleType = vehicle.getVehicleType();
        bookingsCount = 1;
    }

    //This method counts the amount of bookings the user has of a particular vehicle
    //So users can see how many seats they've booked in the booked_vehicle_row_view.xml in the RecyclerView in activity_your_bookings.xml
    public int getBookingsCount() {
        return bookingsCount;
    }

    public void addedBooking(){
        bookingsCount++;
    }

    public void cancelledBooking(){
        bookingsCount--;
    }

    public String getBookedVehicleID() {
        return bookedVehicleID;
    }
    public String getOwner() {
        return owner;
    }

    public String getVehicleType(){return this.vehicleType;}

    public String getModel() {
        return model;
    }
}
