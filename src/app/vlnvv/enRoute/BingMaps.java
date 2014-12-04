package app.vlnvv.enRoute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BingMaps
{
    private double totalDistance;

    public double getTotalDistance() {
        return totalDistance;
    }

	public void getDeviations(List<Venue> foursquareMarkers, Coordinates[] pathCoordinates)
	{
		Coordinates sourceCoordinates =  pathCoordinates[0];
        Coordinates destCoordinates =  pathCoordinates[pathCoordinates.length -1];
        
		for(int x=0; x<foursquareMarkers.size(); x++)
        {
        	Venue fsqMarker = foursquareMarkers.get(x);
        	int i=0;
        	double deviation = 0;
            Coordinates fsqMarkerCoords = new Coordinates(fsqMarker.getCoordinates().getLatitude(),fsqMarker.getCoordinates().getLongitude());
        	double distanceFromSource = calculateDistance(fsqMarkerCoords, sourceCoordinates, 'K');
        	double distanceFromDestination = calculateDistance(fsqMarkerCoords, destCoordinates, 'K');
        	boolean gettingFarther = false;
        	if(distanceFromSource < distanceFromDestination)
        	{
        		//Closer to source. Now find minimum deviation from main path.
        		deviation = distanceFromSource;
        		for(int j=1;j<pathCoordinates.length-1;j++)
        		{
        			double distanceFromMarker = calculateDistance(fsqMarkerCoords, pathCoordinates[j], 'K');
        			if(distanceFromMarker < deviation)
        			{
        				deviation = distanceFromMarker; 
        			}
        			else
        			{
        				if(gettingFarther == true)
        					break;
        				else
        					gettingFarther = true;
        			}
        		}
        		fsqMarker.setDeviation(deviation);
        	}
        	else
        	{
        		deviation = distanceFromDestination;
        		for(int j=pathCoordinates.length-2;j>0;j--)
        		{
        			double distanceFromMarker = calculateDistance(fsqMarkerCoords, pathCoordinates[j], 'K');
        			if(distanceFromMarker < deviation)
        			{
        				deviation = distanceFromMarker; 
        			}
        			else
        			{
        				if(gettingFarther == true)
        					break;
        				else
        					gettingFarther = true;
        			}
        		}
                fsqMarker.setDeviation(deviation);
        	}
        }
	}
	
	public double calculateDistance(Coordinates coordinates1, Coordinates coordinates2, char unit) {
		double lat1 = coordinates1.getLatitude();
		double lon1 = coordinates1.getLongitude();
		double lat2 = coordinates2.getLatitude();
		double lon2 = coordinates2.getLongitude();
		  double theta = lon1 - lon2;
		  double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		  dist = Math.acos(dist);
		  dist = rad2deg(dist);
		  dist = dist * 60 * 1.1515;
		  if (unit == 'K') {
		    dist = dist * 1.609344;
		  } else if (unit == 'N') {
		  	dist = dist * 0.8684;
		    }
		  return (dist);
		}

		private double deg2rad(double deg) {
		  return (deg * Math.PI / 180.0);
		}

		private double rad2deg(double rad) {
		  return (rad * 180 / Math.PI);
		}
	
	public Coordinates[] getPointsOnRouteWithTolerance(String source, String destination, double tolerance) throws Exception {
        // Binf API key
		String key = "AhIRfZxIaRl_WYcGbiF3gpFckEDFJDxr1xi6dx3QNrY8I906kJOQTPYFBQ7UHcrS";
		List<Coordinates> pathCoordinates = null;
		try {
			source = URLEncoder.encode(source, "UTF-8");
			destination = URLEncoder.encode(destination, "UTF-8");
            URL url = new URL("http://dev.virtualearth.net/REST/V1/Routes/Driving?wp.1="+source+"&wp.2="+destination+"&optmz=distance&rpo=Points&tolerances="+tolerance+"&key="+key);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            JSONObject bingResponse = readStream(con.getInputStream());
            totalDistance = bingResponse.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).getDouble("travelDistance");
            JSONObject routePath = bingResponse.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).getJSONObject("routePath");
            JSONArray pathIndices = routePath.getJSONArray("generalizations").getJSONObject(0).getJSONArray("pathIndices");
            JSONArray allCoordinates = routePath.getJSONObject("line").getJSONArray("coordinates");

            pathCoordinates = new ArrayList<Coordinates>();
            for(int i=0; i<pathIndices.length(); i++)
            {
                JSONArray coordinates = allCoordinates.getJSONArray(pathIndices.getInt(i));
                pathCoordinates.add(new Coordinates(coordinates.getDouble(0), coordinates.getDouble(1)));
            }
        }
		catch (Exception e) {
            e.printStackTrace();
            throw e;
			}
		return pathCoordinates.toArray(new Coordinates[pathCoordinates.size()]);
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
}
