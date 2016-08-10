package com.maps.temp.mapsfunc.GenaticPointRoad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
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
import com.maps.temp.mapsfunc.DB.Save_Path;
import com.maps.temp.mapsfunc.Genatic.City;
import com.maps.temp.mapsfunc.Genatic.GA;
import com.maps.temp.mapsfunc.Genatic.Population;
import com.maps.temp.mapsfunc.Genatic.Tour;
import com.maps.temp.mapsfunc.Genatic.TourManager;
import com.maps.temp.mapsfunc.R;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Sam on 4/16/2016.
 */
public class MapPoints extends MapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,

        GoogleMap.OnMarkerClickListener {
    Button result, clear, save,share;
    private GoogleApiClient mGoogleApiClient;
    public static final String MyPREFERENCES = "MyPrefs";
    ArrayList<String> spinnerArray;
    ArrayAdapter<String> spinnerArrayAdapter;
    private Location mCurrentLocation;
    List<Save_Path> allPathes;
    List<LatLng> mPoints = new ArrayList<>();

    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};
    private int curMapTypeIndex = 1;
    SharedPreferences sharedpreferences;

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share_item){
            Random random = new Random();
            String randoma = generateString(random,"abcdefghijklmnopqrst",10);
            Bitmap bm = screenShot(this.getView());
            File file = saveBitmap(bm, randoma+".png");
            Log.i("chase", "filepath: "+file.getAbsolutePath());
            Uri uri = Uri.fromFile(new File(file.getAbsolutePath()));
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "share via"));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        setHasOptionsMenu(true);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        initListeners();


        spinnerArray = new ArrayList<String>();
        allPathes = Save_Path.listAll(Save_Path.class);
        spinnerArray.add("Saved Routes");
        for (int z = 0; z < allPathes.size(); z++) {
            Save_Path path = allPathes.get(z);
            spinnerArray.add(path.getName());
        }
        LinearLayout linearLayout = new LinearLayout(getActivity());
        ViewGroup.LayoutParams params1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(params1);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        result = new Button(getActivity());
        final Spinner spinner = new Spinner(getActivity());
        clear = new Button(getActivity());
        share = new Button(getActivity());
        save = new Button(getActivity());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        result.setLayoutParams(params);
        spinner.setLayoutParams(params);
        share.setLayoutParams(params);
        spinner.setAdapter(spinnerArrayAdapter);
        result.setText("get Route");
        save.setLayoutParams(params);
        save.setText("Save");
        clear.setLayoutParams(params);
        clear.setText("clear");
        share.setText("Share");
