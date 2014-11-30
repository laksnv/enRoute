package app.vlnvv.enRoute;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Vicky on 11/27/14.
 */
public class MapViewFragment extends android.support.v4.app.Fragment {

    private static View inflatedView;
    private GoogleMap mMap = null;

    // Contains all markers added to map
    private HashMap<Marker, MyMarker> mMarkersHashMap;

    // Contains a list of MyMarker objects
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();

    // Holds the actual photos
    ArrayList<Bitmap> friendPhotos = new ArrayList<Bitmap>();

    // Holds the names of friends
    ArrayList<String> friendNames = new ArrayList<String>();

    // Holds friends locations
    ArrayList<Location> friendLocations = new ArrayList<Location>();

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

        setUpMapIfNeeded();
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

        // For showing "move to my location" button
        mMap.setMyLocationEnabled(true);

        mMap.addMarker(new MarkerOptions().position(new LatLng(40.4947810, -74.4400870)).title("My Home").snippet("Home Address"));
        // For zooming automatically to the Dropped PIN Location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.4947810, -74.4400870), 12.0f));
    }


    protected LatLngBounds setMarkers(ArrayList<MyMarker> markers) {

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
        int index = 0;
        byte[] byteArray;
        ByteArrayOutputStream stream;

        Bitmap userImage = BitmapFactory.decodeResource(getResources(), R.drawable.blank_image);
        stream = new ByteArrayOutputStream();
        userImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();

        // First marker is the user
//        if(myLatestLocation != null) {
//            mMyMarkersArray.add(new MyMarker("You", byteArray, myLatestLocation.getLatitude(), myLatestLocation.getLongitude()));
//        } else {
//            mMyMarkersArray.add(new MyMarker("You", byteArray, 0.0, 0.0));
//        }

        for(String name: friendNames) {
            if((name != null) && (friendLocations.get(index) != null)) {
                stream = new ByteArrayOutputStream();
                friendPhotos.get(index).compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();

                mMyMarkersArray.add(new MyMarker(name, byteArray, friendLocations.get(index).getLatitude(), friendLocations.get(index).getLongitude()));
            }
            index++;
        }

        friendsDownloaded = true;
    }


    // Custom InfoWindow, overrides default class
    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
    {
        public MarkerInfoWindowAdapter() {
        }

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

            ImageView friendImage = (ImageView) v.findViewById(R.id.marker_icon);
            TextView friendName = (TextView)v.findViewById(R.id.marker_label);
            TextView friendDistance = (TextView)v.findViewById(R.id.marker_distance);
            TextView lastUpdatedTime = (TextView)v.findViewById(R.id.marker_time);

            friendImage.setImageBitmap(BitmapFactory.decodeByteArray(myMarker.getmIcon(), 0, myMarker.getmIcon().length));
            friendName.setText(myMarker.getmLabel());

            String distance = String.format("%.3f", (myMarker.getmDistance()/1000.0)) + " km";
            friendDistance.setText(distance);

            return v;
        }
    }
}