package com.utsanonymous.profbotandroidopentok;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.utsanonymous.profbotandroidopentok.LoginActivity;
import com.utsanonymous.profbotandroidopentok.PubNubMain;
import com.utsanonymous.profbotandroidopentok.R;
import com.utsanonymous.profbotandroidopentok.adt.BotPresence;
import com.utsanonymous.profbotandroidopentok.adt.BotPresenceAdapter;
import com.utsanonymous.profbotandroidopentok.presence_popup_window;
import com.utsanonymous.profbotandroidopentok.util.Constants;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Main Activity";
    private PubNubMain mPubNub;
    private ListView mListView;
    private SharedPreferences mSharedPrefs;
    ArrayList<BotPresence> botList = new ArrayList<>();
    private String username;

    /**
     * Oncreate
     * @note function will be called when this activity starts
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get shared preference and check is there is a username,if not then go back
        //to the login page
        mSharedPrefs = getSharedPreferences(Constants.CHAT_PREFS, MODE_PRIVATE);
        if(!mSharedPrefs.contains(Constants.CHAT_USERNAME)) {
            Intent toLogin = new Intent(this, LoginActivity.class);
            startActivity(toLogin);
            return;
        }

        //get username from the shared preference
        this.username = mSharedPrefs.getString(Constants.CHAT_USERNAME, "Anonymous");

        mPubNub = new PubNubMain(getApplicationContext(),this.username,MainActivity.this,Constants.LOBBY_CHANNEL);

        //TEMPORARY ARRAY USAGE -------------------//
        for (int i = 1; i < 10; i++){
            BotPresence bot1 = new BotPresence("Sanbot Elf "+ i,"BLD11.10.403",Constants.ROBOT_AVAILABLE);
            botList.add(bot1);
        }
        BotPresence bot1 = new BotPresence("Sanbot Elf Special Edition 5","BLD11.10.403","User: Nic");
        botList.add(bot1);

        mListView = findViewById(R.id.presence_list);
        BotPresenceAdapter botAdapter = new BotPresenceAdapter(this,R.layout.adapter_view,botList);
        mListView.setAdapter(botAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BotPresence robot = (BotPresence) parent.getItemAtPosition(position);
                if(robot.getUser().equals(Constants.ROBOT_AVAILABLE)) {
                    Intent intent = new Intent(MainActivity.this, presence_popup_window.class);
                    intent.putExtra(Constants.ROBOT_NAME_KEY,robot.getBotModel());
                    intent.putExtra(Constants.ROBOT_LOCATION_KEY,robot.getAssignedRoom());
                    intent.putExtra(Constants.CHAT_USERNAME,username);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), robot.getBotModel()+" in room " + robot.getAssignedRoom() + " is unavailable", Toast.LENGTH_LONG).show();
                }
            }
        });

        mPubNub.initPubNub();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_sign_out:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * onStop
     * @note Pubnub will unsubscribe from all of the available channel when the app is not
     * in the foreground
     */
    @Override
    public void onStop() {
        super.onStop();
        if(this.mPubNub != null)
            this.mPubNub.unsubscribeAll();
    }

    /**
     * onRestart
     * @note Instantiate Pubnub object if it is null. Subscribe to the lobby channel
     */

    @Override
    public void onRestart(){
        super.onRestart();
        if (this.mPubNub==null) {
            this.mPubNub.initPubNub();
            this.mPubNub.subscribeWithPresence();
        } else {
            this.mPubNub.subscribeWithPresence();
        }
    }

    /**
     * signOut
     * @note this functions is one of the option under menu for users to signout from the
     * application
     */
    public void signOut(){
        this.mPubNub.unsubscribeAll();
        SharedPreferences.Editor edit = mSharedPrefs.edit();
        edit.remove(Constants.CHAT_USERNAME);
        edit.apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("oldUsername",this.username);
        startActivity(intent);
    }

}
