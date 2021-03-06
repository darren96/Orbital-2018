package edu.nus.sunlabitro.peernus;


import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity
        implements OnTaskCompleted {

    private static final String TAG = "LoginActivity";
    private static final String retrieveLogin = "retrieveLogin";
    private static final String retrieveToken = "retrieveToken";
    private static final String firebaseLogin = "firebaseLogin";
    private static final String nusnetLogin = "nusnetLogin";
    private String loginMode = "";
    private static String USER_PREF;
    private static String HOST;
    private static String LOGIN_DIR;
    private static String FIREBASE_STATIC_HOST;

    private TextView mEmailTV;
    private TextView mPasswordTV;
    private EditText mEmail;
    private EditText mPassword;
    private Button mLoginBtn;
    private Button mSignUpBtn;
    private Button mEmailLoginBtn;
    private Button mNusnetLoginBtn;
    private Button mBackBtn;
    private FrameLayout mLoadingFrame;
    private ImageView mLoadingImageView;

    private FirebaseAuth mAuth;
    private String email;
    private String name;
    private String token;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        USER_PREF = getString(R.string.USER_PREF);
        HOST = getString(R.string.HOST);
        LOGIN_DIR = getString(R.string.LOGIN_DIR);
        FIREBASE_STATIC_HOST = getString(R.string.FIREBASE_ADMIN_HOST);

        mEmailTV = (TextView) findViewById(R.id.usernameTV);
        mPasswordTV = (TextView) findViewById(R.id.passwordTV);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mLoginBtn = (Button) findViewById(R.id.loginbtn);
        mSignUpBtn = (Button) findViewById(R.id.signUpBtn);
        mEmailLoginBtn = (Button) findViewById(R.id.emailLoginBtn);
        mNusnetLoginBtn = (Button) findViewById(R.id.nusnetLoginBtn);
        mBackBtn = (Button) findViewById(R.id.backBtn);
        mLoadingFrame = (FrameLayout) findViewById(R.id.loadingFrame);
        mLoadingImageView = (ImageView) findViewById(R.id.loadingImageView);

        mAuth = FirebaseAuth.getInstance();


        mEmailTV.setVisibility(View.GONE);
        mEmail.setVisibility(View.GONE);
        mPasswordTV.setVisibility(View.GONE);
        mPassword.setVisibility(View.GONE);
        mSignUpBtn.setVisibility(View.GONE);
        mLoginBtn.setVisibility(View.GONE);
        mBackBtn.setVisibility(View.GONE);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if (loginMode.equals(nusnetLogin)) {
                    openChromeTab();
                } else {
                    loginUser(email, password);
                }
            }
        });

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                registerUser(email, password);
            }
        });

        mEmailLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginMode = firebaseLogin;
                mEmailTV.setVisibility(View.VISIBLE);
                mEmailTV.setText("Email: ");
                mEmail.setHint("johndoe@mail.com");
                mEmail.setVisibility(View.VISIBLE);
                mPasswordTV.setVisibility(View.VISIBLE);
                mPassword.setVisibility(View.VISIBLE);
                mSignUpBtn.setVisibility(View.VISIBLE);
                mLoginBtn.setVisibility(View.VISIBLE);
                mEmailLoginBtn.setVisibility(View.GONE);
                mNusnetLoginBtn.setVisibility(View.GONE);
                mBackBtn.setVisibility(View.VISIBLE);
            }
        });

        mNusnetLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginMode = nusnetLogin;
                mEmailTV.setVisibility(View.VISIBLE);
                mEmailTV.setText("NUSNET ID: ");
                mEmail.setHint("e0123456");
                mEmail.setVisibility(View.VISIBLE);
                mPasswordTV.setVisibility(View.GONE);
                mPassword.setVisibility(View.GONE);
                mSignUpBtn.setVisibility(View.GONE);
                mLoginBtn.setVisibility(View.VISIBLE);
                mEmailLoginBtn.setVisibility(View.GONE);
                mNusnetLoginBtn.setVisibility(View.GONE);
                mBackBtn.setVisibility(View.VISIBLE);
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmailTV.setVisibility(View.GONE);
                mEmail.setVisibility(View.GONE);
                mPasswordTV.setVisibility(View.GONE);
                mPassword.setVisibility(View.GONE);
                mSignUpBtn.setVisibility(View.GONE);
                mLoginBtn.setVisibility(View.GONE);
                mNusnetLoginBtn.setVisibility(View.VISIBLE);
                mBackBtn.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            loginMode = firebaseLogin;
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            loginMode = firebaseLogin;
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void updateUI(FirebaseUser user) {
        mLoadingFrame.setVisibility(View.GONE);
        Intent intent = null;

        if (user != null) {
            SharedPreferences.Editor editor =
                    getSharedPreferences(USER_PREF, MODE_PRIVATE).edit();

            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                email = user.getUid();
                Log.d(TAG, email);
            } else {
                email = user.getEmail();
            }

            editor.putString("email", email);
            if (loginMode.equals(nusnetLogin)) {
                editor.putString("name", name);
            }
            editor.apply();
            editor.commit();
            intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void openChromeTab() {
        email = mEmail.getText().toString().toLowerCase();
        String nusnet_pattern = "[A,E,a,e]{1}[0-9]{7}$";
        Pattern pattern = Pattern.compile(nusnet_pattern);
        Matcher matcher = pattern.matcher(email);

        if (email == null && email.length() == 0) {
            mEmail.setError("Please fill in your NUSNet ID.");
            return;
        } else if (!matcher.matches()) {
            mEmail.setError("Invalid NUSNet");
        } else {

            final String url = "https://peernus.000webhostapp.com/openid.php?nusnet_id=" + email;
            CustomTabsServiceConnection connection = new CustomTabsServiceConnection() {
                @Override
                public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient client) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                    CustomTabsIntent intent = builder.build();
                    client.warmup(0L);
                    intent.launchUrl(LoginActivity.this, Uri.parse(url));
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };

            CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", connection);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (email != null) {
            mLoadingFrame.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(R.drawable.loading)
                    .into(mLoadingImageView);

            String REQ_TYPE = retrieveLogin;

            String jsonString = convertToJSON(REQ_TYPE);
            HttpAsyncTask task = new HttpAsyncTask(this);
            task.execute("https://" + HOST + "/" + LOGIN_DIR + "/retrieveLogin.php", jsonString, "POST",
                    REQ_TYPE);

            REQ_TYPE = retrieveToken;
            jsonString = convertToJSON(REQ_TYPE);
            task = new HttpAsyncTask(this);
            task.execute("https://" + FIREBASE_STATIC_HOST + "/createToken", jsonString, "POST", REQ_TYPE);

        }
    }

    @Override
    public void onTaskCompleted(String response, String REQ_TYPE) {
        retrieveFromJson(response, REQ_TYPE);

        if (REQ_TYPE != null) {
            if (REQ_TYPE.equals(retrieveToken) && status.equals("OK")) {
                mAuth.signInWithCustomToken(token)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(LoginActivity.this, "Login successfully", Toast.LENGTH_LONG).show();
                                    updateUI(user);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                                    mLoadingFrame.setVisibility(View.GONE);
                                }
                            }
                        });
            } else if (status.equals("NOK")) {
                mLoadingFrame.setVisibility(View.GONE);
            }
        }
    }

    private String convertToJSON(String REQ_TYPE) {
        JSONStringer jsonText = new JSONStringer();
        try {
            jsonText.object();
            jsonText.key("nusnet");
            jsonText.value(email);
            jsonText.endObject();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonText.toString();
    }

    private void retrieveFromJson(String message, String REQ_TYPE) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(message);
            if (REQ_TYPE.equals(retrieveLogin)) {
                if (jsonObject.has("name")) {
                    name = jsonObject.getString("name");
                }
                status = jsonObject.getString("status");
            } else if (REQ_TYPE.equals(retrieveToken)) {
                token = jsonObject.getString("token");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
