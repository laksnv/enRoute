package app.vlnvv.enRoute;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private AutoCompleteTextView from, to;
    private Button loadDirections;
    private android.support.v7.widget.Toolbar toolbar;

    public static final String LOG_TAG = "app.vlnvv.enRoute";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyDXngtZ_5yKXWfprUULkfRaGn6ukNO4BFs";

    private String start = null;
    private String end = null;

    protected Coordinates[] enRoutePoints;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);
        loadDirections = (Button) findViewById(R.id.load_directions);

        from.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        from.setOnItemClickListener(this);

        to.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        to.setOnItemClickListener(this);

        loadDirections.setOnClickListener(this);

        // Custom font
        SpannableString s = new SpannableString("enRoute");
        //s.setSpan(new TypefaceSpan(this, "Allura-Regular.otf"), 0, s.length(),
                //Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the tool bar title with the TypefaceSpan instance
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle(s);
        setSupportActionBar(toolbar);

        // Start location update
        getLocation();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.app_settings) {

        }

        return super.onOptionsItemSelected(item);
    }

    protected void getLocation() {
        // Instantiate the location manager, note you will need to request permissions in your manifest
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Get the last know location from your location manager.
        Location location= locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getAddressLine(1);
            String country = addresses.get(0).getAddressLine(2);
            from.setHint(address + " " + city + " " + country);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void goToSwipeView(List<Venue> foursquareMarkers) throws JSONException {
        Intent intent = new Intent(this, SwipeView.class);

        intent.putExtra("fsqMarkers", (java.io.Serializable) foursquareMarkers);

        if(enRoutePoints != null && enRoutePoints.length >= 2) {
            intent.putExtra("source", enRoutePoints[0]);
            intent.putExtra("destination", enRoutePoints[enRoutePoints.length - 1]);
        }
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.load_directions) {

            start = from.getText().toString();

            if(start == null || start.length() == 0) {
                start = from.getHint().toString();
            }

            end = to.getText().toString();

            if(end == null || end.length() == 0) {
                Toast.makeText(this, "Enter destination", Toast.LENGTH_SHORT).show();

            } else {
                if (start != null && start.length() > 0) {
                    if ((new getVenuesTask()).execute(start, end) == null) {
                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String str = (String) adapterView.getItemAtPosition(i);
    }

    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }


    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {

                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }
    }


    protected class getVenuesTask extends AsyncTask<String, Void, List<Venue> > {

        @Override
        protected List<Venue> doInBackground(String... params) {

            BingMaps bingMaps = new BingMaps();
            try {
                enRoutePoints = bingMaps.getPointsOnRouteWithTolerance(params[0], params[1], 0.02);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "BingMaps getEnRoutePoints Exception thrown");
                return null;
            }

            Coordinates source = enRoutePoints[0];
            Coordinates destination = enRoutePoints[enRoutePoints.length - 1];

            FourSquare fs = new FourSquare(source, destination);
            CallAPI call = new CallAPI(fs.getUrl(), fs.getAltURL());

            String response = call.JSON_result;
            JSONObject JSONresponse = call.convertToJSON(response);
            ArrayList<Venue> foursquareMarkers = fs.buildListOfVenues(JSONresponse);

            bingMaps.getDeviations(foursquareMarkers, enRoutePoints);

            String dev = ((EditText) findViewById(R.id.max_deviation)).getText().toString();

            // Default deviation is 100km
            float maxDeviation = 100;

            if(dev.length() != 0 && tryParseFloat(dev) == true) {
                maxDeviation = Float.parseFloat(dev);
            }

            List<Venue> foursquareLocations = new ArrayList<Venue>();

            for(Venue v : foursquareMarkers) {
                if(v.getDeviation() < maxDeviation) {
                    foursquareLocations.add(v);
                }
            }

            for(Venue v : foursquareLocations) {
                v.setEnRouteRating((((v.getRating() / v.getDeviation()) * bingMaps.getTotalDistance())));
            }

            Collections.sort(foursquareLocations);

            if(foursquareLocations.size() > 10) {
                return new ArrayList<Venue>(foursquareLocations.subList(0, 10));

            } else {
                return foursquareLocations;
            }
        }

        boolean tryParseFloat(String value) {
            try {
                Float.parseFloat(value);
                return true;

            } catch(NumberFormatException nfe) {
                return false;
            }
        }

        /**
         * A method that's called once doInBackground() completes. Set the markers in map
         * that displays their details. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(List<Venue> foursquareMarkers) {

            if(foursquareMarkers != null) {
                // Add all these locations to myMarkersArray
                try {
                    goToSwipeView(foursquareMarkers);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}