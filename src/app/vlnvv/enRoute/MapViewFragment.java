package app.vlnvv.enRoute;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class MapViewFragment extends Fragment implements RoutingListener {

    private static View inflatedView;

    private GoogleMap mMap = null;
    private LatLngBounds bounds;

    private Coordinates fromPosition;
    private Coordinates toPosition;

    // Contains all markers added to map
    private Map<Marker, Venue> mMarkersHashMap;

    // Contains a list of Venue objects
    private List<Venue> mMyMarkersArray;

    // Foursquare location list
    private List<Venue> foursquareLocations;

    private List<Coordinates> selectedVenues;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        inflatedView = inflater.inflate(R.layout.map_fragment, container, false);

        return inflatedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize the HashMap for markers and Venue objects
        mMarkersHashMap = new HashMap<Marker, Venue>();

        selectedVenues = new ArrayList<Coordinates>();
        mMyMarkersArray = new ArrayList<Venue>();

        fromPosition = ((SwipeView) getActivity()).getSource();
        toPosition = ((SwipeView) getActivity()).getDestination();
        foursquareLocations = new ArrayList<Venue>(((SwipeView) getActivity()).getFoursquareMarkers());

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15));
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        initMarkers();
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
     * This should only be called when we are sure that mMap is not null.
     * It is called once after the calculations are done by initMarkers().
     */
    public void setUpMap() {

        // To display "move to my location" button
        mMap.setMyLocationEnabled(true);

        bounds = setMarkers(mMyMarkersArray);

        if (fromPosition != null && toPosition != null) {
            displayRoute(fromPosition, toPosition);
        }
    }


    protected void displayRoute(Coordinates fromPosition, Coordinates toPosition) {
        Routing routing = new Routing(Routing.TravelMode.DRIVING);
        routing.registerListener(this);

        LatLng source = new LatLng(fromPosition.getLatitude(), fromPosition.getLongitude());
        LatLng destination = new LatLng(toPosition.getLatitude(), toPosition.getLongitude());

        routing.execute(source, destination);
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

        if(mMap != null) {

            PolylineOptions polyoptions = new PolylineOptions();
            polyoptions.color(Color.BLUE);
            polyoptions.width(10);
            polyoptions.addAll(mPolyOptions.getPoints());
            mMap.addPolyline(polyoptions);


            // Start marker
            MarkerOptions options = new MarkerOptions();
            LatLng source = new LatLng(fromPosition.getLatitude(), fromPosition.getLongitude());
            options.position(source);
            //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
            mMap.addMarker(options);

            // End marker
            options = new MarkerOptions();
            LatLng destination = new LatLng(toPosition.getLatitude(), toPosition.getLongitude());
            options.position(destination);
            //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
            mMap.addMarker(options);
        }

    }


    protected LatLngBounds setMarkers(List<Venue> markers) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds bounds = null;
        MarkerOptions markerOptions;
        Marker currentMarker;

        if(fromPosition != null && toPosition != null) {
            markerOptions = new MarkerOptions()
                    .position(new LatLng(fromPosition.getLatitude(), fromPosition.getLongitude()))
                    .title("Source");
            currentMarker = mMap.addMarker(markerOptions);
            mMarkersHashMap.put(currentMarker, new Venue("Source", "", fromPosition, 0));
            builder.include(currentMarker.getPosition());

            markerOptions = new MarkerOptions()
                    .position(new LatLng(toPosition.getLatitude(), toPosition.getLongitude()))
                    .title("Destination")
            ;
            currentMarker = mMap.addMarker(markerOptions);
            mMarkersHashMap.put(currentMarker, new Venue("Destination", "", toPosition, 0));
            builder.include(currentMarker.getPosition());
        }


        if(markers.size() > 0) {
            for (Venue venue : markers) {

                // Create user marker with custom icon and other options
                markerOptions = new MarkerOptions()
                        .position(new LatLng(venue.getCoordinates().getLatitude(), venue.getCoordinates().getLongitude()))
                        .title(venue.getName())
                        //.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeByteArray(Venue.getmIcon(), 0, Venue.getmIcon().length)));
                        ;

                currentMarker = mMap.addMarker(markerOptions);
                mMarkersHashMap.put(currentMarker, venue);

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
        for(Venue v : foursquareLocations) {
            mMyMarkersArray.add(v);
        }

        setUpMapIfNeeded();
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

        // Use info_window.xml as layout file and display info from Venue class
        @Override
        public View getInfoContents(Marker marker)
        {
            View v = getActivity().getLayoutInflater().inflate(R.layout.info_window, null);

            Venue venue = mMarkersHashMap.get(marker);

            ImageView venueImage = (ImageView) v.findViewById(R.id.marker_icon);
            TextView venueRating = (TextView) v.findViewById(R.id.rating);
            TextView venueName = (TextView) v.findViewById(R.id.marker_label);
            TextView venueAddress = (TextView) v.findViewById(R.id.address);

            //venueImage.setImageBitmap(BitmapFactory.decodeByteArray(venue.getmIcon(), 0, venue.getmIcon().length));
            venueImage.setImageDrawable(getResources().getDrawable(R.drawable.blank_image));
            venueName.setText(venue.getName());
            venueAddress.setText(venue.getAddress());
            venueRating.setText(venue.getRating() + "");

            return v;
        }
    }
}