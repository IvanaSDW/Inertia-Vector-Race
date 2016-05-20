package com.sdw.ivana.inertiavectorrace;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

public class DummyActivity extends AppCompatActivity {
    private TextView loginType, email, nickName, persistent, firstTime;
    private Button signOut, exit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);
        bindViews();
        Bundle extras = getIntent().getExtras();

        updateViews(extras);

    }

    private void updateViews(Bundle extras) {
        loginType.setText("Login type: " + extras.getString("social_account", "Inertia Account"));
        email.setText("Email: " + extras.getString("email"));
        nickName.setText("Nickname: " + extras.getString("nick_name"));
        firstTime.setText("First time: " + extras.getBoolean("first_time"));
        persistent.setText("Persistent" + extras.getBoolean("persistent"));
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
}
