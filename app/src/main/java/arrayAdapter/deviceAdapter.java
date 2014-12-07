package arrayAdapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import communication.AbstractCommunication;
import threadProcessing.ConnectedThread;

/**
 * Created by Bailantaotao on 2014/8/22.
 */
public class deviceAdapter<T> extends ArrayAdapter<T> {
    // Vars
    private LayoutInflater mInflater;
    ArrayList<ConnectedThread> connected = null;
    Context context = null;

    public deviceAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        this.context = context;
        this.connected = (ArrayList<ConnectedThread>) objects;
        this.connected.add(0, new ConnectedThread("", null, null, null, AbstractCommunication.TARGET_ALL));
        init(context);
    }

    private void init(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return connected.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public T getItem(int position) {
        return (T) connected.get(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView tv = new TextView(context);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(20);
        tv.setText(connected.get(position).getDeviceName());

        return tv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.WHITE);
        label.setTextSize(15);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(connected.get(position).getDeviceName());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }
}
