package edu.nus.sunlabitro.peernus;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfilesListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfilesListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfilesListFragment extends Fragment
        implements OnTaskCompleted {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "purpose";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String purpose;
    private String mParam2;

    private ListView mMatchesListView;
    private TextView mNoDataTextView;

    private String USER_PREF;
    private final String getMatches = "getMatches";
    private final String retrieveRequests = "getRequests";
    private final String retrieveFriends = "getFriends";

    private static String HOST;
    private static String MATCHES_DIR;
    private static String REQUEST_DIR;
    private static String FRIENDS_DIR;

    private int id;
    private String email;

    private JSONArray profiles;

    private OnFragmentInteractionListener mListener;

    public ProfilesListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MatchesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfilesListFragment newInstance(String param1, String param2) {
        ProfilesListFragment fragment = new ProfilesListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            purpose = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profiles_list, container, false);
        mMatchesListView = (ListView) view.findViewById(R.id.matchesList);
        mNoDataTextView = (TextView) view.findViewById(R.id.noData);

        USER_PREF = getString(R.string.USER_PREF);
        HOST = getString(R.string.HOST);
        MATCHES_DIR = getString(R.string.MATCHES_DIR);
        REQUEST_DIR = getString(R.string.REQUEST_DIR);
        FRIENDS_DIR = getString(R.string.FRIENDS_DIR);

        if (purpose.equals(getMatches)) {
            getMatches();
        } else if (purpose.equals(retrieveRequests)) {
            getRequests();
        } else if (purpose.equals(retrieveFriends)) {
            getFriends();
        }

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void getMatches() {
        String REQ_TYPE = getMatches;
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+MATCHES_DIR+"/retrieveMatches.php", jsonString, "POST",
                REQ_TYPE);
    }

    private void getRequests() {
        String REQ_TYPE = retrieveRequests;

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        id = sharedPreferences.getInt("id", 0);

        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+REQUEST_DIR+"/retrieveRequest.php", jsonString, "POST",
                REQ_TYPE);
    }

    private void getFriends() {
        String REQ_TYPE = retrieveFriends;

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        id = sharedPreferences.getInt("id", 0);

        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+FRIENDS_DIR+"/retrieveFriends.php", jsonString, "POST",
                REQ_TYPE);
    }

    @Override
    public void onTaskCompleted(String response, String REQ_TYPE) {
        retrieveFromJSON(response, REQ_TYPE);

        ArrayList<Profile> profileList = generateData();
        if (profileList.size() != 0) {
            ProfilesListAdapter profilesListAdapter =
                    new ProfilesListAdapter(getActivity(), generateData());
            mMatchesListView.setAdapter(profilesListAdapter);
            mMatchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Fragment fragment = new DisplayProfileFragment();
                    FragmentTransaction transaction = getActivity()
                            .getSupportFragmentManager().beginTransaction();

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack
                    Bundle args = new Bundle();
                    TextView nusnetTV = (TextView) view.findViewById(R.id.nusnet);
                    String nusnet = nusnetTV.getText().toString();
                    args.putString("nusnet", nusnet);
                    args.putString("purpose", purpose);
                    fragment.setArguments(args);
                    transaction.replace(R.id.fragment_frame, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        } else {
            mNoDataTextView.setVisibility(View.VISIBLE);
        }
    }

    // Convert profile information to JSON string
    public String convertToJSON(String REQ_TYPE) {
        JSONStringer jsonText = new JSONStringer();
        String courseStr = "";
        try {

            jsonText.object();
            if (REQ_TYPE.equals(getMatches)) {
                jsonText.key("course");
                Set<String> course = getActivity()
                        .getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
                        .getStringSet("course", null);
                jsonText.array();
                Iterator<String> courseIterator = course.iterator();
                while (courseIterator.hasNext()) {
                    courseStr = courseIterator.next();
                    jsonText.value(courseStr);
                    Log.d("getMatches", courseStr);
                }
                jsonText.endArray();
            } else if (REQ_TYPE.equals(retrieveRequests)) {
                jsonText.key("receiverId");
                jsonText.value(id);
            } else if (REQ_TYPE.equals(retrieveFriends)) {
                jsonText.key("id");
                jsonText.value(id);
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
            JSONObject jsonObject = new JSONObject(message);
            profiles = jsonObject.getJSONArray("results");
            Log.d("JSON Courses", profiles.toString());
        } catch (JSONException e) {
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
        ArrayList<String> course;
        for (int i = 0; i < profiles.length(); i++) {
            course = new ArrayList<>();
            try {
                profileObj = profiles.getJSONObject(i);
                id = profileObj.getInt("id");
                name = profileObj.getString("name");
                nusnet = profileObj.getString("nusnet");
                matricYear = profileObj.getInt("matricYear");
                JSONArray courseJSONArray = profileObj.getJSONArray("course");
                for (int j = 0; j < courseJSONArray.length(); j++) {
                    course.add(courseJSONArray.getString(j));
                }
                profileArrayList.add(new Profile (id, name, nusnet, matricYear, course));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return profileArrayList;
    }
}
