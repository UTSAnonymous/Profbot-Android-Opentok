package com.utsanonymous.profbotandroidopentok;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.utsanonymous.profbotandroidopentok.opentok.OpenTokConfig;
import com.utsanonymous.profbotandroidopentok.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.utsanonymous.profbotandroidopentok.R.drawable.mic_on;

public class CallActivity extends AppCompatActivity
        implements
        EasyPermissions.PermissionCallbacks,
        Session.SessionListener,
        PublisherKit.PublisherListener,
        SubscriberKit.SubscriberListener{

    private static final String LOG_TAG = "CallActivity";

    private static final int RC_VIDEO_APP_PERM = 124;

    //Pubnub
    protected PubNubMain mPubNub;
    private String username;

    //Opentok API
    private Session mSession;
    private PublisherKit mPublisher;
    private Subscriber mSubscriber;

    //Opentok api view
    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;

    public Button mButtonUp;
    public Button mButtonLeft;
    public Button mButtonRight;
    public Button mButtonDown;
    public Button mButtonStop;
    public Button mButtonDisconnect;
    public Button mButtonMic;
    public Button mButtonVideo;
    Boolean micOn, videoOn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_call);

        //Initialize view
        mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);

        setupListners();

        final Intent intent = getIntent();
        String roomId = intent.getStringExtra(Constants.PRIVATE_CHANNEL_KEY);
        this.username = intent.getStringExtra(Constants.CHAT_USERNAME);
        Log.d(LOG_TAG, "Room ID: " + roomId);
        if (roomId == null || roomId.length() == 0) {
            logAndToast(getString(R.string.missing_url));
            Log.e(LOG_TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        requestPermission();

        mPubNub = new PubNubMain(getApplicationContext(),this.username,CallActivity.this,roomId);
        mPubNub.initPubNub();
    }

    /**
     * Override android functions and button listeners
     */
    //=========================================================================================//
    //=========================================================================================//

    @SuppressLint("ClickableViewAccessibility")
    private void setupListners() {

        micOn = true;
        videoOn = true;

        mButtonUp = (Button) findViewById(R.id.top_button);
        mButtonRight = (Button) findViewById(R.id.right_button);
        mButtonLeft = (Button) findViewById(R.id.left_button);
        mButtonDown = (Button) findViewById(R.id.down_button);
        mButtonStop = (Button) findViewById(R.id.emergency_stop);
        mButtonDisconnect = (Button) findViewById(R.id.session_disconnect);
        mButtonMic = (Button) findViewById(R.id.mic);
        mButtonVideo = (Button) findViewById(R.id.video);

        mButtonUp.setOnTouchListener(new MyTouchListner(Constants.JSON_FORWARD));
        mButtonRight.setOnTouchListener(new MyTouchListner(Constants.JSON_RIGHT));
        mButtonLeft.setOnTouchListener(new MyTouchListner(Constants.JSON_LEFT));
        mButtonDown.setOnTouchListener(new MyTouchListner(Constants.JSON_BACKWARD));
        mButtonStop.setOnClickListener(view -> stopButton());
        mButtonDisconnect.setOnClickListener(view -> disconnectButton());
        mButtonMic.setOnClickListener(view -> muteMic());
        mButtonVideo.setOnClickListener(view -> videoSetting());
    }

    private void disconnectButton(){
        if(mSession != null){
            mSession.disconnect();
            finish();
        }
    }

    private void muteMic(){
        //turn off mic
        if(micOn){
            if(mPublisher != null){
                mPublisher.setPublishAudio(false);
                micOn = false;
                mButtonMic.setBackground(getResources().getDrawable(R.drawable.mic_off));
            }
        }
        //turn on mic
        else{
            if(mPublisher != null){
                mPublisher.setPublishAudio(true);
                micOn = true;
                mButtonMic.setBackground(getResources().getDrawable(R.drawable.mic_on));
            }
        }
    }

    private void videoSetting(){
        //turn off video
        if(videoOn){
            if(mPublisher != null){
                mPublisher.setPublishVideo(false);
                videoOn = false;
                mButtonVideo.setBackground(getResources().getDrawable(R.drawable.video_off));
            }
        }
        //turn on video
        else{
            if(mPublisher != null){
                mPublisher.setPublishVideo(true);
                videoOn = true;
                mButtonVideo.setBackground(getResources().getDrawable(R.drawable.video_on));
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if(mSession != null){
            mSession.onResume();
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if(mSession != null){
            mSession.onPause();
        }
    }

    /**
     * EasyPermission listener methods and functions
     */
    //=========================================================================================//
    //=========================================================================================//

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    public void requestPermission(){

        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA};
        if(EasyPermissions.hasPermissions(this,perms)){
            initializeSession();
            initializePublisher();
        }
        else{
            EasyPermissions.requestPermissions(this,getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    /**
     * Session listener methods and functions
     */
    //=========================================================================================//
    //=========================================================================================//

    public void initializeSession(){
        mSession = new Session.Builder(this, OpenTokConfig.API_KEY, OpenTokConfig.SESSION_ID).build();
        mSession.setSessionListener(this);
        mSession.connect(OpenTokConfig.TOKEN);
    }

    public void initializePublisher(){
        mPublisher = new Publisher.Builder(this)
                .audioTrack(true)
                .frameRate(Publisher.CameraCaptureFrameRate.FPS_30)
                .resolution(Publisher.CameraCaptureResolution.LOW)
                .build();
        mPublisher.setPublisherListener(this);
        //mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FIT);
        mPublisherViewContainer.addView(mPublisher.getView());
    }

    @Override
    public void onConnected(Session session) {
        logAndToast("Session connected :" + session.getSessionId());

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        logAndToast("Session disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        if(mSubscriber == null){
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FIT);
            mSubscriber.setPreferredFrameRate((float) 25);
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        log("Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        logAndToast("Opentok Session connection error : " + opentokError.getMessage());
    }

    /**
     * Publisher listener methods
     */
    //=========================================================================================//
    //=========================================================================================//

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        log("onStreamConnected :" + publisherKit.getSession());
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        log("onStreamDestroyed :" + publisherKit.getSession());
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        logAndToast("Publishing error :" + opentokError.getMessage());
    }

    /**
     * Subscriber listener methods
     */
    //=========================================================================================//
    //=========================================================================================//

    @Override
    public void onConnected(SubscriberKit subscriberKit) {

    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {

    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {

    }

    /**
     * Publishing Pubnub control data
     */
    //=========================================================================================//
    //=========================================================================================//

    public class MyTouchListner implements View.OnTouchListener {

        private Handler mHandler;
        private String button_lable;

        public MyTouchListner(String button_lable){
            this.button_lable = button_lable;
        }

        @Override
        public boolean onTouch(View v, android.view.MotionEvent event) {
            switch(event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    if (mHandler != null) return true;
                    mHandler = new Handler();
                    mHandler.postDelayed(mAction, 100);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                    if (mHandler == null) return true;
                    mHandler.postDelayed(mAction, 100);
                    mHandler.removeCallbacks(mAction);
                    mHandler = null;
                    for(int i = 0; i<5; i++){ stopRobot(); }
                    System.out.println("Stopping action...");
                    break;
            }
            return false;
        }

        Runnable mAction = new Runnable() {
            @Override public void run() {
                switch(button_lable){
                    case Constants.JSON_FORWARD:
                        forwardButton();
                        break;
                    case Constants.JSON_BACKWARD:
                        backwardButton();
                        break;
                    case Constants.JSON_LEFT:
                        rotateLeftButton();
                        break;
                    case Constants.JSON_RIGHT:
                        rotateRightButton();
                        break;
                }
                System.out.println("Performing action...");
                mHandler.postDelayed(this, 100);
            }
        };
    }

    public void forwardButton() {
        JSONObject data = new JSONObject();
        try{
            data.put(Constants.JSON_USER,this.username);
            data.put(Constants.JSON_MESSAGE,Constants.JSON_FORWARD);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        mPubNub.publish(data);
    }

    public void backwardButton() {
        JSONObject data = new JSONObject();
        try{
            data.put(Constants.JSON_USER,this.username);
            data.put(Constants.JSON_MESSAGE,Constants.JSON_BACKWARD);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        mPubNub.publish(data);
    }

    public void rotateLeftButton() {
        JSONObject data = new JSONObject();
        try{
            data.put(Constants.JSON_USER,this.username);
            data.put(Constants.JSON_MESSAGE,Constants.JSON_LEFT);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        mPubNub.publish(data);
    }

    public void rotateRightButton() {
        JSONObject data = new JSONObject();
        try{
            data.put(Constants.JSON_USER,this.username);
            data.put(Constants.JSON_MESSAGE,Constants.JSON_RIGHT);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        mPubNub.publish(data);
    }

    public void stopRobot(){
        JSONObject data = new JSONObject();
        try{
            data.put(Constants.JSON_USER,this.username);
            data.put(Constants.JSON_MESSAGE,Constants.JSON_STOP);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        mPubNub.publish(data);
    }

    private void stopButton() {
        stopRobot();
    }

    /**
     * Util
     */
    //=========================================================================================//
    //=========================================================================================//

    public void logAndToast(String msg){
        Log.i(LOG_TAG,msg);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void log(String msg){
        Log.i(LOG_TAG,msg);
    }
}
