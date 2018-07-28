package edu.nus.sunlabitro.peernus;

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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONStringer;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity
        implements OnTaskCompleted, DisplayProfileFragment.OnFragmentInteractionListener {

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private DatabaseReference mFirebaseDatabaseReference;
    private StorageReference mFirebaseStorageReference;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Message, MessagesViewHolder> mFirebaseAdapter;

    private String USER_PREF;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    private static String FIREBASE_ADMIN_HOST;
    private final int ONE_MEGABYTE = 2048 * 2048;

    private LinearLayout mProfileBar;
    private ImageView mProfilePicImageView;
    private TextView mFriendNameTextView;
    private RecyclerView mChatList;
    private ImageView mUploadImageView;
    private EditText mMessageEditText;
    private Button mSendMsgBtn;
    private FrameLayout mFrame;

    private String chatroomId;
    private int userId;
    private String username;
    private int receiverId;
    private String receiverName;
    private String email;
    private int profilePicId;
    private byte[] bytes;
    private String messageText;
    private String receiverToken;

    private SharedPreferences sharedPreferences;

    private ArrayList<Message> messages;

    private final String TAG = "ChatActivity";
    private final int PICK_IMAGE = 101;

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public static class MessagesViewHolder extends RecyclerView.ViewHolder {
        public TextView mMessageTextView;
        public TextView mTimestampTextView;
        public ImageView mImageMessageView;
        public LinearLayout mLinearLayout;

        public MessagesViewHolder (View v) {
            super(v);
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.messageRow);
            mMessageTextView = (TextView) itemView.findViewById(R.id.message);
            mTimestampTextView = (TextView) itemView.findViewById(R.id.timestamp);
            mImageMessageView = (ImageView) itemView.findViewById(R.id.imageMessage);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        USER_PREF = getString(R.string.USER_PREF);
        FIREBASE_ADMIN_HOST = getString(R.string.FIREBASE_ADMIN_HOST);

        SharedPreferences sharedPreferences = getSharedPreferences(USER_PREF, MODE_PRIVATE);
        userId = sharedPreferences.getInt("id", 0);
        username = sharedPreferences.getString("name", "");

        Bundle bundle = getIntent().getExtras();
        receiverId = bundle.getInt("receiverId");
        receiverName = bundle.getString("receiverName");
        email = bundle.getString("email");
        profilePicId = bundle.getInt("profilePicId");

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        mFirebaseDatabaseReference = database.getReference("messages");
        mFirebaseStorageReference = storage.getReference("messages");

        retrieveReceiverToken();

        if (receiverId < userId) {
            chatroomId = receiverId + "_" + userId;
        } else {
            chatroomId = userId + "_" +receiverId;
        }

        messages = new ArrayList<>();

        mProfileBar = (LinearLayout) findViewById(R.id.profileBar);
        mProfilePicImageView = (ImageView) findViewById(R.id.smallProfilePic);
        mFriendNameTextView = (TextView) findViewById(R.id.friendName);
        mFrame = (FrameLayout) findViewById(R.id.fragment_frame);

        if (profilePicId > 0) {
            retrieveProfilePic();
        }

        mProfileBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSupportFragmentManager().getBackStackEntryCount() < 1) {
                    Fragment fragment = new DisplayProfileFragment();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack
                    Bundle args = new Bundle();
                    args.putString("nusnet", email);
                    args.putString("purpose", "showFriendInfo");
                    fragment.setArguments(args);
                    transaction.replace(R.id.fragment_frame, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    mFrame.setVisibility(View.VISIBLE);
                }
            }
        });

        mFriendNameTextView.setText(receiverName);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);

        mSendMsgBtn = findViewById(R.id.sendButton);
        mSendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("") || s == null) {
                    mSendMsgBtn.setEnabled(false);
                } else {
                    mSendMsgBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mUploadImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mUploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImage();
            }
        });

        mChatList = (RecyclerView) findViewById(R.id.chatList);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        SnapshotParser<Message> parser = new SnapshotParser<Message>() {
            @Override
            public Message parseSnapshot(DataSnapshot dataSnapshot) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    message.setId(dataSnapshot.getKey());
                }
                return message;
            }
        };

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(chatroomId);

        Log.d(TAG, messagesRef.toString());

        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(messagesRef, parser)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessagesViewHolder>(options) {

            @NonNull
            @Override
            public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

                return new MessagesViewHolder(layoutInflater
                        .inflate(R.layout.messages_list, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final MessagesViewHolder holder,
                    int position, @NonNull Message message) {
                LinearLayout.LayoutParams params = new LinearLayout
                        .LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (message.getName().equals(username)) {
                    params.gravity = Gravity.END;
                } else {
                    params.gravity = Gravity.START;
                }

                holder.mTimestampTextView.setLayoutParams(params);

                if (message.getText() != null) {
                    holder.mMessageTextView.setText(message.getText());
                    holder.mMessageTextView.setVisibility(View.VISIBLE);
                    holder.mImageMessageView.setVisibility(View.GONE);
                    holder.mMessageTextView.setLayoutParams(params);
                } else if (message.getImageUrl() != null) {
                    String imageUrl = message.getImageUrl();
                    if (imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(holder.mImageMessageView.getContext())
                                                    .load(downloadUrl)
                                                    .into(holder.mImageMessageView);
                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    } else {
                        Glide.with(holder.mImageMessageView.getContext())
                                .load(message.getImageUrl())
                                .into(holder.mImageMessageView);
                    }

                    holder.mMessageTextView.setVisibility(View.GONE);
                    holder.mImageMessageView.setVisibility(View.VISIBLE);
                    holder.mImageMessageView.setLayoutParams(params);

                }

                String timestamp = convertTimestampToDateTime(message.getTimestamp());
                holder.mTimestampTextView.setText(String.valueOf(timestamp));
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mChatList.scrollToPosition(positionStart);
                }
            }
        });

        mChatList.setLayoutManager(mLinearLayoutManager);
        mChatList.setAdapter(mFirebaseAdapter);

    }

    private void sendMessage() {
        messageText = mMessageEditText.getText().toString();
        long timestamp = System.currentTimeMillis();
        Message message = new Message(messageText, username, timestamp,null);
        mFirebaseDatabaseReference.child(chatroomId).push().setValue(message);
        mMessageEditText.setText("");
        sendNotification();
    }

    private void sendImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    private void uploadImage(StorageReference storageReference, Uri uri,
                             final String key, final long timestamp) {
        storageReference
                .putFile(uri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
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
                        Message message = new Message(null, username, timestamp,
                                taskSnapshot.getDownloadUrl().toString());
                        mFirebaseDatabaseReference.child(chatroomId).child(key)
                            .setValue(message);
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            if (data != null) {
                final Uri uri = data.getData();
                final long timestamp = System.currentTimeMillis();
                Message tmpMessage = new Message(null, username, timestamp, LOADING_IMAGE_URL);
                mFirebaseDatabaseReference.child(chatroomId).push().setValue(
                        tmpMessage, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError,
                                    DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    String key = mFirebaseDatabaseReference.getKey();
                                    StorageReference storageReference =
                                            FirebaseStorage.getInstance()
                                                    .getReference("chatImages/" + chatroomId)
                                                    .child(key)
                                                    .child(uri.getLastPathSegment());

                                    uploadImage(storageReference, uri, key, timestamp);
                                }
                            }
                        });
                messageText = "[Image]";
                sendNotification();
            }
        }
    }

    private String convertTimestampToDateTime(long inputTimestamp) {
        Timestamp timestamp = new Timestamp(inputTimestamp);
        Date date = new Date(timestamp.getTime());

        // S is the millisecond
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm");

        return simpleDateFormat.format(date);
    }

    @Override
    protected void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mFirebaseAdapter.startListening();
        super.onResume();
    }

    private void sendNotification() {
        String REQ_TYPE = "sendMessage";
        String jsonString = convertToJsonString();
        HttpAsyncTask task = new HttpAsyncTask(this);
        task.execute("https://" + FIREBASE_ADMIN_HOST + "/sendMessage", jsonString, "POST", REQ_TYPE);
    }

    @Override
    public void onTaskCompleted(String response, String REQ_TYPE) {

    }

    private String convertToJsonString() {
        JSONStringer jsonText = new JSONStringer();

        try {
            jsonText.object();
            jsonText.key("notification");
            jsonText.object();
            jsonText.key("title");
            jsonText.value(username);
            jsonText.key("body");
            jsonText.value(messageText);
            jsonText.endObject();
            jsonText.key("token");
            jsonText.value(receiverToken);
            jsonText.endObject();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonText.toString();
    }

    private void retrieveReceiverToken() {
        DatabaseReference userReference = database.getReference("users");
        userReference.child(String.valueOf(receiverId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                receiverToken = user.getToken();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void retrieveProfilePic() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        storageRef.child("images/" + profilePicId).getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Use the bytes to display the image
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        bitmap = imageRounded(bitmap);
                        mProfilePicImageView.setImageBitmap(bitmap);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mFrame.setVisibility(View.GONE);
    }

}
