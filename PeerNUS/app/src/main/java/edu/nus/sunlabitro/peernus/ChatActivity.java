package edu.nus.sunlabitro.peernus;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference mFirebaseDatabaseReference;
//    private FirebaseRecyclerAdapter<Message, MessageViewHolder>
//            mFirebaseAdapter;

    private String USER_PREF;

    private EditText mMessageEditText;
    private Button mSendMsgBtn;

    private String chatroomId;

    private final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        USER_PREF = getString(R.string.USER_PREF);

        String senderName = getSharedPreferences(USER_PREF, MODE_PRIVATE)
                .getString("senderName", "");
        String receiverName = getIntent().getExtras().getString("receiverName");

        if (senderName.compareTo(receiverName) < 0) {
            chatroomId = senderName + "_" + receiverName;
        } else {
            chatroomId = receiverName + "_" + senderName;
        }

        database = FirebaseDatabase.getInstance();
        mFirebaseDatabaseReference = database.getReference("messages/" + chatroomId);

        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        mMessageEditText = findViewById(R.id.messageEditText);
        mSendMsgBtn = findViewById(R.id.sendButton);
        mSendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String message = mMessageEditText.getText().toString();
        mFirebaseDatabaseReference.setValue(message);
    }

}