//        share.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Random random = new Random();
//                String randoma = generateString(random,"abcdefghijklmnopqrst",10);
//                View view = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
//                View screenView = view.getRootView();
//                screenView.setDrawingCacheEnabled(true);
//                Bitmap bm = Bitmap.createBitmap(screenView.getDrawingCache());
//                screenView.setDrawingCacheEnabled(false);
//                final String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
//                File dir = new File(dirPath);
//                if(!dir.exists())
//                    dir.mkdirs();
//                File file = new File(dirPath, randoma);
//                try {
//                    FileOutputStream fOut = new FileOutputStream(file);
//                    bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
//                    fOut.flush();
//                    fOut.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                Uri uri = Uri.fromFile(file);
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_MEDIA_SHARED);
//                intent.setType("image/*");
//                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
//                intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
//                intent.putExtra(Intent.EXTRA_STREAM, uri);
//                startActivity(Intent.createChooser(intent, "Share Screenshot"));
//
//
//
//            }
//        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                allPathes = Save_Path.listAll(Save_Path.class);
                if (position == 0) {

                } else {
                    Save_Path fPath = allPathes.get(position - 1);
                    mPoints.clear();
                    if (mPoints != null) {
                        String[] stockar = convertStringToArray(fPath.getmPoint());
                        for (int y = 0; y < stockar.length; y++) {
                            String splitted = stockar[y];
                            String[] latlang = splitted.split(",");
                            mPoints.add(new LatLng(Double.parseDouble(latlang[0]), Double.parseDouble(latlang[1])));
                        }
                        getMap().clear();
                        for (int i = 0; i < mPoints.size(); i++) {
                            MarkerOptions options = new MarkerOptions().position(mPoints.get(i));
                            options.title(getAddressFromLatLng(mPoints.get(i)));
                            options.icon(BitmapDescriptorFactory.defaultMarker());
                            getMap().addMarker(options);

                        }


                        for (int i = 0; i < mPoints.size(); i++) {
                            LatLng tempLatLng = mPoints.get(i);
                            City city = new City(tempLatLng.latitude, tempLatLng.longitude);
                            TourManager.addCity(city);

                        }
                        Population pop = new Population(50, true);
                        System.out.println("Initial distance: " + pop.getFittest().getDistance());
                        pop = GA.evolvePopulation(pop);
                        for (int i = 0; i < 200; i++) {
                            pop = GA.evolvePopulation(pop);
                        }
                        System.out.println("Finished");
                        System.out.println("Final distance: " + pop.getFittest().getDistance());
                        System.out.println("Solution:");
                        System.out.println(pop.getFittest());
                        Tour map = pop.getFittest();
                        for (int x = 0; x < map.tourSize(); x++) {
                            City cityfROM = map.getCity(x);
                            int num = x + 1;
                            getMap().addMarker(new MarkerOptions()
                                    .position(new LatLng(cityfROM.getX(), cityfROM.getY()))
                                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2))
                                    .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.mipmap.ic_map, "" + num)))
                                    .anchor(0.5f, 1)
                            );
                            if ((x + 1) < map.tourSize()) {
                                City cityTo = map.getCity(x + 1);
                                requestDirection(new LatLng(cityfROM.getX(), cityfROM.getY()), new LatLng(cityTo.getX(), cityTo.getY()));

                            }
                        }
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LovelyTextInputDialog(getActivity(), R.style.EditTextTintTheme)
                        .setTopColorRes(R.color.colorPrimary)
                        .setTitle("Save Path")
                        .setMessage("Enter the name of the path")
                        .setIcon(R.drawable.ic_action_name)

                        .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                            @Override
                            public void onTextInputConfirmed(String text) {
                                LatLng[] stockArr = new LatLng[mPoints.size()];
                                stockArr = mPoints.toArray(stockArr);
                                Save_Path path = new Save_Path(text, convertArrayToString(stockArr));
                                path.save()
                                ;
                                final List<Save_Path> allPathes = Save_Path.listAll(Save_Path.class);
                                spinnerArray.clear();
                                spinnerArray.add("Saved Routes");
                                for (int z = 0; z < allPathes.size(); z++) {
                                    Save_Path patah = allPathes.get(z);
                                    spinnerArray.add(patah.getName());
                                }

                                spinnerArrayAdapter.notifyDataSetChanged();
                            }
                        })
                        .show();

            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMap().clear();
                mPoints.clear();
            }
        });
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPoints.isEmpty()) {
                    for (int i = 0; i < mPoints.size(); i++) {
                        LatLng tempLatLng = mPoints.get(i);
                        City city = new City(tempLatLng.latitude, tempLatLng.longitude);
                        TourManager.addCity(city);

                    }
                    Population pop = new Population(50, true);
                    System.out.println("Initial distance: " + pop.getFittest().getDistance());
                    pop = GA.evolvePopulation(pop);
                    for (int i = 0; i < 200; i++) {
                        pop = GA.evolvePopulation(pop);
                    }
                    System.out.println("Finished");
                    System.out.println("Final distance: " + pop.getFittest().getDistance());
                    System.out.println("Solution:");
                    System.out.println(pop.getFittest());
                    Tour map = pop.getFittest();
                    for (int x = 0; x < map.tourSize(); x++) {
                        City cityfROM = map.getCity(x);
                        int num = x + 1;
                        getMap().addMarker(new MarkerOptions()
                                .position(new LatLng(cityfROM.getX(), cityfROM.getY()))
                                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2))
                                .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.mipmap.ic_map, "" + num)))
                                .anchor(0.5f, 1)
                        );
                        if ((x + 1) < map.tourSize()) {
                            City cityTo = map.getCity(x + 1);
                            requestDirection(new LatLng(cityfROM.getX(), cityfROM.getY()), new LatLng(cityTo.getX(), cityTo.getY()));

                        } else {

                        }

                    }

                } else {
                    Toast.makeText(getActivity(), "Please Long Click on map to initialize Markers", Toast.LENGTH_SHORT).show();
                }


            }
        });
        ViewGroup viewGroup = (ViewGroup) view;
        linearLayout.addView(result);
        linearLayout.addView(save);
        linearLayout.addView(clear);
//        linearLayout.addView(share);
        linearLayout.addView(spinner);
        viewGroup.addView(linearLayout);
    }

    private void initListeners() {
        getMap().setOnMarkerClickListener(this);
        getMap().setOnMapLongClickListener(this);
        getMap().setOnInfoWindowClickListener(this);
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
        if (mCurrentLocation != null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            City current = new City(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            TourManager.addCity(current);

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
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions options = new MarkerOptions().position(latLng);
        options.title(getAddressFromLatLng(latLng));
        mPoints.add(latLng);
        options.icon(BitmapDescriptorFactory.defaultMarker());
        getMap().addMarker(options);

//        SharedPreferences prefs = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
//        String lats = prefs.getString("Lat", null);
//        String lans = prefs.getString("Lan", null);
//        SharedPreferences.Editor editor = sharedpreferences.edit();
//        editor.putString("Lat", String.valueOf(latLng.latitude));
//        editor.putString("Lan", String.valueOf(latLng.longitude));
//        editor.commit();


    }

    @Override
    public void onDetach() {
        super.onDetach();
//        SharedPreferences.Editor editor = sharedpreferences.edit();
//        editor.putString("Lat",null);
//        editor.putString("Lan",null);
//        editor.commit();
        mPoints = new ArrayList<>();

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


                            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                            Random rnd = new Random();
                            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

                            getMap().addPolyline(DirectionConverter.createPolyline(getActivity(), directionPositionList, 3, color));


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

    private Bitmap writeTextOnDrawable(int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(getActivity(), 20));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if (textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(getActivity(), 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));

        canvas.drawText(text, xPos, yPos, paint);

        return bm;
    }


    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f);

    }

    public static String strSeparator = "__,__";

    public static String convertArrayToString(LatLng[] array) {
        String str = "";
        for (int i = 0; i < array.length; i++) {
            str = str + array[i].latitude + "," + array[i].longitude;
            // Do not append comma at the end of last element
            if (i < array.length - 1) {
                str = str + strSeparator;
            }
        }
        return str;
    }

    public static String[] convertStringToArray(String str) {
        String[] arr = str.split(strSeparator);
        return arr;
    }
    public static String generateString(Random rng, String characters, int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }
    private Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private static File saveBitmap(Bitmap bm, String fileName){
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
