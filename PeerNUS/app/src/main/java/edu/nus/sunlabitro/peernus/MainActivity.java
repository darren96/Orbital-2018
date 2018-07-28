package edu.nus.sunlabitro.peernus;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.Arrays;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        UpdateProfileFragment.OnFragmentInteractionListener,
        DisplayProfileFragment.OnFragmentInteractionListener,
        ProfilesListFragment.OnFragmentInteractionListener,
        OnTaskCompleted {

    private final String retrieveProfile = "202";

    private static String HOST;
    private static String PROFILE_DIR;
    private static String MATCHES_DIR;
    private String USER_PREF;

    private TextView mName;
    private TextView mEmail;
    private ImageView mProfilePic;
    private ActionBarDrawerToggle toggle;

    private SharedPreferences sharedPreferences;
    private boolean isRegistered;
    private int id;
    private String email;
    private String name;

    final long ONE_MEGABYTE = 2048 * 2048;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        USER_PREF = getString(R.string.USER_PREF);
        HOST = getString(R.string.HOST);
        PROFILE_DIR = getString(R.string.PROFILE_DIR);
        MATCHES_DIR = getString(R.string.MATCHES_DIR);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        mEmail = (TextView) headerView.findViewById(R.id.userEmail);
        mName = (TextView) headerView.findViewById(R.id.userName);
        mProfilePic = (ImageView) headerView.findViewById(R.id.profilePic);

        sharedPreferences = getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        retrieveProfile();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                super.onBackPressed();
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            Fragment fragment = new ProfilesListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putString("purpose", "getMatches");
            fragment.setArguments(bundle);

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.fragment_frame, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        } else if (id == R.id.nav_profile) {
            Fragment fragment = new UpdateProfileFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putBoolean("isRegistered", isRegistered);

            fragment.setArguments(bundle);

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.fragment_frame, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        } else if (id == R.id.nav_request) {
            Fragment fragment = new ProfilesListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putString("purpose", "getRequests");
            fragment.setArguments(bundle);

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.fragment_frame, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        } else if (id == R.id.nav_friend) {
            Fragment fragment = new ProfilesListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putString("purpose", "getFriends");
            fragment.setArguments(bundle);

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.fragment_frame, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        } else if (id == R.id.nav_chat) {

        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
            MainActivity.this.finish();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear().commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void retrieveProfile() {
        String REQ_TYPE = retrieveProfile;

        id = sharedPreferences.getInt("id", 0);
        if (id == 0) {
            email = sharedPreferences.getString("email", "");
            String jsonString = convertToJSON(REQ_TYPE);
            HttpAsyncTask task = new HttpAsyncTask(this);
            task.execute("https://" + HOST + "/" + PROFILE_DIR + "/retrieveProfile.php", jsonString, "POST",
                    REQ_TYPE);
        } else {
            isRegistered = true;
            generateUI();
        }
    }

    @Override
    public void onTaskCompleted(String response, String REQ_TYPE) {
        if (REQ_TYPE.equals(retrieveProfile)) {
            retrieveFromJSON(response, REQ_TYPE);
            generateUI();
        }
    }

    // Convert profile information to JSON string
    public String convertToJSON(String REQ_TYPE) {
        JSONStringer jsonText = new JSONStringer();
        try {

            jsonText.object();
            if (REQ_TYPE.equals(retrieveProfile)) {
                jsonText.key("nusnet");
                jsonText.value(email);
            }
            jsonText.endObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonText.toString();
    }

    // Retrieve profile information from JSON string
    public void retrieveFromJSON(String message, String REQ_TYPE) {
        try {

            if (REQ_TYPE.equals(retrieveProfile)) {
                JSONObject jsonObject = new JSONObject(message);
                id = jsonObject.getInt("id");

                if (id == 0) {
                    isRegistered = false;
                } else {

                    isRegistered = true;
                    name = jsonObject.getString("name");
                    String sex = jsonObject.getString("sex");
                    int matricYear = Integer.parseInt(jsonObject.getString("matricYear"));

                    JSONArray courseJsonArray = jsonObject.getJSONArray("course");
                    HashSet<String> selectedCourseSet = new HashSet<>();
                    for (int i = 0; i < courseJsonArray.length(); i++) {
                        selectedCourseSet.add(courseJsonArray.getString(i));
                    }

                    JSONArray courseIdJsonArray = jsonObject.getJSONArray("courseId");
                    HashSet<String> selectedCourseIdSet = new HashSet<>();
                    for (int i = 0; i < courseIdJsonArray.length(); i++) {
                        selectedCourseIdSet.add(String.valueOf(courseIdJsonArray.getInt(i)));
                    }

                    JSONArray moduleJsonArray = jsonObject.getJSONArray("modules");
                    HashSet<String> selectedModuleSet = new HashSet<>();
                    for (int i = 0; i < moduleJsonArray.length(); i++) {
                        selectedModuleSet.add(moduleJsonArray.get(i).toString());
                    }

                    String description = jsonObject.getString("description");
                    int isProfilePicSet = jsonObject.getInt("profilePic");

                    SharedPreferences.Editor editor =
                            sharedPreferences.edit();
                    editor.putInt("id", id);
                    editor.putString("name", name);
                    editor.putString("sex", sex);
                    editor.putInt("matricYear", matricYear);
                    editor.putStringSet("course", selectedCourseSet);
                    editor.putStringSet("courseId", selectedCourseIdSet);
                    editor.putStringSet("modules", selectedModuleSet);
                    editor.putString("description", description);
                    editor.putInt("isProfilePicSet", isProfilePicSet);
                    editor.apply();
                    editor.commit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateUI() {
        if(isRegistered) {

            if (sharedPreferences.getInt("isProfilePicSet", 0) > 0) {
                getProfileImage();
            }

            toggle.setDrawerIndicatorEnabled(true);

            email = sharedPreferences.getString("email", "");
            name = sharedPreferences.getString("name", "");

            mEmail.setText(email);
            mName.setText(name);

            String token = FirebaseInstanceId.getInstance().getToken();
            sendTokenToDatabase(token);

            Fragment fragment = new ProfilesListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putString("purpose", "getMatches");
            fragment.setArguments(bundle);

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.fragment_frame, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        } else {

            toggle.setDrawerIndicatorEnabled(false);

            Fragment fragment = new UpdateProfileFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();

            bundle.putBoolean("isRegistered", isRegistered);

            fragment.setArguments(bundle);

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.fragment_frame, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    }

    private void sendTokenToDatabase(String token) {
        int id = sharedPreferences.getInt("id", 0);
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users");

        User currUser = new User(id, name, email, token);

        databaseReference.child(String.valueOf(id)).setValue(currUser);
    }

    private Bitmap imageRounded(Bitmap bitmap) {
        Bitmap imageRounded = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(imageRounded);
        Paint mpaint = new Paint();
        mpaint.setAntiAlias(true);
        mpaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP));
        canvas.drawOval((new RectF(0, 0, bitmap.getWidth(),
                bitmap.getHeight())), mpaint);// Round Image Corner 100 100 100 100
        return imageRounded;
    }

    private void getProfileImage() {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        storageRef.child("images/" + id).getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Use the bytes to display the image
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("profilePic", Arrays.toString(bytes));
                        editor.commit();

                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        Bitmap imageRounded = imageRounded(bitmap);
                        mProfilePic.setImageBitmap(imageRounded);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
}
