package com.maps.temp.mapsfunc;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.maps.temp.mapsfunc.IsNear.IsNearActivity;
import com.maps.temp.mapsfunc.Rubish.PlaceFragmentMap;
import com.maps.temp.mapsfunc.GenaticPointRoad.MapPointsActivity;

public class MainActivity extends AppCompatActivity {

    private static final int PLACE_PICKER_REQUEST = 1;
    private TextView mName;
    private TextView mAddress;
    private TextView mAttributions;
    Button pickerButton;
    Button shortest;
    Button points;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(26.7745297, -26.2994344), new LatLng(37.430610, -121.972090));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mName = (TextView) findViewById(R.id.textView);
        mAddress = (TextView) findViewById(R.id.textView2);
        mAttributions = (TextView) findViewById(R.id.textView3);
        pickerButton = (Button) findViewById(R.id.pickerButton);
        shortest = (Button) findViewById(R.id.button);
        points = (Button) findViewById(R.id.button2);
        points.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent x = new Intent(MainActivity.this, MapPointsActivity.class);
                startActivity(x);


            }
        });
        shortest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, IsNearActivity.class);
                startActivity(i);
            }
        });
        pickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder intentBuilder =
                            new PlacePicker.IntentBuilder();
                    intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
                    Intent intent = intentBuilder.build(MainActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
//            final CharSequence name = place.getName();
//            final CharSequence address = place.getAddress();
//            String attributions = (String) place.getAttributions();
////            String geoUri = "http://maps.google.com/maps?q=loc:" + place.getLatLng().latitude + "," + place.getLatLng().longitude + " (" + name + ")";
////            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
//            startActivity(intent);
            FragmentManager fm = getFragmentManager();

// add
            FragmentTransaction ft = fm.beginTransaction();
            PlaceFragmentMap frg = new PlaceFragmentMap();
            Bundle extra = new Bundle();
            extra.putDouble("Lat", place.getLatLng().latitude);
            extra.putDouble("Lan", place.getLatLng().longitude);
            ft.add(R.id.container, frg).addToBackStack("tag");
            ft.commit();

            frg.setArguments(extra);

//            mName.setText(name);
//            mAddress.setText(address);
//            mAttributions.setText(Html.fromHtml(attributions));

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();

        } else {
            getFragmentManager().popBackStack();

        }

    }
}
