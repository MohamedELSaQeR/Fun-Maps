package com.maps.temp.mapsfunc.Model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Sam on 5/2/2016.
 */
public class Mplace {
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getCoordin() {
        return coordin;
    }

    public void setCoordin(LatLng coordin) {
        this.coordin = coordin;
    }

    String name;
    String icon;
    LatLng coordin;
}
