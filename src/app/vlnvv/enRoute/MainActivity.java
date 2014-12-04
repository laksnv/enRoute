package app.vlnvv.enRoute;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
import java.util.List;

public class MainActivity extends FragmentActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    AutoCompleteTextView from, to;
    Button loadDirections;

    private static final String LOG_TAG = "enRoute";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyDXngtZ_5yKXWfprUULkfRaGn6ukNO4BFs";

    private String start;
    private String end;

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
    }

    protected void goToSwipeView(List<Coordinates> foursquareMarkers) throws JSONException {
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
            end = to.getText().toString();

            if((new getVenuesTask()).execute(start, end) == null) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }

            /*
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + "saddr=" + 40.4947810 + "," + -74.4400870 + "&daddr=" + 39.0839970 + "," + -77.1527580));
            intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
            startActivity(intent);
            */
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


    protected class getVenuesTask extends AsyncTask<String, Void, List<Coordinates>> {

        @Override
        protected List<Coordinates> doInBackground(String... params) {

            BingMaps bingMaps = new BingMaps();
            try {
                enRoutePoints = bingMaps.getPointsOnRouteWithTolerance(params[0], params[1], 0.02);

            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }

            // Call FS method
            //Dummy foursquare markers

            Venue marker1 = new Venue();
            marker1.setCoordinates(new Coordinates(40.4867, -74.4444));

            Venue marker2 = new Venue();
            marker2.setCoordinates(new Coordinates(39.3642830, -74.4229270));

            List<Venue> foursquareMarkers = new ArrayList<Venue>();

            foursquareMarkers.add(marker1);
            foursquareMarkers.add(marker2);


            bingMaps.getDeviations(foursquareMarkers, enRoutePoints);

            float maxDeviation = 100;
            List<Coordinates> foursquareLocations = new ArrayList<Coordinates>();

            for(Venue v : foursquareMarkers) {
                if(v.getDeviation() < maxDeviation) {
                    foursquareLocations.add(new Coordinates(v.getCoordinates().getLatitude(), v.getCoordinates().getLongitude()));
                }
            }

            return foursquareLocations;
        }

        /**
         * A method that's called once doInBackground() completes. Set the markers in map
         * that displays their details. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(List<Coordinates> foursquareMarkers) {
            // Add all these locations to myMarkersArray
            try {
                goToSwipeView(foursquareMarkers);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}