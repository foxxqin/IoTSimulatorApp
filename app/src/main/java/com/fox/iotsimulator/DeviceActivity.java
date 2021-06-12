package com.fox.iotsimulator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.core.Amplify;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeviceActivity extends AppCompatActivity {

    private TextView tvSubCategory, tvDeviceID, tvStage, tvRunTime;
    private Button btnStartStop, btnDisabled;
    private ProgressBar progressBar;
    private Device device;
    private String token;
    private static final String TAG = "DeviceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        tvSubCategory = findViewById(R.id.tvSubCategory);
        tvDeviceID = findViewById(R.id.tvDeviceID);
        tvRunTime = findViewById(R.id.tvRunTime);
        tvStage = findViewById(R.id.tvStage);

        btnStartStop = findViewById(R.id.btnStart);
        btnDisabled = findViewById(R.id.btnDisabled);
//        btnStartStop.setActivated(false);
        progressBar = findViewById(R.id.pbProgressBar);

        progressBar.setVisibility(View.VISIBLE);
        //check if login
        Amplify.Auth.fetchAuthSession(
            session -> {
                if (session.isSignedIn() == false) {
                    Intent loginIntent = new Intent(this, MainActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            },
            error -> Log.e("AuthSession", "Error +++++++" + error.getMessage())
        );

        //load token
        Amplify.Auth.fetchAuthSession(
            session -> {
                AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) session;
                switch(cognitoAuthSession.getUserPoolTokens().getType()) {
                    case SUCCESS:
                        token = cognitoAuthSession.getUserPoolTokens().getValue().getIdToken();
                        progressBar.setVisibility(View.INVISIBLE);
                        refreshView();

                        break;
                    case FAILURE:
                        Log.i("AuthQuickStart123", "IdentityId not present because: " + cognitoAuthSession.getUserPoolTokens().getError().toString());
                        Intent loginIntent = new Intent(this, MainActivity.class);
                        startActivity(loginIntent);
                        finish();
                }
            },
            error -> Log.e("AuthSession122", "Error +++++++" + error.getMessage())
        );

        //Get the data from the last activity
        Intent intent = getIntent();
        String userID = intent.getStringExtra("USERID");
        String deviceID = intent.getStringExtra("DEVICEID");
        String category = intent.getStringExtra("CATEGORY");
        String subCategory = intent.getStringExtra("SUBCATEGORY");
        String stage = intent.getStringExtra("STAGE");
        int runTime = intent.getIntExtra("RUNTIME", 0);
        String typeID = intent.getStringExtra("TYPEID");

        device = new Device(userID, deviceID, stage, runTime, category, subCategory, typeID);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
//                btnStartStop.setActivated(false);
                //send restapi request to start or stop the simulation
                StartOrStopDevice();
            }
        });

    }

    private void StartOrStopDevice() {
        String url = "https://3zpb6wo7kf.execute-api.ap-southeast-1.amazonaws.com/prod/devices/widgets/";
        url = url + device.DeviceID;

        JSONObject body = new JSONObject();
        try {
            body.put("typeId", device.TypeID);
            body.put("category", device.Category);
            body.put("subCategory", device.SubCategory);
            body.put("stage", device.Stage);
            if (device.Stage.equals("sleeping")) {
                body.put("operation", "hydrate");
            } else {
                body.put("operation", "stop");
            }
            body.put("metadata", new JSONArray());

        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                device = new Device(response);
                progressBar.setVisibility(View.INVISIBLE);
                refreshView();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Volley Response.ErrorListener: " + error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", token);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void refreshView() {
        tvDeviceID.setText(device.DeviceID);
        tvSubCategory.setText(device.SubCategory);
        tvStage.setText(device.Stage);
        tvRunTime.setText(String.valueOf(device.RunTime));

        //only when token is available, show the start stop button
        if (device.Stage.toLowerCase().equals("sleeping")) {
            btnStartStop.setText("START");
            tvStage.setTextColor(Color.RED);
        } else if (device.Stage.toLowerCase().equals("hydrated")){
            btnStartStop.setText("STOP");
            btnStartStop.setBackgroundColor(Color.RED);
            tvStage.setTextColor(Color.GREEN);
        } else if (device.Stage.toLowerCase().equals("stopping")){
            btnDisabled.setVisibility(View.VISIBLE);
            btnStartStop.setVisibility(View.INVISIBLE);
            btnStartStop.setText("STOP");
            btnStartStop.setBackgroundColor(Color.RED);
            tvStage.setTextColor(Color.GREEN);
        }

//        btnStartStop.setActivated(true);
    }
}