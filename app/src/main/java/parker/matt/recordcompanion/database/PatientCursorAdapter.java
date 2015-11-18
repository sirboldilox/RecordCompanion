package parker.matt.recordcompanion.database;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import parker.matt.recordcompanion.R;

/**
 * Created by matt on 18/11/15.
 */
public class PatientCursorAdapter extends CursorAdapter {
    public PatientCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.listview_patient_entry, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.patientEntryName);
        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow("first_name"));
        name += " " + cursor.getString(cursor.getColumnIndexOrThrow("last_name"));
        // Populate fields with extracted properties
        tvName.setText(name);
    }
}
