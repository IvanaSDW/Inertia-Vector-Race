package com.sdw.ivana.inertiavectorrace;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserIdStorageFactory;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private UserSignUpTask mSignUpTask = null;
    private UserV1 loggedUser = null;
    private boolean loggedIn = false;

    // UI references.
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilNickname;
    private AutoCompleteTextView mEmailView;
    private TextInputEditText mPasswordView;
    private TextInputEditText mNickView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    private String email;
    private String password;
    private String nickname;
    private TextView promptSignUp;
    private String signUpError;
    private boolean toSignOut = false;
    private int lobbyId = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            if (extras.containsKey("to_log_out")) toSignOut = extras.getBoolean("to_log_out");
//            if (extras.containsKey("lobby_id")) lobbyId = extras.getInt("lobby_id");
        }

        //Init main UI views
        mLoginFormView = findViewById(R.id.login_form); //ScrollVIew that contains form
        mProgressView = findViewById(R.id.login_progress);

        //Set Backendless
        Backendless.initApp(this, Defaults.APPLICATION_ID, Defaults.SECRET_KEY, Defaults.VERSION);

        //Check if a valid login still exists
        final AsyncCallback<Boolean> isValidLoginCallBack = new AsyncCallback<Boolean>() {

            @Override
            public void handleResponse(Boolean response) {
                Log.i("Login Validation", response.toString());
                if (response){
                    String userObjectId = UserIdStorageFactory.instance().getStorage().get();
                    if (!userObjectId.isEmpty()){
                        Backendless.Data.of(BackendlessUser.class).findById(userObjectId, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser response) {
                                Log.i("Login Validation", "User " + response.getEmail() + " logged in automatically");
                                loggedUser = new UserV1(response.getEmail(), response.getProperty("nickName").toString(),
                                        null, false);
                                if (toSignOut) signOutInertiaUser();
                                else enterGame(loggedUser, true);
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Log.i("Login Validation", fault.getMessage());
                            }
                        });
                    }
                }
                showProgress(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.i("Login Validation", fault.getMessage());
                showProgress(false);
            }
        };
        showProgress(true);
        Backendless.UserService.isValidLogin(isValidLoginCallBack);

        initUI();

        promptSignUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (promptSignUp.getText() == getString(R.string.prompt_for_new_account)){
                    promptSignUp.setText(R.string.already_a_user);
                    tilNickname.setVisibility(View.VISIBLE);
                    mEmailSignInButton.setText(R.string.action_sign_up);
                } else {
                    promptSignUp.setText(R.string.prompt_for_new_account);
                    mEmailSignInButton.setText(R.string.action_sign_in);
                    tilNickname.setVisibility(View.GONE);
                }
            }
        });

