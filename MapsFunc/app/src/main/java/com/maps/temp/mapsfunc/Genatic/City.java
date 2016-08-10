package com.maps.temp.mapsfunc.Genatic;

import android.location.Location;

/**
 * Created by Sam on 5/14/2016.
 */
public class City {
    double x;
    double y;

    // Constructs a randomly placed city


    // Constructs a city at chosen x, y location
    public City(double x, double y){
        this.x = x;
        this.y = y;
    }

    // Gets city's x coordinate
    public double getX(){
        return this.x;
    }

    // Gets city's y coordinate
    public double getY(){
        return this.y;
    }

    // Gets the distance to given city
    public double distanceTo(City city){
//        double xDistance = Math.abs(getX() - city.getX());
//        double yDistance = Math.abs(getY() - city.getY());
//        double distance = Math.sqrt( (xDistance*xDistance) + (yDistance*yDistance) );
        Location loc1 = new Location("");
        loc1.setLatitude(getX());
        loc1.setLongitude(getX());

        Location loc2 = new Location("");
        loc2.setLatitude(city.getX());
        loc2.setLongitude(city.getY());

        double distanceInMeters = loc1.distanceTo(loc2);

        return distanceInMeters;
    }

    @Override
    public String toString(){
        return getX()+", "+getY();
    }
}