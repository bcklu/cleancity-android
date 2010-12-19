/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.barcamp.cleancity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.facebook.android.Util;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;


public class Login extends Activity {

    // Your Facebook Application ID must be set before running this example
    // See http://www.facebook.com/developers/createapp.php
    public static final String APP_ID = "171571639549640";

    private LoginButton mLoginButton;
    private TextView mText;

    public Login me = this;
    
    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (APP_ID == null) {
            Util.showAlert(this, "Warning", "Facebook Applicaton ID must be " +
                    "specified before running this example: see Example.java");
        }
        

        setContentView(R.layout.login);
        mLoginButton = (LoginButton) findViewById(R.id.login);
        mText = (TextView) this.findViewById(R.id.txt);

       	mFacebook = new Facebook(APP_ID);
       	mAsyncRunner = new AsyncFacebookRunner(mFacebook);
       	
        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());
        mLoginButton.init(this, mFacebook, new String[] {"offline_access"});
        
        if (mFacebook.isSessionValid()) {
        	this.startUploader();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mFacebook.authorizeCallback(requestCode, resultCode, data);
    }

    public class SampleAuthListener implements AuthListener {

        public void onAuthSucceed() {
            mText.setText("You have logged in! ");
            
            // OPEN 2nd ACTIVITY HERE
            startUploader();
            
        }

        public void onAuthFail(String error) {
            mText.setText("Login Failed: " + error);
        }
    }
    
    public void startUploader() {
    	Intent intent = new Intent(Login.this, Uploader.class);
        Login.this.startActivity(intent);
    }

    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
            mText.setText("Logging out...");
        }

        public void onLogoutFinish() {
            mText.setText("You have logged out! ");
        }
    }

}