//        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) { //Listen if user push "Send" in keyboard instead of SignIn Button
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(mEmailSignInButton.getText().toString());
            }
        });

    }

    private void signOutInertiaUser() {
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                Log.i("Logout", "succesfully logged out");
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                editor.putInt("sign_in_type", Defaults.SIGNED_OUT);
                editor.apply();
                Intent lobby = new Intent(getBaseContext(), LobbyActivity.class);
                lobby.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(lobby);
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.i("Logout", fault.getMessage());
                Intent lobby = new Intent(getBaseContext(), LobbyActivity.class);
                lobby.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(lobby);
                finish();
            }
        });
    }

    private void initUI() {
        //Set Window Size
        Point lowerBottomPoint = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(lowerBottomPoint);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = Math.min(600, lowerBottomPoint.x);
        params.height = 320;
        getWindow().setAttributes(params);

        //Set Custom fonts
        Typeface typeface = Typeface.createFromAsset(getAssets(), getString(R.string.font_file_string));

        // Set up the login form.
        tilEmail = (TextInputLayout) findViewById(R.id.tilEmail);
        tilPassword = (TextInputLayout) findViewById(R.id.tilPassword);
        tilNickname = (TextInputLayout) findViewById(R.id.tilNick);
        promptSignUp = (TextView) findViewById(R.id.new_account_prompt);
        promptSignUp.setTypeface(typeface);
        tilEmail.setTypeface(typeface);
        tilPassword.setTypeface(typeface);
        tilNickname.setTypeface(typeface);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mEmailView.setTypeface(typeface);
        populateAutoComplete();

        mPasswordView = (TextInputEditText) findViewById(R.id.password);
        mPasswordView.setTypeface(typeface);

        mNickView = (TextInputEditText) findViewById(R.id.nickName);
        mNickView.setTypeface(typeface);

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setTypeface(typeface);

    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }


    //Check whether have access to contacts service
    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(String action) {
        if (mAuthTask != null || mSignUpTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNickView.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();
        if (tilNickname.getVisibility() == View.VISIBLE) nickname = mNickView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        //Check for a valid nickname
        if (tilNickname.getVisibility() == View.VISIBLE && TextUtils.isEmpty(nickname)) {
            mNickView.setError(getString(R.string.error_field_required));
            focusView = mNickView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if (action.equals(getString(R.string.action_sign_in))){
                mAuthTask = new UserLoginTask(email, password);
                mAuthTask.execute((Void) null);
            } else {
                mSignUpTask = new UserSignUpTask(email, password, nickname);
                mSignUpTask.execute((Void) null);
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    public void enterGame(UserV1 user, boolean persisted){
        Intent enterGame = new Intent(this, DummyActivity.class);
        enterGame.putExtra("email", user.getEmail());
        enterGame.putExtra("nick_name", user.getNickName());
        enterGame.putExtra("social_account", user.getSocialAccount());
        enterGame.putExtra("first_time", user.isFirstTime());
        enterGame.putExtra("persisted", persisted);
        startActivity(enterGame);
        //if (lobbyId != 9999) android.os.Process.killProcess(lobbyId);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try
            {
                BackendlessUser user = Backendless.UserService.login( mEmail, mPassword, true );
                Log.i( "LogIn", user.getEmail() + " succesfully signed in" );
                loggedUser = new UserV1(user.getEmail(), user.getProperty("nickName").toString(),
                        null, false);
                loggedIn = true;
            }
            catch( BackendlessException exception )
            {
                Log.i( "LogIn", " error signin in" + exception.getCode() + " " + exception.getDetail() + exception.getMessage() );
                loggedIn = false;
            }

            return loggedIn;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                enterGame(loggedUser, false);
            } else {
                mEmailView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Asynchronous registration task used to sign up
     * the user.
     */
    public class UserSignUpTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mNick;

        UserSignUpTask(String email, String password, String nick) {
            mEmail = email;
            mPassword = password;
            mNick = nick;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            showProgress(true);

            // Register the new account here.
            BackendlessUser user = new BackendlessUser();
            user.setEmail(mEmail);
            user.setPassword(mPassword);
            user.setProperty("nickName", mNick);
            try
            {
                user = Backendless.UserService.register( user );
                Log.i( "Registration", user.getEmail() + " successfully registered" );
                return true;
            }
            catch( BackendlessException exception )
            {
                Log.i( "SignUp ", exception.getMessage() );
                signUpError = exception.getMessage();
                return false;
                // an error has occurred, the error code can be retrieved with fuault.getCode()
            }

        }

        @Override
        protected void onPostExecute(final Boolean signUpSuccess) {
            mSignUpTask = null;
            showProgress(false);

            if (signUpSuccess) {
                loggedUser = new UserV1(mEmail, mNick, null, true);
                finish();
                enterGame(loggedUser, false);
            } else {
                mEmailView.requestFocus();
                mEmailView.setError(signUpError);
            }
        }

        @Override
        protected void onCancelled() {
            mSignUpTask = null;
            showProgress(false);
        }
    }
}

