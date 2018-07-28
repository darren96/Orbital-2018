package edu.nus.sunlabitro.peernus;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ModuleListAdapter extends ArrayAdapter<Module> {

    private final Context context;
    private final ArrayList<Module> modulesArrayList;
    private final ArrayList<Module> tempList;
    private ArrayList<Module> suggestionList;

    public ModuleListAdapter(Context context, ArrayList<Module> modulesArrayList) {

        super(context, R.layout.module_suggestion_list, modulesArrayList);
        this.context = context;
        this.modulesArrayList = modulesArrayList;
        this.tempList = new ArrayList<>(modulesArrayList);

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

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String modCode;
            String modTitle;
            int id;
            if (constraint != null) {
                suggestionList = new ArrayList<>();
                suggestionList.clear();
                String query = constraint.toString().toUpperCase();
                int count = 0;

                for (Module module : tempList) {
                    id = module.getId();
                    modCode = module.getModuleCode();
                    modTitle = module.getModuleTitle();
                    if (modCode.startsWith(query) || modTitle.startsWith(query.toLowerCase())) {
                        suggestionList.add(new Module(id, modCode, modTitle));
                        count++;
                    }
                    if (count == 5) {
                        break;
                    }

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestionList;
                filterResults.count = suggestionList.size();
                return filterResults;

            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<Module> filteredList = (ArrayList<Module>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (Module module : filteredList) {
                    add(module);
                }
                ModuleListAdapter.this.notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };
}
