package at.barcamp.cleancity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RequestHandler {
	
	public static void SendReport(String url, String imagePath, String description, double lat, double lng, String access_token, String userId) throws JSONException, IOException{
		Post(url,buildJSON(encodeFile(imagePath),description,lat, lng, access_token, userId));
	}
	
	private static void Post(String url, JSONObject data){
    	try {
            URL url1;
            URLConnection urlConnection;
            DataOutputStream outStream;
     
            String body = data.toString();
            
            // Create connection
            url1 = new URL(url);
            urlConnection = url1.openConnection();
            ((HttpURLConnection)urlConnection).setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Content-Length", ""+ body.length());
     
            // Create I/O streams
            outStream = new DataOutputStream(urlConnection.getOutputStream());
     
            // Send request
            outStream.writeBytes(body);
            outStream.flush();
            outStream.close();
     
            // Close I/O streams
            outStream.close();
        }
        catch(Exception ex) {
            Log.e("TAG", ex.getMessage());
        }
    	
    }

	private static String encodeFile(String path) throws IOException{
    	File f = new File(path);
    	FileInputStream fs = new FileInputStream(f);
    	
    	byte[] b = new byte[(int) f.length()];
    	fs.read(b);
    	
    	return Base64.encodeBytes(b);
    }
	
	private static JSONObject buildJSON(String image, String desc, double lat, double lng, String access_token, String userId) throws JSONException{    	
    	JSONObject json = new JSONObject();
    	JSONObject report = new JSONObject();
    	
    	report.put("os_info", userId); 
    	report.put("description", desc);
    	report.put("latitude", lat);
    	report.put("longitude", lng);
    	report.put("image", image);
    	report.put("access_token", access_token);
    	
    	json.put("incident_report", report);
    	
    	
    	return json;
    }
	
}
