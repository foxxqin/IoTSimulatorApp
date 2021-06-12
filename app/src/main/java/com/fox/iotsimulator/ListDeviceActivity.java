package com.fox.iotsimulator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListDeviceActivity extends AppCompatActivity {

    private List<Device> deviceList = new ArrayList<>();
    private String token;
    private ProgressBar progressBar;
    private RecyclerView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_device);

        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.listView);

        //check if login
        Amplify.Auth.fetchAuthSession(
            session -> {
                if (session.isSignedIn() == false) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
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
                            progressBar.setVisibility(View.VISIBLE);
                            RequestDeviceList();
                            break;
                        case FAILURE:
                            Log.i("AuthQuickStart123", "IdentityId not present because: " + cognitoAuthSession.getUserPoolTokens().getError().toString());
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();
                    }
                },
                error -> Log.e("AuthSession122", "Error +++++++" + error.getMessage())
        );
    }

    private void RequestDeviceList() {
        String url = "https://3zpb6wo7kf.execute-api.ap-southeast-1.amazonaws.com/prod/devices/widgets?op=list";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i=0; i<response.length(); i++) {
                    try {
                        JSONObject jsonDevice = response.getJSONObject(i);
                        Device device = new Device(jsonDevice);
                        deviceList.add(device);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                ListViewAdapter adapter = new ListViewAdapter(deviceList);
                listView.setAdapter(adapter);
                progressBar.setVisibility(View.INVISIBLE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceID, tvStage, tvSubCategory, tvRunTime;
        CardView cardView;
        int postion;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDeviceID = itemView.findViewById(R.id.txtDeviceID);
            tvRunTime = itemView.findViewById(R.id.txtRunTime);
            tvStage = itemView.findViewById(R.id.txtStage);
            tvSubCategory = itemView.findViewById(R.id.txtSubCategory);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    private class ListViewAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<Device> deviceList;

        public ListViewAdapter(List<Device> data) {
            this.deviceList = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false));
            holder.cardView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), DeviceActivity.class);
                    intent.putExtra("DEVICEID", deviceList.get(holder.postion).DeviceID);
                    intent.putExtra("CATEGORY", deviceList.get(holder.postion).Category);
                    intent.putExtra("STAGE", deviceList.get(holder.postion).Stage);
                    intent.putExtra("SUBCATEGORY", deviceList.get(holder.postion).SubCategory);
                    intent.putExtra("USERID", deviceList.get(holder.postion).UserID);
                    intent.putExtra("RUNTIME", deviceList.get(holder.postion).RunTime);

                    startActivity(intent);
                }
            });
//            holder.tv1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    TextView tv = (TextView)view;
//                    String locationName = String.valueOf(tv.getText());
//
//                    navigateTo(locationName);
//                }
//            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Device device = deviceList.get(position);
            holder.tvDeviceID.setText(device.DeviceID);
            holder.tvSubCategory.setText(device.SubCategory);
            holder.tvStage.setText(device.Stage);
            holder.tvRunTime.setText(String.valueOf(device.RunTime));
            holder.postion = position;
            if (device.Stage.toLowerCase().equals("sleeping")) {
                holder.cardView.setCardBackgroundColor(Color.GRAY);
            } else {
                holder.cardView.setCardBackgroundColor(Color.BLUE);
            }
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }
    }
}

