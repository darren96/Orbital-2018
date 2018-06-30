package edu.nus.sunlabitro.peernus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ModuleListAdapter extends ArrayAdapter<Module> {

    private final Context context;
    private final ArrayList<Module> modulesArrayList;

    public ModuleListAdapter(Context context, ArrayList<Module> modulesArrayList) {

        super(context, R.layout.module_suggestion_list, modulesArrayList);

        this.context = context;
        this.modulesArrayList = modulesArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.module_suggestion_list, parent, false);

        // 3. Get the two text view from the rowView
        TextView labelView = (TextView) rowView.findViewById(R.id.moduleCode);
        TextView valueView = (TextView) rowView.findViewById(R.id.moduleName);
        TextView idView = (TextView) rowView.findViewById(R.id.moduleId);

        // 4. Set the text for textView
        labelView.setText(modulesArrayList.get(position).getModuleCode());
        valueView.setText(modulesArrayList.get(position).getModuleTitle());
        idView.setText(String.valueOf(modulesArrayList.get(position).getId()));

        // 5. retrn rowView
        return rowView;
    }
}
