package edu.nus.sunlabitro.peernus;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;

public class ProfilesListAdapter extends ArrayAdapter<Profile> {

    private final Context context;
    private final ArrayList<Profile> profilesArrayList;
    private final int ONE_MEGABYTE = 1024 * 1024;

    public ProfilesListAdapter(Context context, ArrayList<Profile> profilesArrayList) {

        super(context, R.layout.profiles_list, profilesArrayList);

        this.context = context;
        this.profilesArrayList = profilesArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        final View rowView = inflater.inflate(R.layout.profiles_list, parent, false);

        // 3. Get the two text view from the rowView
        TextView nameTV = (TextView) rowView.findViewById(R.id.name);
        TextView yearOfStudiesTV = (TextView) rowView.findViewById(R.id.yearOfStudies);
        TextView courseTV = (TextView) rowView.findViewById(R.id.course);
        TextView nusnetTV = (TextView) rowView.findViewById(R.id.nusnet);

        // 4. Set the text for textView
        int profilePic = profilesArrayList.get(position).getProfilePic();

        nameTV.setText(profilesArrayList.get(position).getName());
        yearOfStudiesTV.setText("Year " +
                String.valueOf(profilesArrayList.get(position).getMatricYear()));
        ArrayList<String> courses = profilesArrayList.get(position).getCourse();
        String courseStr = "";
        for (int i = 0; i < courses.size(); i++) {
            if (i > 0) {
                courseStr += ", ";
            }
            courseStr += courses.get(i);
        }
        courseTV.setText(courseStr);
        nusnetTV.setText(profilesArrayList.get(position).getNusnet());

        if (profilePic != 0) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            storageRef.child("images/" + profilePic).getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            // Use the bytes to display the image
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            ImageView mProfilePic = (ImageView) rowView.findViewById(R.id.profilePic);
                            Bitmap imageRounded = MainActivity.imageRounded(bitmap);
                            mProfilePic.setImageBitmap(imageRounded);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
        // 5. return rowView
        return rowView;
    }
}
