package at.barcamp.cleancity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class Uploader extends Activity implements LocationListener, Runnable{
	
	// path to image (generated by OS!)
	private String imageUrl;
	
	// controls..
	private Button picture;
	private EditText desc;
	private Button send;
	private Uri imageUri;
	private ImageView image;
	private ImageView emptyImage;
	
	// error-flags
	private static final int TAKE_PICTURE = 0;

	// REST API Server
	private static String SERVER_URL = "http://cleancity.dyndns.org/1/incident_reports";
	
	// scale image to...
	private static final int width = 640;
	private static final int heigh = 480;
	
	// ocation stuff..
	LocationManager locationManager;
	double dlat;
	double dlng;
	
	// facebook accessToken to send to server
	private String accessToken = "";
	// facebook user id to send to server
	private String userId = "( " +Build.BRAND +" / " +Build.MANUFACTURER +" / " +Build.PRODUCT +" / " +Build.TAGS +" / " +Build.DEVICE +" )";

	// upload-progress-dialog
	protected ProgressDialog dialog = null;

	// this, but for use in subclasses
	protected Uploader me = this;

	// fleg to check if a picture was taken or not
	private boolean picture_taken = false;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        picture = (Button)findViewById(R.id.uploader_picture);
        send = (Button)findViewById(R.id.uploader_send); 
        desc = (EditText)findViewById(R.id.uploader_desc);
        image = (ImageView)findViewById(R.id.uploader_image);
        
        emptyImage = image;
        
        // set default image
        me.image.setImageResource(android.R.drawable.ic_menu_camera);
        
        // get facebook credentials...
        SharedPreferences savedSession = this.getSharedPreferences("facebook-session", Context.MODE_PRIVATE);
        accessToken = savedSession.getString("access_token", null);
        
        // set GPS...
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria(); 
		c.setAccuracy(Criteria.ACCURACY_FINE); 
        String provider = locationManager.getBestProvider(c, true);
        locationManager.requestLocationUpdates(provider, 1000L, 500.0f, this);
		
        picture.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// open camera view here
				takePhoto(arg0);
			} 
        }); 
        
        send.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				
				if (me.desc.getText().length() <= 0) { // no desc
					if (me.picture_taken) { // no desc but picture
						GUITools.showOKDialog(me, R.string.uploader_error_noDesc);
					} else { // no desc and no picture
						GUITools.showOKDialog(me, R.string.uploader_error_noPic_noDesc);
					}
				} else if (!me.picture_taken) { // desc but no picture
					GUITools.showOKDialog(me, R.string.uploader_error_noPic);
				} else { // desc and picture => send
				// start upload-thread here...
				java.lang.Thread thread = new java.lang.Thread(me);
				thread.start();
				
				dialog = ProgressDialog.show(
						me , me.getString(R.string.app_name), me.getString(R.string.uploader_sending), true,false
											);
				}
			} 
        }); 
        
    }
    
    // close the activity properly
    @Override
	public void onBackPressed() {
		this.finish();
	}
    
    private File getTempFile(Context context){
    	  //it will return /sdcard/image.tmp
    	  final File path = new File( Environment.getExternalStorageDirectory(), context.getPackageName() );
    	  if(!path.exists()){
    	    path.mkdir();
    	  }
    	  return new File(path, "clearcity_tmp.tmp");
    }
    
    // take the picture...
    public void takePhoto(View view) {
    	Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    	File photo = getTempFile(this);
        imageUrl = photo.getAbsolutePath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }
    
    // when the picture was taken...
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case TAKE_PICTURE :
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = imageUri;
                getContentResolver().notifyChange(selectedImage, null);
                
                // do the scaling...
                Bitmap bitm = BitmapFactory.decodeFile(imageUrl);
                //Bitmap bitm = Media.getBitmap(getContentResolver(), Uri.fromFile(new File(imageUrl)) );
                
                int origWidth = bitm.getWidth();
                int origHeight = bitm.getHeight();
                
                float scaleWidth;
                float scaleHeight;
                
                // calculate scale-values..
                if (origWidth > origHeight) { // wide!
	                scaleWidth = ((float) width) / origWidth;
	                scaleHeight = scaleWidth;
                } else { // high!
                	scaleHeight = ((float) heigh) / origHeight;
                	scaleWidth = scaleHeight;
                } 
                
                Matrix matrix = new Matrix();
                
                matrix.postScale(scaleWidth, scaleHeight);
                
                Bitmap resizedBitm = Bitmap.createBitmap(bitm,0,0,origWidth,origHeight,matrix,true);
                
                FileOutputStream out = null;
                try {
                	out = new FileOutputStream(imageUrl);
					resizedBitm.compress(Bitmap.CompressFormat.JPEG, 100, out);
				} catch (FileNotFoundException e) {
					Log.e("TAG", e.getMessage());
				} finally {
					try {
						out.close();
					} catch (IOException e) {
						Log.e("TAG", e.getMessage());
					}
				}
                
                // append to GUI
                Drawable d = Drawable.createFromPath(imageUrl);
                image.setImageDrawable(d);
                
                this.picture_taken = true;
            }
        }
    }

	public void onLocationChanged(Location location) {
		if (location != null) {
			dlat = location.getLatitude();
			dlng = location.getLongitude();
//			int lat = (int) (location.getLatitude() * 1000000);
//			int lng = (int) (location.getLongitude() * 1000000);
//			TextView tv = (TextView) findViewById(R.id.txtTitle);
//			tv.setText(String.valueOf(dlat) + "-" + String.valueOf(dlng));
		}
	}
	
	// thread to send the data
	public void run() {
		boolean error = false;
		try {
			RequestHandler.SendReport(SERVER_URL, imageUrl, desc.getText().toString(), dlat, dlng, accessToken, userId);
		} catch (JSONException e) {
			Log.e("UPLOADER", e.getMessage());
			GUITools.showOKDialog(me, e.getMessage());
			error = true;
		} catch (IOException e) {
			Log.e("UPLOADER", e.getMessage());
			GUITools.showOKDialog(me, e.getMessage());
			error = true;
		}
		if (!error) handler.sendEmptyMessage(0);
	}
	
	 private Handler handler = new Handler() {
         public void handleMessage(Message msg) {
             
        	 dialog.dismiss();
     		GUITools.showOKDialog(me, R.string.uploader_sended);
     		me.desc.setText("");
     		// reset image here
     		me.image.setImageResource(android.R.drawable.ic_menu_camera);
     		me.picture_taken  = false;
         }
 };

	public void onProviderDisabled(String arg0) {
		// do nothing
		
	}

	public void onProviderEnabled(String arg0) {
		// do nothing
		
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// do nothing
		
	}

}

