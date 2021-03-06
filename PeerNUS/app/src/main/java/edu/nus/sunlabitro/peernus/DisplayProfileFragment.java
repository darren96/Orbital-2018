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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private static final String ARG_PARAM2 = "purpose";

    // TODO: Rename and change types of parameters
    private final String retrieveProfile = "301";
    private final String sendRequest = "302";
    private final String acceptRequest = "303";
    private final String cancelRequest = "304";
    private final String sendMessage = "305";
    private final String unFriend = "306";

    private final String getMatches = "getMatches";
    private final String retrieveRequests = "getRequests";
    private final String retrieveFriends = "getFriends";
    private final String showFriendInfo = "showFriendInfo";
    private final String sendNotification = "sendNotification";

    private final int ONE_MEGABYTE = 2048 * 2048;

    private static String HOST;
    private static String PROFILE_DIR;
    private static String REQUEST_DIR;
    private static String FRIEND_DIR;
    private static String USER_PREF;
    private static String FIREBASE_ADMIN_HOST;

    private FirebaseDatabase database;
    private SharedPreferences sharedPreferences;
    private String nusnet;
    private String purpose;
    private Bitmap bitmap;
    private int profilePicId;
    private String receiverToken;
    private int profileId;
    private int userId;
    private String username;
    private int senderId;
    private int receiverId;
    private boolean isFriend;
    private boolean isSentRequest;
    private boolean isReceivedRequest;

    private ImageView mProfilePic;
    private TextView nameTV;
    private TextView sexTV;
    private TextView yearOfStudiesTV;
    private TextView courseTV;
    private TextView modulesTV;
    private TextView descriptionTV;
    private Button btnSendRequest;
    private Button btnAcceptRequest;
    private Button btnCancelRequest;
    private Button btnSendMessage;
    private Button btnUnFriend;

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
            purpose = getArguments().getString(ARG_PARAM2);
        }

        database = FirebaseDatabase.getInstance();

        HOST = getString(R.string.HOST);
        PROFILE_DIR = getString(R.string.PROFILE_DIR);
        REQUEST_DIR = getString(R.string.REQUEST_DIR);
        FRIEND_DIR = getString(R.string.FRIENDS_DIR);
        USER_PREF = getString(R.string.USER_PREF);
        FIREBASE_ADMIN_HOST = getString(R.string.FIREBASE_ADMIN_HOST);

        sharedPreferences = getActivity()
                .getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("id", 0);
        username = sharedPreferences.getString("name", "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("PeerNUS");
        View view = inflater.inflate(R.layout.fragment_display_profile, container, false);

        nameTV = (TextView) view.findViewById(R.id.name);
        sexTV = (TextView) view.findViewById(R.id.sex);
        yearOfStudiesTV = (TextView) view.findViewById(R.id.yearOfStudies);
        courseTV = (TextView) view.findViewById(R.id.course);
        modulesTV = (TextView) view.findViewById(R.id.module);
        descriptionTV = (TextView) view.findViewById(R.id.description);
        btnSendRequest = (Button) view.findViewById(R.id.btnSendRequest);
        btnAcceptRequest = (Button) view.findViewById(R.id.btnAcceptRequest);
        btnCancelRequest = (Button) view.findViewById(R.id.btnCancelRequest);
        btnSendMessage = (Button) view.findViewById(R.id.btnSendMessage);
        btnUnFriend = (Button) view.findViewById(R.id.btnUnFriend);

        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
        btnAcceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest();
            }
        });
        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRequest();
            }
        });
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        btnUnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unFriend();
            }
        });

        if (purpose.equals(getMatches)) {
            btnSendRequest.setVisibility(View.VISIBLE);
        } else if (purpose.equals(retrieveRequests)) {
            btnAcceptRequest.setVisibility(View.VISIBLE);
            btnCancelRequest.setVisibility(View.VISIBLE);
        } else if (purpose.equals(retrieveFriends)) {
            btnSendMessage.setVisibility(View.VISIBLE);
            btnUnFriend.setVisibility(View.VISIBLE);
        } else if (purpose.equals(showFriendInfo)) {
            btnSendMessage.setVisibility(View.INVISIBLE);
            btnUnFriend.setVisibility(View.INVISIBLE);
        }

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

    private void sendRequest() {
        senderId = sharedPreferences.getInt("id", 0);
        receiverId = profileId;
        String REQ_TYPE = sendRequest;
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+REQUEST_DIR+"/sendRequest.php", jsonString, "POST",
                REQ_TYPE);
        btnSendRequest.setVisibility(View.INVISIBLE);
    }

    private void acceptRequest() {
        String REQ_TYPE = acceptRequest;
        senderId = profileId;
        receiverId = userId;
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+REQUEST_DIR+"/acceptRequest.php", jsonString, "POST",
                REQ_TYPE);
    }

    private void cancelRequest() {
        String REQ_TYPE = cancelRequest;
        if (isSentRequest) {
            senderId = userId;
            receiverId = profileId;
        } else {
            senderId = profileId;
            receiverId = userId;
        }
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+REQUEST_DIR+"/cancelRequest.php", jsonString, "POST",
                REQ_TYPE);
    }

    private void unFriend() {
        String REQ_TYPE = unFriend;
        senderId = profileId;
        receiverId = userId;
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://"+HOST+"/"+FRIEND_DIR+"/unFriend.php", jsonString, "POST",
                REQ_TYPE);
    }

    private void sendMessage() {
        senderId = sharedPreferences.getInt("id", 0);
        receiverId = profileId;
        Intent intent = new Intent(getActivity(), ChatActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt("receiverId", receiverId);
        bundle.putString("receiverName", nameTV.getText().toString());
        bundle.putString("email", nusnet);
        bundle.putInt("profilePicId", profilePicId);

        intent.putExtras(bundle);
        startActivity(intent);

    }

    @Override
    public void onTaskCompleted(String response, String REQ_TYPE) {
        retrieveFromJSON(response, REQ_TYPE);
    }

    // Convert profile information to JSON string
    private String convertToJSON(String REQ_TYPE) {
        JSONStringer jsonText = new JSONStringer();
        try {

            jsonText.object();
            if (REQ_TYPE.equals(retrieveProfile)) {
                jsonText.key("nusnet");
                jsonText.value(nusnet);
                jsonText.key("userId");
                jsonText.value(userId);
            } else if (REQ_TYPE.equals(sendRequest) || REQ_TYPE.equals(acceptRequest)
                    || REQ_TYPE.equals(cancelRequest) || REQ_TYPE.equals(unFriend)) {
                jsonText.key("senderId");
                jsonText.value(senderId);
                jsonText.key("receiverId");
                jsonText.value(receiverId);
            } else {
                jsonText.key("notification");
                jsonText.object();
                jsonText.key("title");
                if (purpose.equals(sendRequest)) {
                    jsonText.value("Friend Request");
                } else if (purpose.equals(acceptRequest)) {
                    jsonText.value("Friend");
                }
                jsonText.key("body");
                if (purpose.equals(sendRequest)) {
                    jsonText.value("You have received a friend request from " + username + ".");
                } else if (purpose.equals(acceptRequest)) {
                    jsonText.value("You and " + username + " are friends now. You can start and chat now.");
                }
                jsonText.endObject();
                jsonText.key("token");
                jsonText.value(receiverToken);
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

            if (REQ_TYPE.equals(retrieveProfile) || REQ_TYPE.equals(showFriendInfo)) {
                profileId = jsonObject.getInt("id");

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
                profilePicId = jsonObject.getInt("profilePic");

                isFriend = jsonObject.getBoolean("isFriend");
                isSentRequest = jsonObject.getBoolean("isSentRequest");
                isReceivedRequest = jsonObject.getBoolean("isReceivedRequest");

                nameTV.setText(name);
                sexTV.setText(sex);
                yearOfStudiesTV.setText(String.valueOf(matricYear));
                courseTV.setText(courseStr);
                modulesTV.setText(moduleStr);
                descriptionTV.setText(description);

                if (profilePicId != 0) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();

                    storageRef.child("images/" + profileId).getBytes(ONE_MEGABYTE)
                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Use the bytes to display the image
                                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    mProfilePic = getActivity().findViewById(R.id.profilePic);
                                    bitmap = imageRounded(bitmap);
                                    mProfilePic.setImageBitmap(bitmap);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                }

                retrieveReceiverToken();


                if (isFriend) {
                    btnUnFriend.setVisibility(View.VISIBLE);
                    btnSendMessage.setVisibility(View.VISIBLE);
                    btnSendRequest.setVisibility(View.GONE);
                }
                if (isSentRequest) {
                    btnCancelRequest.setVisibility(View.VISIBLE);
                    btnCancelRequest.setText("Cancel Request");
                    btnSendRequest.setVisibility(View.GONE);
                }
                if (isReceivedRequest) {
                    btnAcceptRequest.setVisibility(View.VISIBLE);
                    btnCancelRequest.setVisibility(View.VISIBLE);
                    btnCancelRequest.setText("Cancel");
                }
                if (purpose.equals(showFriendInfo)) {
                    btnUnFriend.setVisibility(View.GONE);
                    btnSendMessage.setVisibility(View.GONE);
                }


            } else if (REQ_TYPE.equals(sendRequest)) {
                String status = jsonObject.getString("Status");
                if (status.equals("OK")) {
                    Toast.makeText(getActivity(), "Friends Request Sent", Toast.LENGTH_LONG)
                            .show();
                    purpose = sendRequest;
                    sendNotification(sendNotification);
                } else {
                    Toast.makeText(getActivity(), "Request Failed. Please Try Again Later.", Toast.LENGTH_LONG)
                            .show();
                }
            } else if (REQ_TYPE.equals(acceptRequest)) {
                String status = jsonObject.getString("Status");
                if (status.equals("OK")) {
                    Toast.makeText(getActivity(), "Friends Request Accepted", Toast.LENGTH_LONG)
                            .show();
                    purpose = acceptRequest;
                    sendNotification(sendNotification);
                    btnAcceptRequest.setVisibility(View.GONE);
                    btnCancelRequest.setVisibility(View.GONE);
                    btnSendMessage.setVisibility(View.VISIBLE);
                    btnUnFriend.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), "Acceptance Failed. Please Try Again Later.", Toast.LENGTH_LONG)
                            .show();
                }
            } else if (REQ_TYPE.equals(cancelRequest)) {
                String status = jsonObject.getString("Status");
                if (status.equals("OK")) {
                    Toast.makeText(getActivity(), "Deleted friend request", Toast.LENGTH_LONG)
                            .show();
                    btnAcceptRequest.setVisibility(View.GONE);
                    btnCancelRequest.setVisibility(View.GONE);
                    btnSendRequest.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), "Cancellation Failed. Please Try Again Later.", Toast.LENGTH_LONG)
                            .show();
                }
            } else if (REQ_TYPE.equals(unFriend)) {
                String status = jsonObject.getString("Status");
                if (status.equals("OK")) {
                    Toast.makeText(getActivity(), "Removed from friend list", Toast.LENGTH_LONG)
                            .show();
                    btnSendMessage.setVisibility(View.GONE);
                    btnUnFriend.setVisibility(View.GONE);
                    btnSendRequest.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), "Unfriend Failed. Please Try Again Later.", Toast.LENGTH_LONG)
                            .show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String REQ_TYPE) {
        String jsonString = convertToJSON(REQ_TYPE);
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://" + FIREBASE_ADMIN_HOST + "/sendMessage", jsonString, "POST", REQ_TYPE);

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
                bitmap.getWidth())), mpaint);
        return imageRounded;
    }

    private void retrieveReceiverToken() {
        DatabaseReference userReference = database.getReference("users");
        userReference.child(String.valueOf(profileId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        receiverToken = user.getToken();
                        Log.d("DisplayProfileFragment", receiverToken);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

}
