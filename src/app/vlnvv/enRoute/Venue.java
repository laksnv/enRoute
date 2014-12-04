package app.vlnvv.enRoute;

import java.io.Serializable;

public class Venue implements Serializable, Comparable<Venue> {

    private String name;
    private String address; //street address + City/State/ZIP
    private Coordinates coordinates;
    private float rating;
    private double enRouteRating;
    private double deviation;
    private String img_url;

    public Venue(String n, String a, Coordinates coordinates, float rate){

        this.setName(n);
        this.setAddress(a);
        this.setRating(rate);
        this.coordinates = coordinates;
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

    public double getEnRouteRating() {
        return this.enRouteRating;
    }

    public void setEnRouteRating(double enRouteRating) {
        this.enRouteRating = enRouteRating;
    }


    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    @Override
    public int compareTo(Venue another) {

        if(another.getRating() - this.rating < 0) {
            return -1;
        }

        else if(another.getRating() - this.rating > 0) {
            return 1;
        }

        else {
            return 0;
        }
    }
}