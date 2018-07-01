package edu.nus.sunlabitro.peernus;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpdateProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateProfileFragment extends Fragment
        implements OnTaskCompleted {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static String HOST;
    private static String NUSMOD_HOST;
    private static String MOD_DIR;
    private static String PROFILE_DIR;
    private static String COURSES_DIR;
    private static String USER_PREF;

    private static String getCourses = "101";
    private static String getModules = "102";
    private static String retrieveProfile = "103";
    private static String registerProfile = "104";
    private static String updateProfile = "105";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText mName;
    private RadioGroup mSex;
    private EditText mYearofStudies;
    private ImageView mProfilePic;
    private Spinner mCourse;
    private TextView mCourseList;
    private EditText mModule;
    private TextView mModuleList;
    private TextView mModuleSelectedId;
    private ListView mModuleListView;
    private EditText mDescription;

    private Button mAddCourseBtn;
    private Button mAddModuleBtn;
    private Button mSaveProfileBtn;

    private static ArrayList<String> course;
    private static ArrayList<Module> module;
    private ArrayList<Integer> selectedCourseList;
    private ArrayList<String> selectedModuleList;
    private JSONArray courses;
    private JSONArray modules;

    private int id;
    private String name;
    private String nusnet;
    private String sex;
    private int matricYear;
    private String description;

    private OnFragmentInteractionListener mListener;

    public UpdateProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpdateProfileFragment newInstance(String param1, String param2) {
        UpdateProfileFragment fragment = new UpdateProfileFragment();
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

        HOST = getString(R.string.HOST);
        PROFILE_DIR = getString(R.string.PROFILE_DIR);
        COURSES_DIR = getString(R.string.COURSES_DIR);
        NUSMOD_HOST = getString(R.string.NUSMOD_HOST);
        MOD_DIR = getString(R.string.MOD_DIR);
        USER_PREF = getString(R.string.USER_PREF);

        selectedCourseList = new ArrayList<>();
        selectedModuleList = new ArrayList<>();

        getAllCourses();
        getAllModules();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_update_profile, container, false);

        mProfilePic = (ImageView) view.findViewById(R.id.profilePic);
        mName = (EditText) view.findViewById(R.id.name);
        mSex = (RadioGroup) view.findViewById(R.id.sex);
        mYearofStudies = (EditText) view.findViewById(R.id.yearOfStudies);
        mCourse = (Spinner) view.findViewById(R.id.course);
        mCourseList = (TextView) view.findViewById(R.id.courseList);
        mModule = (EditText) view.findViewById(R.id.module);
        mModuleList = (TextView) view.findViewById(R.id.moduleList);
        mModuleListView = (ListView) view.findViewById(R.id.moduleListView);
        mModuleSelectedId = (TextView) view.findViewById(R.id.selectedModuleId);
        mDescription = (EditText) view.findViewById(R.id.description);

        mAddCourseBtn = (Button) view.findViewById(R.id.btnAddCourse);
        mAddModuleBtn = (Button) view.findViewById(R.id.btnAddModule);
        mSaveProfileBtn = (Button) view.findViewById(R.id.btnSaveProfile);

        mAddCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCourse();
            }
        });

        mAddModuleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addModule();
            }
        });

        mModule.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = mModule.getText().toString();
                ModuleListAdapter moduleListAdapter =
                        new ModuleListAdapter(getContext(), generateData(query));
                mModuleListView.setAdapter(moduleListAdapter);
                mModuleListView.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mModule.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mModuleListView.setVisibility(View.INVISIBLE);
                }
            }
        });

        mModuleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView mModuleId = (TextView) view.findViewById(R.id.moduleId);
                TextView mModuleCode = (TextView) view.findViewById(R.id.moduleCode);
                String selectedModuleId = mModuleId.getText().toString();
                String selectedModuleCode = mModuleCode.getText().toString();
                selectModule(selectedModuleId, selectedModuleCode);
                mModuleListView.setVisibility(View.INVISIBLE);
            }
        });

        mSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) getView().findViewById(checkedId);
                sex = radioButton.getText().toString();
            }
        });

        mSaveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadProfile();
            }
        });

        return view;
    }

    private void addCourse() {
        String tmp = mCourse.getSelectedItem().toString();
        int id;
        String courseName;
        String faculty;

        String mCourseListStr = mCourseList.getText().toString();

        try {
            JSONObject selectedCourse =
                    courses.getJSONObject(mCourse.getSelectedItemPosition());
            id = selectedCourse.getInt("course_id");
            courseName = selectedCourse.getString("name");
            faculty = selectedCourse.getString("faculty");

            if (mCourseListStr.equals("") || mCourseListStr.equals(null)) {
                mCourseList.setText(tmp);
                selectedCourseList.add(id);
            } else {
                if (!selectedCourseList.contains(id)) {
                    mCourseList.setText(mCourseListStr + ", " + tmp);
                    selectedCourseList.add(id);
                }
                else {
                    Toast.makeText(getContext(),"You have already added the course!", Toast.LENGTH_LONG).show();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void selectModule(String selectedModuleId, String selectedModuleCode) {
        mModule.setText(selectedModuleCode);
        mModuleSelectedId.setText(selectedModuleId);
    }

    private void addModule() {
        String modCode = mModule.getText().toString().toUpperCase();

        String mModuleListStr = mModuleList.getText().toString();
        if (mModuleListStr.equals("") || mModuleListStr.equals(null)) {
            mModuleList.setText(modCode);
        } else {
            if (!selectedModuleList.contains(modCode)) {
                mModuleList.setText(mModuleListStr + ", " + modCode);
            }
            else {
                Toast.makeText(getContext(),"You have already added the module!", Toast.LENGTH_LONG).show();
            }
        }

        selectedModuleList.add(modCode);
        mModule.setText("");

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

    @Override
    public void onTaskCompleted(String response, String REQ_TYPE) {
        retrieveFromJSON(response, REQ_TYPE);

        if (REQ_TYPE.equals(getCourses)) {
            course = new ArrayList<>();
            for (int i = 0; i < courses.length(); i++) {
                try {
                    course.add(courses.getJSONObject(i).getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String[] courseArray = course.toArray(new String[course.size()]);
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, courseArray);

            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

            mCourse.setAdapter(arrayAdapter);
        } else if (REQ_TYPE.equals(getModules)) {
            module = new ArrayList<>();

            String modCode;
            String modTitle;

            for (int i = 0; i < modules.length(); i++) {
                try {
                    modCode = modules.getJSONObject(i).getString("ModuleCode");
                    modTitle = modules.getJSONObject(i).getString("ModuleTitle");
                    module.add(new Module(i, modCode, modTitle));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (REQ_TYPE.equals(retrieveProfile)) {
            HttpAsyncTask task = new HttpAsyncTask(this);

            name = mName.getText().toString();
            matricYear = Integer.parseInt(mYearofStudies.getText().toString());
            description = mDescription.getText().toString();
            String jsonString;

            if (id > 0) {
                REQ_TYPE = updateProfile;
                jsonString = convertToJSON(REQ_TYPE);
                task.execute("https://" + HOST + "/" + PROFILE_DIR + "/updateProfile.php", jsonString, "POST",
                        REQ_TYPE);
            } else {
                REQ_TYPE = registerProfile;
                jsonString = convertToJSON(REQ_TYPE);
                task.execute("https://" + HOST + "/" + PROFILE_DIR + "/registerProfile.php", jsonString, "POST",
                        REQ_TYPE);
            }
        }

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

    private ArrayList<Module> generateData(String query){
        int count = 0;
        ArrayList<Module> moduleArrayList = new ArrayList<>();
        JSONObject moduleObj = null;
        String modCode;
        String modTitle;
        for (int i = 0; i < modules.length(); i++) {
            if (count <= 5) {
                try {
                    moduleObj = modules.getJSONObject(i);
                    modCode = moduleObj.getString("ModuleCode");
                    modTitle = moduleObj.getString("ModuleTitle");
                    if (modCode.contains(query) || modTitle.contains(query)) {
                        moduleArrayList.add(new Module (i, modCode, modTitle));
                        count++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return moduleArrayList;
    }

    public void getAllCourses() {
        String REQ_TYPE = getCourses;
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+COURSES_DIR+"/retrieveCourses.php", jsonString, "POST",
                REQ_TYPE);
    }

    public void getAllModules() {
        String REQ_TYPE = getModules;
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+NUSMOD_HOST+"/"+MOD_DIR+"/moduleList.json", jsonString, "GET",
                REQ_TYPE);
    }

    public void uploadProfile() {
        String REQ_TYPE;
        String jsonString;
        id = 0;
        HttpAsyncTask task = new HttpAsyncTask(this);

        REQ_TYPE = retrieveProfile;
        nusnet = getActivity().getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
                .getString("email", "");
        jsonString = convertToJSON(REQ_TYPE);
        task.execute("https://"+HOST+"/"+PROFILE_DIR+"/retrieveProfile.php", jsonString, "POST",
                REQ_TYPE);

    }

    // Convert profile information to JSON string
    public String convertToJSON(String REQ_TYPE) {
        JSONStringer jsonText = new JSONStringer();
        try {

            jsonText.object();
            if (REQ_TYPE.equals(registerProfile) || REQ_TYPE.equals(updateProfile)) {
                jsonText.key("id");
                jsonText.value(id);
                jsonText.key("name");
                jsonText.value(name);
                jsonText.key("nusnet");
                jsonText.value(nusnet);
                jsonText.key("sex");
                jsonText.value(sex);
                jsonText.key("matricYear");
                jsonText.value(matricYear);
                jsonText.key("course");
                jsonText.array();
                for (int i = 0; i < selectedCourseList.size(); i++) {
                    jsonText.value(selectedCourseList.get(i));
                }
                jsonText.endArray();
                jsonText.key("modules");
                jsonText.array();
                for (int i = 0; i < selectedModuleList.size(); i++) {
                    jsonText.value(selectedModuleList.get(i));
                }
                jsonText.endArray();
                jsonText.key("description");
                jsonText.value(description);
            } else if (REQ_TYPE.equals(retrieveProfile)) {
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

            if (REQ_TYPE.equals(getCourses)) {
                JSONObject jsonObject = new JSONObject(message);
                courses = jsonObject.getJSONArray("results");
                Log.d("JSON Courses", courses.toString());
            } else if (REQ_TYPE.equals(getModules)) {
                modules = new JSONArray(message);
                Log.d("JSON Modules", modules.toString());
            } else if (REQ_TYPE.equals(retrieveProfile)) {
                JSONObject jsonObject = new JSONObject(message);
                id = jsonObject.getInt("id");
                /*
                name = jsonObject.getString("name");
                sex = jsonObject.getString("sex");
                matricYear = Integer.parseInt(jsonObject.getString("matricYear"));
                JSONArray courseJsonArray = jsonObject.getJSONArray("course");
                selectedCourseList = new ArrayList<>();
                for (int i = 0; i < courseJsonArray.length(); i++) {
                    selectedCourseList.add(courseJsonArray.getInt(i));
                }

                JSONArray moduleJsonArray = jsonObject.getJSONArray("modules");
                selectedModuleList = new ArrayList<>();
                for (int i = 0; i < moduleJsonArray.length(); i++) {
                    selectedModuleList.add(moduleJsonArray.get(i).toString());
                }
                */
                Log.d("ID", String.valueOf(id));
            } else if (REQ_TYPE.equals(registerProfile)) {
                Log.d("JSON Profile", message);
            } else if (REQ_TYPE.equals(updateProfile)) {
                Log.d("JSON Profile", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
