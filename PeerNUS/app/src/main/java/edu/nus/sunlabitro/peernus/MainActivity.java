package edu.nus.sunlabitro.peernus;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        UpdateProfileFragment.OnFragmentInteractionListener,
        OnTaskCompleted {

    private TextView mName;
    private TextView mEmail;
    private ImageView mProfilePic;
    private ListView mMatchesListView;

    private String USER_PREF;
    private final String getMatches = "201";
    private final String retrieveProfile = "202";

    private static String HOST;
    private static String NUSMOD_HOST;
    private static String MOD_DIR;
    private static String PROFILE_DIR;
    private static String COURSES_DIR;
    private static String MATCHES_DIR;

    private String email;

    private JSONArray matches;
    private ArrayList<Profile> matchesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        mEmail = (TextView) headerView.findViewById(R.id.userEmail);
        mName = (TextView) headerView.findViewById(R.id.userName);
        mProfilePic = (ImageView) headerView.findViewById(R.id.profilePic);

        mMatchesListView = (ListView) findViewById(R.id.matchesList);

        USER_PREF = getString(R.string.USER_PREF);
        HOST = getString(R.string.HOST);
        PROFILE_DIR = getString(R.string.PROFILE_DIR);
        COURSES_DIR = getString(R.string.COURSES_DIR);
        NUSMOD_HOST = getString(R.string.NUSMOD_HOST);
        MOD_DIR = getString(R.string.MOD_DIR);
        MATCHES_DIR = getString(R.string.MATCHES_DIR);

        Boolean isRegistered = getIntent().getExtras().getBoolean("isRegistered");

        email = getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
                .getString("email", "");
        mEmail.setText(email);

        if (!isRegistered) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            Fragment fragment = new UpdateProfileFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.fragment_frame, fragment);
            transaction.addToBackStack(null);

            drawer.setVisibility(View.INVISIBLE);

            // Commit the transaction
            transaction.commit();
        } else {
            retrieveProfile();
            getMatches();
//            mName.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        } else if (id == R.id.nav_profile) {
            Fragment fragment = new UpdateProfileFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.fragment_frame, fragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        } else if (id == R.id.nav_chat) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
            MainActivity.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void getMatches() {
        String REQ_TYPE = getMatches;
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+MATCHES_DIR+"/retrieveMatches.php", jsonString, "POST",
                REQ_TYPE);
    }

    private void retrieveProfile() {
        String REQ_TYPE = retrieveProfile;
        email = getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
                .getString("email", "");
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+PROFILE_DIR+"/retrieveProfile.php", jsonString, "POST",
                REQ_TYPE);
    }

    @Override
    public void onTaskCompleted(String response, String REQ_TYPE) {
        if (REQ_TYPE.equals(getMatches)) {
            retrieveFromJSON(response, REQ_TYPE);
            MatchesListAdapter matchesListAdapter =
                    new MatchesListAdapter(this, generateData());
            mMatchesListView.setAdapter(matchesListAdapter);
            mMatchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Fragment fragment = new DisplayProfileFragment();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack
                    Bundle args = new Bundle();
                    TextView nusnetTV = (TextView) view.findViewById(R.id.nusnet);
                    String nusnet = nusnetTV.getText().toString();
                    args.putString("nusnet", nusnet);
                    fragment.setArguments(args);
                    transaction.replace(R.id.fragment_frame, fragment);
                    transaction.addToBackStack(null);
                }
            });
        } else if (REQ_TYPE.equals(retrieveProfile)) {
            retrieveFromJSON(response, REQ_TYPE);
        }
    }

    // Convert profile information to JSON string
    public String convertToJSON(String REQ_TYPE) {
        JSONStringer jsonText = new JSONStringer();
        try {

            jsonText.object();
            if (REQ_TYPE.equals(getMatches)) {
                jsonText.key("course");
                Set<String> course = getSharedPreferences(USER_PREF, MODE_PRIVATE)
                        .getStringSet("course", null);
                jsonText.array();
                HashSet courseHashSet = new HashSet(course);
                Iterator<String> courseIterator = courseHashSet.iterator();
                while (courseIterator.hasNext()) {
                    jsonText.value(courseIterator.next());
                }
                jsonText.endArray();
            } else if (REQ_TYPE.equals(retrieveProfile)) {
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

            if (REQ_TYPE.equals(getMatches)) {
                JSONObject jsonObject = new JSONObject(message);
                matches = jsonObject.getJSONArray("results");
                Log.d("JSON Courses", matches.toString());
            } else if (REQ_TYPE.equals(retrieveProfile)) {
                JSONObject jsonObject = new JSONObject(message);
                int id = jsonObject.getInt("id");

                String name = jsonObject.getString("name");
                String sex = jsonObject.getString("sex");
                int matricYear = Integer.parseInt(jsonObject.getString("matricYear"));
                JSONArray courseJsonArray = jsonObject.getJSONArray("course");
                HashSet<String> selectedCourseSet = new HashSet<>();
                for (int i = 0; i < courseJsonArray.length(); i++) {
                    selectedCourseSet.add(courseJsonArray.getString(i));
                }

                JSONArray moduleJsonArray = jsonObject.getJSONArray("modules");
                HashSet<String> selectedModuleSet = new HashSet<>();
                for (int i = 0; i < moduleJsonArray.length(); i++) {
                    selectedModuleSet.add(moduleJsonArray.get(i).toString());
                }

                String description = jsonObject.getString("description");

                SharedPreferences.Editor editor =
                        getSharedPreferences(USER_PREF, MODE_PRIVATE).edit();
                editor.putInt("id", id);
                editor.putString("name", name);
                editor.putString("sex", sex);
                editor.putInt("matricYear", matricYear);
                editor.putStringSet("course", selectedCourseSet);
                editor.putStringSet("modules", selectedModuleSet);
                editor.putString("description", description);
                editor.apply();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Profile> generateData(){
        ArrayList<Profile> profileArrayList = new ArrayList<>();
        JSONObject profileObj = null;
        int id;
        String name;
        String nusnet;
        int matricYear;
        ArrayList<String> course = new ArrayList<>();
        for (int i = 0; i < matches.length(); i++) {
            try {
                profileObj = matches.getJSONObject(i);
                id = profileObj.getInt("id");
                name = profileObj.getString("name");
                nusnet = profileObj.getString("nusnet");
                matricYear = profileObj.getInt("matricYear");
                JSONArray courseJSONArray = profileObj.getJSONArray("course");
                for (int j = 0; j < courseJSONArray.length(); j++) {
                    course.add(courseJSONArray.get(j).toString());
                }
                profileArrayList.add(new Profile (id, name, nusnet, matricYear, course));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return profileArrayList;
    }
}
