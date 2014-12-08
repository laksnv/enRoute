package app.vlnvv.enRoute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


//The call to callAPI object's init() should be asynchronous!!

public class FourSquare {

    private Coordinates source;
	private Coordinates dest;
	private String url;
	private String altURL;
	
	public FourSquare(Coordinates s, Coordinates d) {

		Coordinates[] s_d = new Coordinates[2];
		
		s_d[0] = new Coordinates(s.getLatitude(),s.getLongitude());
		s_d[1] = new Coordinates(d.getLatitude(), d.getLongitude());
		s_d = processCo_ords(s_d);
		this.setSource(s_d[0]);
		this.setDest(s_d[1]);
		this.setUrl(this.buildURL(this.source, this.dest));
		this.setAltURL(buildaltURL(this.dest));
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
		
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public String getAltURL() {
		return altURL;
	}
	public void setAltURL(String altURL) {
		this.altURL = altURL;
	}
	
	
	
	public Coordinates[] processCo_ords(Coordinates[] s_d){
		if(Math.abs(s_d[0].getLatitude()-s_d[1].getLatitude()) < 1.0)
			{
				if(s_d[0].getLatitude()<s_d[1].getLatitude()){
					s_d[0].setLatitude(s_d[0].getLatitude()-0.1);
					s_d[1].setLatitude(s_d[1].getLatitude()+0.1);
				}
				else
				{
					s_d[1].setLatitude(s_d[1].getLatitude()-0.1);
					s_d[0].setLatitude(s_d[0].getLatitude()+0.1);
				}
			}
		if(Math.abs(s_d[0].getLongitude()-s_d[1].getLongitude()) < 1.0)
		{
			if(s_d[0].getLongitude()<s_d[1].getLongitude()){
				s_d[0].setLongitude(s_d[0].getLongitude()-0.1);
				s_d[1].setLongitude(s_d[1].getLongitude()+0.1);
			}
			else
			{
				s_d[1].setLongitude(s_d[1].getLongitude()-0.1);
				s_d[0].setLongitude(s_d[0].getLongitude()+0.1);
			}
		}
		
		
		return s_d;
		
	}
	
	public String buildaltURL(Coordinates dest){
		Date dNow = new Date( );
	    SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd");

	    String BASE = "https://api.foursquare.com/v2/venues/explore?&venuePhotos=1&limit=50&query=Scenic%20Lookout&radius=100000";
		String OAUTH="VVWFQRYSS331CYBXX2J0T44V3PTXXDNLNMOMUCL2RN5ZJVSA";
		String VERSION = ft.format(dNow).toString();
		
		String LL = Double.toString(dest.getLatitude())+","+Double.toString(dest.getLongitude());
		String result = BASE+"&ll="+LL+"&oauth_token="+OAUTH+"&v="+VERSION;
		
		
		
		return result;
		
		
	}

	public String buildURL(Coordinates source, Coordinates dest){
		
		Date dNow = new Date( );
	    SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd");

		String BASE = "https://api.foursquare.com/v2/venues/explore?&venuePhotos=1&limit=50&query=Scenic%20Lookout";
		String OAUTH="VVWFQRYSS331CYBXX2J0T44V3PTXXDNLNMOMUCL2RN5ZJVSA";
		String VERSION = ft.format(dNow).toString();
		String NE = Double.toString(source.getLatitude())+","+Double.toString(source.getLongitude());
		String SW = Double.toString(dest.getLatitude())+","+Double.toString(dest.getLongitude());
		String result = BASE+"&ne="+NE+"&sw="+SW+"&oauth_token="+OAUTH+"&v="+VERSION;

        return result;
	}
	

	
	public ArrayList<Venue> buildListOfVenues(JSONObject JSONresponse){
		
		ArrayList<Venue> venueList =new ArrayList<Venue>();
		try {
			JSONArray items = JSONresponse.getJSONObject("response").getJSONArray("groups").getJSONObject(0).getJSONArray("items");
			
			for(int i=0; i<items.length();i++){
				String photo=null;
				JSONObject location = items.getJSONObject(i).getJSONObject("venue");
				String name = location.has("name")?location.getString("name"):"";
				String address = "";
				JSONArray formattedAddress = location.getJSONObject("location").getJSONArray("formattedAddress");
				for(int j=0; j<formattedAddress.length();j++)
					address+=formattedAddress.getString(j);
				double lat = Double.parseDouble(location.getJSONObject("location").getString("lat").toString());
				double lng = Double.parseDouble(location.getJSONObject("location").getString("lng").toString());
				float rating = Float.parseFloat(location.has("rating")?location.getString("rating").toString():"1.0");
				
				try{
					String prefix = (location.getJSONObject("photos").getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0).has("prefix"))?location.getJSONObject("photos").getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0).getString("prefix"):null;
					String suffix = (location.getJSONObject("photos").getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0).has("suffix"))?location.getJSONObject("photos").getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0).getString("suffix"):null;
					photo = prefix+"300x300"+suffix;
				}
				catch(JSONException e)
				{
					photo = null;
				}
				Coordinates coord = new Coordinates(lat, lng);
				Venue venue = new Venue(name, address, coord, rating);
				venue.setImg_url(photo);
				venueList.add(venue);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();

            return null;
		}
		
		return venueList;
	}

}

