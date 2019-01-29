package com.utsanonymous.profbotandroidopentok;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.utsanonymous.profbotandroidopentok.MainActivity;
import com.utsanonymous.profbotandroidopentok.util.Constants;

public class LoginActivity extends AppCompatActivity {

    public EditText mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = (EditText) findViewById(R.id.login_username);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            String lastUsername = extras.getString("oldUsername", "");
            mUsername.setText(lastUsername);
        }
    }

    public void joinChannel(View view) {
        String username = mUsername.getText().toString();
        if(!validUsername(username)) return;

        SharedPreferences sp = getSharedPreferences(Constants.CHAT_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(Constants.CHAT_USERNAME, username);
        edit.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private boolean validUsername(String username) {
        if (username.length() == 0) {
            mUsername.setError("Username cannot be empty.");
            return false;
        }
        if (username.length() > 16) {
            mUsername.setError("Username too long.");
            return false;
        }
        if (username == "Name"){
            mUsername.setError("Please enter your name");
            return false;
        }
        return true;
    }

}
