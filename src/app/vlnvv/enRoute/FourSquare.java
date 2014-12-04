package app.vlnvv.enRoute;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


//The call to callAPI object's init() should be asynchronous!!

public class FourSquare {
    //private ArrayList<Venue> venueList;
    private Coordinates source;
    private Coordinates dest;
    //private String[] queries;
    private String url;

    public FourSquare(Coordinates s, Coordinates d) {
        // TODO Auto-generated constructor stub

        Coordinates[] s_d = new Coordinates[2];

        s_d[0] = new Coordinates(s.getLatitude(),s.getLongitude());
        s_d[1] = new Coordinates(d.getLatitude(), d.getLongitude());
        s_d = processCo_ords(s_d);
        this.setSource(s_d[0]);
        this.setDest(s_d[1]);
        this.setUrl(this.buildURL(this.source, this.dest));
    }
    public Coordinates getSource() {
        return source;
    }
    public void setSource(Coordinates source) {
        this.source = source;
    }
    public Coordinates getDest() {
        return dest;
    }
    public void setDest(Coordinates dest) {
        this.dest = dest;
    }
	/*public String[] getQueries() {
		return queries;
	}
	public void setQueries(String[] queries) {
		this.queries = queries;
	}*/


    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }


    public Coordinates[] processCo_ords(Coordinates[] s_d){
        if(Math.abs(s_d[0].getLatitude()-s_d[1].getLatitude()) < 2.0)
        {
            if(s_d[0].getLatitude()<s_d[1].getLatitude()){
                s_d[0].setLatitude(s_d[0].getLatitude()-1);
                s_d[1].setLatitude(s_d[1].getLatitude()+1);
            }
            else
            {
                s_d[1].setLatitude(s_d[1].getLatitude()-1);
                s_d[0].setLatitude(s_d[0].getLatitude()+1);
            }
        }
        if(Math.abs(s_d[0].getLongitude()-s_d[1].getLongitude()) < 1.0)
        {
            if(s_d[0].getLongitude()<s_d[1].getLongitude()){
                s_d[0].setLongitude(s_d[0].getLongitude()-0.5);
                s_d[1].setLongitude(s_d[1].getLongitude()+0.5);
            }
            else
            {
                s_d[1].setLongitude(s_d[1].getLongitude()-0.5);
                s_d[0].setLongitude(s_d[0].getLongitude()+0.5);
            }
        }


        return s_d;

    }

    public String buildURL(Coordinates source, Coordinates dest){

        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd");
        Log.i("Date", ft.format(dNow).toString());
	    
	    /*//Sample URL
	     * String x = "https://api.foursquare.com/v2/venues/explore?&venuePhotos=1&limit=50"&query=Scenic%20Lookout" +" +
	    		"&ne=40.48,-74.44" +
	    		"&sw=39.08,-77.14" +	    		
	    		"&oauth_token=VVWFQRYSS331CYBXX2J0T44V3PTXXDNLNMOMUCL2RN5ZJVSA" +
	    		"&v=20141201";*/

        String BASE = "https://api.foursquare.com/v2/venues/explore?&venuePhotos=1&limit=50&query=Scenic%20Lookout";
        String OAUTH="VVWFQRYSS331CYBXX2J0T44V3PTXXDNLNMOMUCL2RN5ZJVSA";
        String VERSION = ft.format(dNow).toString();
        String NE = Double.toString(source.getLatitude())+","+Double.toString(source.getLongitude());
        String SW = Double.toString(dest.getLatitude())+","+Double.toString(dest.getLongitude());

        //String QUERY = this.buildQuery(queries);
		/*if(!QUERY.equals(""))
			result = result+"&query="+QUERY;*/

        String result = BASE+"&ne="+NE+"&sw="+SW+"&oauth_token="+OAUTH+"&v="+VERSION;
        return result;

    }
	
	/*//Query Builder Function
	 * public String buildQuery(String... queries){
		String result="";
		
		for(String query : queries)
			result+="%20"+query;
		
		return result;
	}*/


    public ArrayList<Venue> buildListOfVenues(JSONObject JSONresponse){

        ArrayList<Venue> venueList =new ArrayList<Venue>();
        try {
            JSONArray items = JSONresponse.getJSONObject("response").getJSONArray("groups").getJSONObject(0).getJSONArray("items");

            for(int i=0; i<items.length();i++){

                JSONObject location = items.getJSONObject(i).getJSONObject("venue");
                String name = location.has("name")?location.getString("name"):"";
                String address = "";
                JSONArray formattedAddress = location.getJSONObject("location").getJSONArray("formattedAddress");
                for(int j=0; j<formattedAddress.length();j++)
                    address+=formattedAddress.getString(j);
                double lat = Double.parseDouble(location.getJSONObject("location").getString("lat").toString());
                double lng = Double.parseDouble(location.getJSONObject("location").getString("lng").toString());
                float rating = Float.parseFloat(location.has("rating")?location.getString("rating").toString():"1.0");
                String prefix = (location.getJSONObject("photos").getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0).has("prefix"))?location.getJSONObject("photos").getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0).getString("prefix"):null;
                String suffix = (location.getJSONObject("photos").getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0).has("suffix"))?location.getJSONObject("photos").getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0).getString("suffix"):null;
                String photo = prefix+"75x75"+suffix;
                Coordinates coord = new Coordinates(lat, lng);
                Venue venue = new Venue(name, address, coord, rating);
                venue.setImg_url(photo);
                venueList.add(venue);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        //Venue[] venues = venueList.toArray(new Venue[venueList.size()]);


        return venueList;
    }

}

