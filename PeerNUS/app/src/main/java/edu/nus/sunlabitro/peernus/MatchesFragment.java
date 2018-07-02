package edu.nus.sunlabitro.peernus;

import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MatchesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MatchesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MatchesFragment extends Fragment
        implements OnTaskCompleted {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView mMatchesListView;

    private String USER_PREF;
    private final String getMatches = "201";
    private final String retrieveProfile = "202";

    private static String HOST;
    private static String MATCHES_DIR;

    private String email;

    private JSONArray matches;

    private OnFragmentInteractionListener mListener;

    public MatchesFragment() {
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
    public static MatchesFragment newInstance(String param1, String param2) {
        MatchesFragment fragment = new MatchesFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_matches, container, false);
        mMatchesListView = (ListView) view.findViewById(R.id.matchesList);

        USER_PREF = getString(R.string.USER_PREF);
        HOST = getString(R.string.HOST);
        MATCHES_DIR = getString(R.string.MATCHES_DIR);

        getMatches();

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

    @Override
    public void onTaskCompleted(String response, String REQ_TYPE) {
        if (REQ_TYPE.equals(getMatches)) {
            retrieveFromJSON(response, REQ_TYPE);
            MatchesListAdapter matchesListAdapter =
                    new MatchesListAdapter(getActivity(), generateData());
            mMatchesListView.setAdapter(matchesListAdapter);
            mMatchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    android.app.Fragment fragment = new DisplayProfileFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack
                    Bundle args = new Bundle();
                    TextView nusnetTV = (TextView) view.findViewById(R.id.nusnet);
                    String nusnet = nusnetTV.getText().toString();
                    args.putString("nusnet", nusnet);
                    fragment.setArguments(args);
                    transaction.replace(R.id.fragment_frame, fragment, "currentFragment");
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
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
                HashSet courseHashSet = new HashSet(course);
                Iterator<String> courseIterator = courseHashSet.iterator();
                while (courseIterator.hasNext()) {
                    courseStr = courseIterator.next();
                    jsonText.value(courseStr);
                    Log.d("getMatches", courseStr);
                }
                jsonText.endArray();
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
