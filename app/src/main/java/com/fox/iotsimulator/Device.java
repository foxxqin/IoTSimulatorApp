package com.fox.iotsimulator;

import org.json.JSONException;
import org.json.JSONObject;

public class Device {
    public String DeviceID;
    public String Stage;
    public String Category;
    public String UserID;
    public String SubCategory;
    public int RunTime;
    public String TypeID;

    public Device(String userID, String deviceID, String stage, int runTime, String category, String subCategory, String typeID) {
        this.DeviceID = deviceID;
        this.Stage = stage;
        this.Category = category;
        this.RunTime = runTime;
        this.SubCategory = subCategory;
        this.UserID = userID;
        this.TypeID = typeID;
    }

    public Device(JSONObject device) {
        try {
            this.DeviceID = device.getString("id");
            this.UserID = device.getString("userId");
            this.SubCategory = device.getString("subCategory");
            this.RunTime = device.getInt("runs");
            this.Category = device.getString("category");
            this.Stage = device.getString("stage");
            this.TypeID = device.getString("typeId");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
