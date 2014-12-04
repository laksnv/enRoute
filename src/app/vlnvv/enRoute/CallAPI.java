package app.vlnvv.enRoute;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CallAPI {

	public String JSON_result;
	
	public CallAPI(String url) { //should take url as  String input
		JSON_result = init(url);
		//Log.i("JSON RESULT : ",JSON_result);
	}
	
	public String init(String url){
		String readJSON;
		try{
	    	readJSON  = getJSON(url);
	    	//JSONObject jsonObject = new JSONObject(readJSON);
	    	//Log.i(MainActivity.class.getName(), jsonObject.getString("date"));
	    	
	    } 
	    catch(Exception e){
	    	
	    		e.printStackTrace();
	    		return e.toString();
	    }
		return readJSON;

	}

	public String getJSON(String address){
		BufferedReader reader =null;
    	StringBuilder builder = new StringBuilder();
    	HttpClient client = new DefaultHttpClient();
    	HttpGet httpGet = new HttpGet(address);
    	try{
    		HttpResponse response = client.execute(httpGet);
    		StatusLine statusLine = response.getStatusLine();
    		int statusCode = statusLine.getStatusCode();
    		if(statusCode == 200){
    			HttpEntity entity = response.getEntity();
    			InputStream content = entity.getContent();
    			reader = new BufferedReader(new InputStreamReader(content));
    			String line;
    			while((line = reader.readLine()) != null){
    				builder.append(line);
    			}
    		} else {
    			Log.e(MainActivity.class.toString(),"Failed at JSON object");
    		}
    	}catch(ClientProtocolException e){
    		e.printStackTrace();
    	} catch (IOException e){
    		e.printStackTrace();
    	}
    	finally{
    		if (reader != null) {
  		      try {
  		        reader.close();
  		      } catch (IOException e) {
  		        e.printStackTrace();
  		        }
  		    }    		
    	}
    	return builder.toString();
    }
	
	public JSONObject convertToJSON(String response){
	
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
