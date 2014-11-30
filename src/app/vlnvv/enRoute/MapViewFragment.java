package app.vlnvv.enRoute;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vicky on 11/27/14.
 */
public class MapViewFragment extends android.support.v4.app.Fragment implements RoutingListener {

    protected static View inflatedView;
    protected GoogleMap mMap = null;

    protected LatLng fromPosition;
    protected LatLng toPosition;

    // Contains all markers added to map
    private Map<Marker, MyMarker> mMarkersHashMap;

    // Contains a list of MyMarker objects
    private List<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();

    // Foursquare location list
    private List<Location> foursquareLocations = new ArrayList<Location>();

    boolean friendsDownloaded = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        inflatedView = inflater.inflate(R.layout.map_fragment, container, false);
        setUpMapIfNeeded();

        return inflatedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize the HashMap for markers and MyMarker objects
        mMarkersHashMap = new HashMap<Marker, MyMarker>();

        setUpMapIfNeeded();

        (new GetLocations(this.getActivity())).execute();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(mMap != null)
            setUpMap();

        if(mMap == null) {
            setUpMapIfNeeded();
        }
    }



    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call setUpMap() once when mMap is not null.
     *
     * If it isn't installed SupportMapFragment (and com.google.android.gms.maps.MapView MapView)
     * will show a prompt for the user to install/update the Google Play services APK on their device.
     *
     * A user can return to this Fragment after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the Fragment may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), onCreate(Bundle) may not be called again so we should call this
     * method in onResume() to guarantee that it will be called.
     */
    public void setUpMapIfNeeded() {

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            android.support.v4.app.FragmentManager fragMan = getChildFragmentManager();
            mMap = ((SupportMapFragment) fragMan.findFragmentById(R.id.map)).getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            } else {
                Toast.makeText(getActivity(), "Unable to create Maps", Toast.LENGTH_SHORT).show();
                Log.e("DEBUG", "Unable to create Maps");
            }
        }
        else {
            setUpMap();
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that mMap is not null.
     */
    public void setUpMap() {

        // To display "move to my location" button
        mMap.setMyLocationEnabled(true);

        fromPosition = new LatLng(40.4947810, -74.4400870);
        toPosition = new LatLng(39.0839970, -77.1527580);

        displayRoute(fromPosition, toPosition);
        /*
        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(40.4947810, -74.4400870),
                        new LatLng(39.0839970, -77.1527580))
                .color(Color.BLUE).geodesic(true));
        */

        LatLngBounds bounds = setMarkers(mMyMarkersArray);

        // Offset from edges of map in pixels
        int padding = 100;
        CameraUpdate cameraUpdate;

        // Display all markers
        if(bounds != null) {
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cameraUpdate);
        }
    }


    protected void displayRoute(LatLng fromPosition, LatLng toPosition) {
        Routing routing = new Routing(Routing.TravelMode.WALKING);
        routing.registerListener(this);
        routing.execute(fromPosition, toPosition);
    }


    @Override
    public void onRoutingFailure() {
        // The Routing request failed
        Log.e("DEBUG", "Routing request failed");
        Toast.makeText(getActivity(), "Routing request failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {
        // The Routing Request starts
    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        PolylineOptions polyoptions = new PolylineOptions();
        polyoptions.color(Color.BLUE);
        polyoptions.width(10);
        polyoptions.addAll(mPolyOptions.getPoints());
        mMap.addPolyline(polyoptions);

        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(fromPosition);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        mMap.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(toPosition);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        mMap.addMarker(options);
    }


    protected LatLngBounds setMarkers(List<MyMarker> markers) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds bounds = null;

        if(friendsDownloaded && (markers.size() > 0)) {
            for (MyMarker myMarker : markers) {

                // Create user marker with custom icon and other options
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()))
                        .title(myMarker.getmLabel())
                        //.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeByteArray(myMarker.getmIcon(), 0, myMarker.getmIcon().length)));
                        ;

                Marker currentMarker = mMap.addMarker(markerOptions);
                mMarkersHashMap.put(currentMarker, myMarker);

                // Calculate bounds of all markers
                builder.include(currentMarker.getPosition());

                mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
            }
            bounds = builder.build();
        }

        return bounds;
    }


    // Called only once by onPostExecute() after downloading friends details
    protected void initMarkers() {
        byte[] byteArray;
        ByteArrayOutputStream stream;

        Bitmap venueImage = BitmapFactory.decodeResource(getResources(), R.drawable.blank_image);
        stream = new ByteArrayOutputStream();
        venueImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();

        // Should iterate through FS objects instead
        for(int i = 0; i < foursquareLocations.size(); i++) {
            int index = i + 1;
            mMyMarkersArray.add(new MyMarker("Venue "+ index, byteArray, foursquareLocations.get(i)));
        }

        friendsDownloaded = true;
        setUpMap();
    }


    // Called from doInBackground()
    protected void foursquareCall() {

        /*
         * Construct a parallelogram using src and dest co-ordinates
         * Get list of locations inside the parallelogram
         * Also, get the details: venue name, checkin count, image if available, rating
         */
        Location location = new Location("1");

        location.setLatitude(40.4947810);
        location.setLongitude(-74.4400870);
        foursquareLocations.add(location);

        location = new Location("2");
        location.setLatitude(39.0839970);
        location.setLongitude(-77.1527580);
        foursquareLocations.add(location);
    }

    // Called from doInBackground()
    protected void calcRatings() {

    }

    protected class GetLocations extends AsyncTask<Void, Void, Void> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;

        // Constructor called by the system to instantiate the task
        public GetLocations(Context context) {
            // Required by the semantics of AsyncTask
            super();

            // Set a Context for the background task
            localContext = context;
        }

        /**
         * Get all info to populate the map
         */
        @Override
        protected Void doInBackground(Void... params) {

            // Find all Scenic spots from Foursquare
            foursquareCall();

            // Calculate ratings in a new thread
            calcRatings();

            return null;
        }

        /**
         * A method that's called once doInBackground() completes. Set the markers in map
         * that displays their details. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(Void v) {
            // Add all these locations to myMarkersArray
            initMarkers();
        }
    }


    // Custom InfoWindow, overrides default class
    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
    {
        public MarkerInfoWindowAdapter() {}

        @Override
        public View getInfoWindow(Marker marker)
        {
            return null;
        }

        // Use info_window.xml as layout file and display info from MyMarker class
        @Override
        public View getInfoContents(Marker marker)
        {
            View v = getActivity().getLayoutInflater().inflate(R.layout.info_window, null);

            MyMarker myMarker = mMarkersHashMap.get(marker);

            ImageView venueImage = (ImageView) v.findViewById(R.id.marker_icon);
            TextView venueName = (TextView)v.findViewById(R.id.marker_label);

            venueImage.setImageBitmap(BitmapFactory.decodeByteArray(myMarker.getmIcon(), 0, myMarker.getmIcon().length));
            venueName.setText(myMarker.getmLabel());

            return v;
        }
    }
}