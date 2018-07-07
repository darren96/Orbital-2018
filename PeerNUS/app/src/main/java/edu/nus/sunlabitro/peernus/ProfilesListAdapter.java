package edu.nus.sunlabitro.peernus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ProfilesListAdapter extends ArrayAdapter<Profile> {

    private final Context context;
    private final ArrayList<Profile> profilesArrayList;

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
        View rowView = inflater.inflate(R.layout.profiles_list, parent, false);

        // 3. Get the two text view from the rowView
        TextView nameTV = (TextView) rowView.findViewById(R.id.name);
        TextView yearOfStudiesTV = (TextView) rowView.findViewById(R.id.yearOfStudies);
        TextView courseTV = (TextView) rowView.findViewById(R.id.course);
        TextView nusnetTV = (TextView) rowView.findViewById(R.id.nusnet);

        // 4. Set the text for textView
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

        // 5. retrn rowView
        return rowView;
    }
}
