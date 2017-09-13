package ch.supsi.dti.ssiot.shimmer.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ch.supsi.dti.ssiot.shimmer.R;

public class PairedDevicesArrayAdapter extends ArrayAdapter<BluetoothDevice> {

    private static final int LAYOUT_ID = R.layout.listitem_bluetooth_device;

    /**
     * Constructor
     * @param context  The current context.
     * @param objects  The objects to represent in the ListView.
     */
    public PairedDevicesArrayAdapter(Context context, List<BluetoothDevice> objects) {
        super(context, LAYOUT_ID, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(LAYOUT_ID, null);

            holder = new ViewHolder();
            holder.text1 = (TextView) convertView.findViewById(R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(R.id.text2);

            // stores view holder
            convertView.setTag(holder);
        }
        else {

            // recycles view holder
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = getItem(position);

        holder.text1.setText(device.getName().trim());
        holder.text2.setText(device.getAddress());

        return convertView;
    }

    /**
     * Holder class used to implement the view holder pattern
     * @see <a href="https://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder">Hold View Objects in a View Holder</a>
     */
    private static class ViewHolder {

        public TextView text1;
        public TextView text2;
    }
}
