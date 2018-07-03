package edu.nus.sunlabitro.peernus;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DisplayProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DisplayProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayProfileFragment extends Fragment
        implements OnTaskCompleted {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "nusnet";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private final String retrieveProfile = "301";
    private static String HOST;
    private static String PROFILE_DIR;

    private String nusnet;
    private String mParam2;

    private TextView nameTV;
    private TextView sexTV;
    private TextView yearOfStudiesTV;
    private TextView courseTV;
    private TextView modulesTV;
    private TextView descriptionTV;
    private Button btnSendMessage;

    private OnFragmentInteractionListener mListener;

    public DisplayProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DisplayProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DisplayProfileFragment newInstance(String param1, String param2) {
        DisplayProfileFragment fragment = new DisplayProfileFragment();
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
            nusnet = getArguments().getString(ARG_PARAM1);
        }

        HOST = getString(R.string.HOST);
        PROFILE_DIR = getString(R.string.PROFILE_DIR);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_profile, container, false);

        nameTV = (TextView) view.findViewById(R.id.name);
        sexTV = (TextView) view.findViewById(R.id.sex);
        yearOfStudiesTV = (TextView) view.findViewById(R.id.yearOfStudies);
        courseTV = (TextView) view.findViewById(R.id.course);
        modulesTV = (TextView) view.findViewById(R.id.module);
        descriptionTV = (TextView) view.findViewById(R.id.description);
        btnSendMessage = (Button) view.findViewById(R.id.btnSendMessage);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        retrieveProfile();

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

    private void retrieveProfile() {
        String REQ_TYPE = retrieveProfile;
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+PROFILE_DIR+"/retrieveProfile.php", jsonString, "POST",
                REQ_TYPE);
    }

    @Override
    public void onTaskCompleted(String response, String REQ_TYPE) {
        if (REQ_TYPE.equals(retrieveProfile)) {
            retrieveFromJSON(response, REQ_TYPE);
        }
    }

    // Convert profile information to JSON string
    public String convertToJSON(String REQ_TYPE) {
        JSONStringer jsonText = new JSONStringer();
        try {

            jsonText.object();
            if (REQ_TYPE.equals(retrieveProfile)) {
                jsonText.key("nusnet");
                jsonText.value(nusnet);
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
                int id = jsonObject.getInt("id");

                String name = jsonObject.getString("name");
                String sex = jsonObject.getString("sex");
                int matricYear = Integer.parseInt(jsonObject.getString("matricYear"));
                JSONArray courseJsonArray = jsonObject.getJSONArray("course");

                String courseStr = "";
                for (int i = 0; i < courseJsonArray.length(); i++) {
                    courseStr += courseJsonArray.getString(i) + "\n";
                }

                JSONArray moduleJsonArray = jsonObject.getJSONArray("modules");
                String moduleStr = "";
                for (int i = 0; i < moduleJsonArray.length(); i++) {
                    moduleStr += moduleJsonArray.get(i).toString() + "\n";
                }

                String description = jsonObject.getString("description");

                nameTV.setText(name);
                sexTV.setText(sex);
                yearOfStudiesTV.setText(String.valueOf(matricYear));
                courseTV.setText(courseStr);
                modulesTV.setText(moduleStr);
                descriptionTV.setText(description);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
