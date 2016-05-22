package com.sdw.ivana.inertiavectorrace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DummyActivity extends AppCompatActivity {
    private TextView loginType, email, nickName, persistent, firstTime;
    private Button signOut, exit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        //Initial tasks
        bindViews();
        updateViews(getIntent().getExtras());
        setListeners();
        updatePrefs();


    }

    private void bindViews() {
        loginType = (TextView) findViewById(R.id.loginType);
        email = (TextView) findViewById(R.id.logedEmail);
        nickName = (TextView) findViewById(R.id.loggedNick);
        persistent = (TextView) findViewById(R.id.persistent);
        signOut = (Button) findViewById(R.id.signOut);
        exit = (Button) findViewById(R.id.exit);
        firstTime = (TextView) findViewById(R.id.firsTime);
    }

    private void updateViews(Bundle extras) {
        loginType.setText("Login type: " + extras.getString("social_account", "Inertia Account"));
        email.setText("Email: " + extras.getString("email"));
        nickName.setText("Nickname: " + extras.getString("nick_name"));
        firstTime.setText("First time: " + extras.getBoolean("first_time"));
        persistent.setText("Persistent" + extras.getBoolean("persisted"));
    }

    private void setListeners() {
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signOut = new Intent(getBaseContext(), LoginActivity.class);
                signOut.putExtra("to_log_out", true);
                startActivity(signOut);
                finish();
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                finish();
            }
        });
    }

    private void updatePrefs() {
        SharedPreferences.Editor spEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        spEditor.putInt("sign_in_type", Defaults.INERTIA_SIGNIN);
        spEditor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        finish();
    }
}
