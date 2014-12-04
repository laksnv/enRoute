package app.vlnvv.enRoute;

import java.io.Serializable;

public class Venue implements Serializable {

    private String name;
    private String address; //street address + City/State/ZIP
    Coordinates coordinates;
    private float rating;
    private double deviation;

    public Venue(String n, String a, Coordinates coordinates, float rate){

        this.setName(n);
        this.setAddress(a);
        this.setRating(rate);
        this.coordinates = coordinates;
    }



    public Venue() {
        //Constructor Stub
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //Getters and Setters
    public String getAddress() {
        return this.address;
    }

    public void setAddress(String Address) {
        this.address = Address;
    }

    public Coordinates getCoordinates() {
        return this.coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }


    public double getDeviation() {
        return deviation;
    }


    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }



}