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
 * Created by matt on 09/03/16.
 */
public class BioTypeCursorAdapter extends CursorAdapter {

    public BioTypeCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    /**
     * Inflate a new view and return it
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.listview_biometric_type_entry, parent, false);
    }

    /**
     * Bind all data to the given view
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.biometricTypeEntryName);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
        tvName.setText(name);
    }
}
