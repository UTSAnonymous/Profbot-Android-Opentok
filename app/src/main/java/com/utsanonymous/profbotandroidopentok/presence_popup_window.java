package com.utsanonymous.profbotandroidopentok;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.utsanonymous.profbotandroidopentok.CallActivity;
import com.utsanonymous.profbotandroidopentok.util.Constants;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class presence_popup_window extends Activity {

    private String TAG = "presence_popup_window_class";
    private String robot_model;
    private String robot_location;
    private String private_channel;
    private String username;

    private static final int CONNECTION_REQUEST = 1;
    private static final int RC_CALL = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_up_available);

        Intent intent = getIntent();
        robot_model = intent.getStringExtra(Constants.ROBOT_NAME_KEY);
        robot_location = intent.getStringExtra(Constants.ROBOT_LOCATION_KEY);
        username = intent.getStringExtra(Constants.CHAT_USERNAME);

        TextView robot_name = (TextView) findViewById(R.id.bot_model_name);
        robot_name.setText(robot_model);
        TextView location = (TextView) findViewById(R.id.bot_pop_up_details);
        location.setText("is available in room " + robot_location);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.8),(int)(height*0.45));
    }

    @AfterPermissionGranted(RC_CALL)
    public void connectToRobot(View view) {
        Intent intent = new Intent(this,CallActivity.class);
        intent.putExtra(Constants.ROBOT_NAME_KEY, robot_model);
        intent.putExtra(Constants.ROBOT_LOCATION_KEY, robot_location);
        intent.putExtra(Constants.CHAT_USERNAME,username);

        String channel = robot_model+robot_location;
        private_channel = channel.replaceAll("[\\s\\.]","");
        Log.i(TAG, "channel name " + private_channel);
        intent.putExtra(Constants.PRIVATE_CHANNEL_KEY, private_channel);
        finish();
        startActivityForResult(intent, CONNECTION_REQUEST);
    }

}
