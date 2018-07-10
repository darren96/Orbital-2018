package edu.nus.sunlabitro.peernus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;


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
    private static String registerProfile = "104";
    private static String updateProfile = "105";

    private static final int PICK_IMAGE = 1;

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
    private Uri profilePicUri = null;

    private boolean isInitialAddCourse = true;
    private boolean isInitialAddModule = true;

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

        retrieveProfile();

        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });

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
                        new ModuleListAdapter(getActivity(), generateData(query));
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

    private void retrieveProfile() {

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(USER_PREF, MODE_PRIVATE);
        id = sharedPreferences.getInt("id", 0);

        name = sharedPreferences.getString("name", "");
        sex = sharedPreferences.getString("sex", "");
        matricYear = sharedPreferences.getInt("matricYear", 0);

        selectedCourseList = new ArrayList<>();
        Set<String> courseSet = sharedPreferences.getStringSet("course", null);
        Iterator<String> courseIterator = courseSet.iterator();
        Set<String> courseIdSet = sharedPreferences.getStringSet("courseId", null);
        Iterator<String> courseIdIterator = courseIdSet.iterator();
        String course = "";
        while (courseIterator.hasNext()) {
            selectedCourseList.add(Integer.parseInt(courseIdIterator.next()));
            course += courseIterator.next() + "\n";
        }

        selectedModuleList = new ArrayList<>();
        Set<String> modulesSet = sharedPreferences.getStringSet("modules", null);
        Iterator<String> moduleIterator = modulesSet.iterator();
        String modules = "";
        String modCode = "";
        while (moduleIterator.hasNext()) {
            modCode = moduleIterator.next();
            selectedModuleList.add(modCode);
            modules += modCode + "\n";
        }

        description = sharedPreferences.getString("description", "");

        mName.setText(name);

        if (sex.equals("Male")) {
            mSex.check(R.id.male);
        } else {
            mSex.check(R.id.female);
        }

        mYearofStudies.setText(String.valueOf(matricYear));
        mCourseList.setText(course);
        mModuleList.setText(modules);
        mDescription.setText(description);

        String bytesArray = sharedPreferences.getString("profilePic", null);

        if (bytesArray != null) {
            String[] split = bytesArray.substring(1, bytesArray.length()-1).split(", ");
            byte[] bytes = new byte[split.length];
            for (int i = 0; i < split.length; i++) {
                bytes[i] = Byte.parseByte(split[i]);
            }
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Bitmap imageRounded = MainActivity.imageRounded(bitmap);
            mProfilePic.setImageBitmap(imageRounded);
        }

    }

    private void addCourse() {
        String tmp = mCourse.getSelectedItem().toString();
        int id;

        String mCourseListStr = mCourseList.getText().toString();

        if (isInitialAddCourse) {
            selectedCourseList = new ArrayList<>();
            mCourseListStr = "";
            isInitialAddCourse = false;
        }

        try {
            JSONObject selectedCourse =
                    courses.getJSONObject(mCourse.getSelectedItemPosition());
            id = selectedCourse.getInt("course_id");

            if (!selectedCourseList.contains(id)) {
                mCourseList.setText(mCourseListStr + tmp + "\n");
                selectedCourseList.add(id);
            }
            else {
                Toast.makeText(getActivity(),"You have already added the course!", Toast.LENGTH_LONG).show();
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

        if (modCode.equals("") || modCode == null) {

            String mModuleListStr = mModuleList.getText().toString();

            if (isInitialAddModule) {
                selectedModuleList = new ArrayList<>();
                mModuleListStr = "";
                isInitialAddModule = false;
            }

            if (!selectedModuleList.contains(modCode)) {
                mModuleList.setText(mModuleListStr + modCode + "\n");
            } else {
                Toast.makeText(getActivity(), "You have already added the module!", Toast.LENGTH_LONG).show();
            }

            selectedModuleList.add(modCode);
            mModule.setText("");

        }

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
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            //TODO: action
            if (data != null) {
                profilePicUri = data.getData();
                mProfilePic.setImageURI(data.getData());
                Bitmap bitmap = ((BitmapDrawable) mProfilePic.getDrawable()).getBitmap();
                mProfilePic.setImageBitmap(MainActivity.imageRounded(bitmap));
            }
        }
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
                    new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, courseArray);

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
        } else if (REQ_TYPE.equals(registerProfile) || REQ_TYPE.equals(updateProfile)) {
            if (profilePicUri != null) {
                uploadImage();
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
        HttpAsyncTask task = new HttpAsyncTask(this);

        name = mName.getText().toString();
        matricYear = Integer.parseInt(mYearofStudies.getText().toString());
        description = mDescription.getText().toString();

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

    private void uploadImage() {
        // Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/png")
                .build();

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReference();

        // Upload file and metadata to the path 'images/mountains.jpg'
        UploadTask uploadTask = storageRef.child("images/" + id)
                .putFile(profilePicUri, metadata);

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                // ...
                Bitmap bitmap = ((BitmapDrawable) mProfilePic.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                SharedPreferences sharedPreferences = getActivity()
                        .getSharedPreferences(USER_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profilePic", Arrays.toString(bytes));
                editor.commit();

                NavigationView navigationView = (NavigationView) getActivity()
                        .findViewById(R.id.nav_view);
                View headerView = navigationView.getHeaderView(0);
                ImageView headerProfilePic = (ImageView) headerView.findViewById(R.id.profilePic);
                Bitmap roundedImage = MainActivity.imageRounded(bitmap);

                headerProfilePic.setImageBitmap(roundedImage);
            }
        });
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
                jsonText.key("profilePic");
                if (mProfilePic.getDrawable().equals(R.drawable.profile)) {
                    jsonText.value(0);
                } else {
                    jsonText.value(id);
                }
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
            } else if (REQ_TYPE.equals(registerProfile) || REQ_TYPE.equals((updateProfile))) {
                JSONObject jsonObject = new JSONObject(message);
                String status = jsonObject.getString("Status");
                if (status.equals("OK")) {
                    Toast.makeText(getActivity(),"Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    id = jsonObject.getInt("id");

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

                    SharedPreferences.Editor editor = getActivity().
                            getSharedPreferences(USER_PREF, MODE_PRIVATE).edit();
                    editor.putInt("id", id);
                    editor.putString("name", name);
                    editor.putString("sex", sex);
                    editor.putInt("matricYear", matricYear);
                    editor.putStringSet("course", selectedCourseSet);
                    editor.putStringSet("courseId", selectedCourseIdSet);
                    editor.putStringSet("modules", selectedModuleSet);
                    editor.putString("description", description);
                    editor.apply();
                } else {
                    Toast.makeText(getActivity(),"Profile update failed!", Toast.LENGTH_LONG).show();
                }
                Log.d("JSON Profile", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
