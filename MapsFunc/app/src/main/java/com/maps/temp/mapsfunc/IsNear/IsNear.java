package com.maps.temp.mapsfunc.IsNear;

import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.maps.temp.mapsfunc.Model.Mplace;
import com.maps.temp.mapsfunc.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 4/9/2016.
 */
public class IsNear extends MapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {
    private GoogleApiClient mGoogleApiClient;

    private Location mCurrentLocation;
    EditText type1, type2, radius;
    CheckBox notNearby;

    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};
    private int curMapTypeIndex = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        setHasOptionsMenu(true);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        initListeners();
        LinearLayout linearLayout = new LinearLayout(getActivity());
        // Set the layout full width, full height
        ViewGroup.LayoutParams params1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(params1);
        linearLayout.setOrientation(LinearLayout.VERTICAL); //or VERTICAL
        type1 = new EditText(getActivity());
        type2 = new EditText(getActivity());
        radius = new EditText(getActivity());
        notNearby = new CheckBox(getActivity());
        //For buttons visibility, you must set the layout params in order to give some width and height:
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        type1.setLayoutParams(params);
        notNearby.setLayoutParams(params);
        radius.setLayoutParams(params);
        type2.setLayoutParams(params);
        notNearby.setText("Not Near by");
        type1.setHint("Enter first type");
        type2.setHint("Enter second type");
        radius.setHint("Enter desired radius in meters");
        ViewGroup viewGroup = (ViewGroup) view;


        linearLayout.addView(type1);
        linearLayout.addView(notNearby);
        linearLayout.addView(type2);
        linearLayout.addView(radius);
        viewGroup.addView(linearLayout);
    }

    private void initListeners() {
        getMap().setOnMarkerClickListener(this);
        getMap().setOnMapLongClickListener(this);
        getMap().setOnInfoWindowClickListener(this);
        getMap().setOnMapClickListener(this);
    }

    private void removeListeners() {
        if (getMap() != null) {
            getMap().setOnMarkerClickListener(null);
            getMap().setOnMapLongClickListener(null);
            getMap().setOnInfoWindowClickListener(null);
            getMap().setOnMapClickListener(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    private void initCamera(Location location) {
        CameraPosition position = CameraPosition.builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(position), null);

        getMap().setMapType(MAP_TYPES[curMapTypeIndex]);
        getMap().setTrafficEnabled(true);
        getMap().setMyLocationEnabled(true);
        getMap().getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation != null) {
            initCamera(mCurrentLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //handle play services disconnecting if location is being constantly used
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Create a default location if the Google API Client fails. Placing location at Googleplex
        mCurrentLocation = new Location("");
        mCurrentLocation.setLatitude(37.422535);
        mCurrentLocation.setLongitude(-122.084804);
        initCamera(mCurrentLocation);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(getActivity(), "Clicked on marker", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions options = new MarkerOptions().position(latLng);
        options.title(getAddressFromLatLng(latLng));
        options.icon(BitmapDescriptorFactory.defaultMarker());
        getMap().addMarker(options);
//        returnFirstType("hospital", latLng, 1000);
        if (type1.getText().toString().equals("") && type2.getText().toString().equals("") && radius.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_LONG).show();
        } else if (type2.getText().toString().equals("") && !type1.getText().toString().equals("") && !radius.getText().toString().equals("")) {
            returnFirstType(type1.getText().toString(), latLng, Integer.parseInt(radius.getText().toString()));
        } else {
            returnExexpttype(type1.getText().toString(),type2.getText().toString(),latLng, Integer.parseInt(radius.getText().toString()));
        }


    }


    public void returnFirstType(String type, final LatLng maLocation, int radius) {

        RequestQueue queue = Volley.newRequestQueue(getContext());
        final List<Mplace> mplaceList = new ArrayList<>();
        String url = "https://maps.googleapis.com/maps/api/place/search/json" +
                "?location=" + maLocation.latitude + "," + maLocation.longitude + "&radius=" + radius + "&types=" + type +
                "&key=AIzaSyCmSePInAc1iK9K0DZ6RqP3tbCe11u5GHc";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        List<Mplace> tempList = new ArrayList<>();

                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                Mplace mplace = new Mplace();
                                LatLng mCoordin;
                                JSONObject place = results.getJSONObject(i);
                                mplace.setName(place.getString("name"));
                                mplace.setIcon(place.getString("icon"));
                                JSONObject geometry = place.getJSONObject("geometry");
                                JSONObject coordin = geometry.getJSONObject("location");
                                mCoordin = new LatLng(coordin.getDouble("lat"), coordin.getDouble("lng"));
                                mplace.setCoordin(mCoordin);
                                tempList.add(mplace);

                            }
                            for (int i = 0; i < tempList.size(); i++) {
                                Mplace sPlace = tempList.get(i);
                                MarkerOptions moptions = new MarkerOptions().position(sPlace.getCoordin());
                                moptions.title(sPlace.getName() + "/n" + getAddressFromLatLng(sPlace.getCoordin()));
                                moptions.icon(BitmapDescriptorFactory.defaultMarker());
                                getMap().addMarker(moptions);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

// add it to the RequestQueue
        queue.add(getRequest);
    }

    public void returnExexpttype(final String type1, final String type2, final LatLng maLocation, final int radius) {

        final RequestQueue queue = Volley.newRequestQueue(getContext());
        final String url = "https://maps.googleapis.com/maps/api/place/search/json" +
                "?location=" + maLocation.latitude + "," + maLocation.longitude + "&radius=" + radius + "&types=" + type1 +
                "&key=AIzaSyCmSePInAc1iK9K0DZ6RqP3tbCe11u5GHc";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        final List<Mplace> tempList = new ArrayList<>();
                        final List<Mplace> nearList = new ArrayList<>();


                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                Mplace mplace = new Mplace();
                                LatLng mCoordin;
                                JSONObject place = results.getJSONObject(i);
                                mplace.setName(place.getString("name"));
                                mplace.setIcon(place.getString("icon"));
                                JSONObject geometry = place.getJSONObject("geometry");
                                JSONObject coordin = geometry.getJSONObject("location");
                                mCoordin = new LatLng(coordin.getDouble("lat"), coordin.getDouble("lng"));
                                mplace.setCoordin(mCoordin);
                                tempList.add(mplace);

                            }

                            for (int i = 0; i < tempList.size(); i++) {
                                if (i>tempList.size()){
                                    break;
                                }
                                final Mplace sPlace = tempList.get(i);
                                String url2 = "https://maps.googleapis.com/maps/api/place/search/json" +
                                        "?location=" + sPlace.getCoordin().latitude + "," + sPlace.getCoordin().longitude + "&radius=" + 200 + "&types=" + type2 +
                                        "&key=AIzaSyCmSePInAc1iK9K0DZ6RqP3tbCe11u5GHc";
                                final int finalI = i;
                                final JsonObjectRequest getRequesta = new JsonObjectRequest(Request.Method.GET, url2, null, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            if (notNearby.isChecked()) {
                                                if (!response.getString("status").equals("ZERO_RESULTS")) {
                                                    tempList.remove(sPlace);

                                                }
                                            } else {
                                                if (!response.getString("status").equals("ZERO_RESULTS")) {
                                                    nearList.add(tempList.get(finalI));
                                                }

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                    }
                                });

                                queue.add(getRequesta);
                            }
                            if (notNearby.isChecked()){
                            Toast.makeText(getActivity(), "Drawing " + type1 + "which isn't nearby" + type2, Toast.LENGTH_LONG).show();
                            for (int i = 0; i < tempList.size(); i++) {
                                Mplace xPlace = tempList.get(i);
                                MarkerOptions moptions = new MarkerOptions().position(xPlace.getCoordin());
                                moptions.title(xPlace.getName() + "/n" + getAddressFromLatLng(xPlace.getCoordin()));
                                moptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
                                getMap().addMarker(moptions);
                            }}
                            else {
                                if (nearList.size()==0){
                                    Toast.makeText(getActivity(), "No " + type1 + " nearby " + type2, Toast.LENGTH_LONG).show();
                                }else {
                                Toast.makeText(getActivity(), "Drawing " + type1 + "which is nearby " + type2, Toast.LENGTH_LONG).show();
                                for (int i = 0; i < nearList.size(); i++) {
                                    Mplace xPlace = tempList.get(i);
                                    MarkerOptions moptions = new MarkerOptions().position(xPlace.getCoordin());
                                    moptions.title(xPlace.getName() + "/n" + getAddressFromLatLng(xPlace.getCoordin()));
                                    moptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
                                    getMap().addMarker(moptions);
                                }
                            }}


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );


// add it to the RequestQueue
        queue.add(getRequest);
    }

    private void requestDirection(final LatLng from, final LatLng to) {
        Toast.makeText(getActivity(), "Direction Requesting...", Toast.LENGTH_SHORT).show();
        GoogleDirection.withServerKey("AIzaSyCmSePInAc1iK9K0DZ6RqP3tbCe11u5GHc")
                .from(from)
                .to(to)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        Toast.makeText(getActivity(), "Success with status :" + direction.getStatus(), Toast.LENGTH_SHORT).show();
                        if (direction.isOK()) {
                            getMap().addMarker(new MarkerOptions().position(from));
                            getMap().addMarker(new MarkerOptions().position(to));

                            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                            getMap().addPolyline(DirectionConverter.createPolyline(getActivity(), directionPositionList, 5, Color.RED));


                        } else {
                            Log.d("APIError", rawBody);

                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getActivity());

        String address = "";
        try {
            address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0).getAddressLine(0);
        } catch (IOException e) {
        }

        return address;
    }

}
