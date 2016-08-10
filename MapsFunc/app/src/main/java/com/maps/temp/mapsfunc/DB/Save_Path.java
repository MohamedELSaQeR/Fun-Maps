package com.maps.temp.mapsfunc.DB;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Sam on 5/14/2016.
 */
public class Save_Path extends SugarRecord {
    public String getmPoint() {
        return mPoint;
    }

    public void setmPoint(String mPoint) {
        this.mPoint = mPoint;
    }

    String mPoint;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    String name;

    public Save_Path() {

    }
    public Save_Path(String name ,String mPoint) {
        this.name=name;
        this.mPoint=mPoint;

    }


}
