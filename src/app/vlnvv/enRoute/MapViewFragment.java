package app.vlnvv.enRoute;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Vicky on 11/27/14.
 */
public class MapViewFragment extends Fragment implements RoutingListener,GoogleMap.OnMarkerClickListener, View.OnClickListener {

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static View inflatedView;

    private GoogleMap mMap = null;
    private LatLngBounds bounds;
    private Button navigate;

    private Coordinates fromPosition;
    private Coordinates toPosition;

    // Contains all markers added to map
    private Map<Marker, Venue> mMarkersHashMap = new HashMap<Marker, Venue>();

    // Contains a list of Venue objects
    private List<Venue> mMyMarkersArray = new ArrayList<Venue>();

    // Foursquare location list
    private List<Venue> foursquareLocations;

    private List<Marker> selectedVenues = new ArrayList<Marker>();

    private ImageLoader imageLoader;
    private DisplayImageOptions options;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        inflatedView = inflater.inflate(R.layout.map_fragment, container, false);

        return inflatedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        navigate = (Button) getView().findViewById(R.id.navigate);
        navigate.setOnClickListener(this);

        initImageLoader();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.blank_image)
                .cacheInMemory(true)
                .build();

        initMarkers();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mMap != null)
            setUpMap();

        if (mMap == null) {
            setUpMapIfNeeded();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setUpMapIfNeeded();
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call setUpMap() once when mMap is not null.
     * <p/>
     * If it isn't installed SupportMapFragment (and com.google.android.gms.maps.MapView MapView)
     * will show a prompt for the user to install/update the Google Play services APK on their device.
     * <p/>
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
            mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            } else {
                Toast.makeText(getActivity(), "Unable to create Maps", Toast.LENGTH_SHORT).show();
                Log.e("DEBUG", "Unable to create Maps");
            }
        } else {
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

        if (mMap != null) {

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

        if (fromPosition != null && toPosition != null) {
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


        if (markers.size() > 0) {
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
        for (Venue v : foursquareLocations) {
            mMyMarkersArray.add(v);
        }

        setUpMapIfNeeded();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.navigate) {

            StringBuilder MapsApiUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + fromPosition.getLatitude() + "," + fromPosition.getLongitude() +
                    "&destination=" + toPosition.getLatitude() + "," + toPosition.getLongitude() +
                    "&waypoints=optimize:true");

            for(Marker marker : selectedVenues) {
                MapsApiUrl.append(
                        "|" + marker.getPosition().latitude + "," + marker.getPosition().longitude
                );
            }

            MapsApiUrl.append("&key=AIzaSyDXngtZ_5yKXWfprUULkfRaGn6ukNO4BFs");

            try {
                (new GetWayPointsOrder()).execute(new URL(MapsApiUrl.toString()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }


    public void IntentGMap(List<Marker> orderedMarkers) {

        StringBuilder BASE_URL = new StringBuilder(
                "https://www.google.com/maps/dir/" +
                        fromPosition.getLatitude() + "," + fromPosition.getLongitude() + "/"
        );

        for(Marker marker : orderedMarkers) {
            BASE_URL.append(
                    marker.getPosition().latitude + "," + marker.getPosition().longitude + "/"
            );
        }

        BASE_URL.append(
                toPosition.getLatitude() + "," + toPosition.getLongitude() + "/"
        );

        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_URL.toString()));
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
        startActivity(intent);
    }



    // Custom InfoWindow, overrides default class
    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View v;

        public MarkerInfoWindowAdapter() {
            v = getActivity().getLayoutInflater().inflate(R.layout.info_window, null);
        }

        @Override
        public View getInfoWindow(final Marker marker) {

            ImageView venueImage = (ImageView) v.findViewById(R.id.marker_icon);
            String imageUrl = mMarkersHashMap.get(marker).getImg_url();

            if(imageUrl != null) {
                imageLoader.displayImage(imageUrl, venueImage, options,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                super.onLoadingCancelled(imageUri, view);
                                getInfoContents(marker);
                            }
                        }
                );
            } else {
                venueImage.setImageResource(R.drawable.blank_image);
            }

            Venue venue = mMarkersHashMap.get(marker);
            RatingBar venueRating = (RatingBar) v.findViewById(R.id.rating);
            TextView venueName = (TextView) v.findViewById(R.id.marker_label);
            TextView venueAddress = (TextView) v.findViewById(R.id.address);

            venueName.setText(venue.getName());
            venueAddress.setText(venue.getAddress());
            venueRating.setRating(venue.getRating()/2);

            return v;
        }

        // Use info_window.xml as layout file and display info from Venue class
        @Override
        public View getInfoContents(Marker marker) {

            if(marker != null && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }

            return null;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (selectedVenues.contains(marker)) {
            selectedVenues.remove(marker);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        } else {
            selectedVenues.add(marker);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        return false;
    }


    private class GetWayPointsOrder extends AsyncTask<URL, Void, List<Marker> > {

        @Override
        protected List<Marker> doInBackground(URL... params) {

            HttpURLConnection con = null;
            List<Marker> orderedVenues = null;
            try {
                con = (HttpURLConnection) params[0].openConnection();
                JSONObject response = readStream(con.getInputStream());
                JSONArray wayPointsOrder = response.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order");
                orderedVenues = new ArrayList<Marker>();

                for(int i=0; i< wayPointsOrder.length();i++)
                {
                    int index = wayPointsOrder.getInt(i);
                    orderedVenues.add(selectedVenues.get(index));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return orderedVenues;
        }

        private JSONObject readStream(InputStream in)
        {
            StringBuilder response = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return jsonResponse;
        }


        @Override
        protected void onPostExecute(List<Marker> orderedMarkers) {
            //super.onPostExecute(orderedMarkers);

            IntentGMap(orderedMarkers);
        }
    }


    private void initImageLoader() {
        int memoryCacheSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            int memClass = ((ActivityManager)
                    getActivity().getSystemService(Context.ACTIVITY_SERVICE))
                    .getMemoryClass();
            memoryCacheSize = (memClass / 8) * 1024 * 1024;
        } else {
            memoryCacheSize = 2 * 1024 * 1024;
        }

        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this.getActivity()).threadPoolSize(5)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(memoryCacheSize)
                .memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize - 1000000))
                .denyCacheImageMultipleSizesInMemory()
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(config);
    }

    // Location Updates

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

}