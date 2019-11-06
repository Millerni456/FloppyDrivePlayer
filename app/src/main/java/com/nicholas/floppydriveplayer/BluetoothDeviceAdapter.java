package com.nicholas.floppydriveplayer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Nicholas on 9/19/2016.
 */
public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {

    /**
     * Creates a BluetoothAdapter with an empty ArrayList of items.
     * @param context
     */
    public BluetoothDeviceAdapter(Context context) {
        this(context, new ArrayList<BluetoothDevice>());
    }

    public BluetoothDeviceAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        super(context, 0, devices);
        //Create an ArrayAdapter that doesn't use a TextView layout.
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            //If we cannot reuse the view, inflate the view from the resource.
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bluetooth_device, parent, false);
        }

        //Populate the data fields in the view.
        BluetoothDevice device = getItem(position);
        String name = device.getName();
        if (name == null || name.isEmpty() ) {
            name = "(null)";
        }
        ((TextView)convertView.findViewById(R.id.text_name)).setText(name);
        ((TextView)convertView.findViewById(R.id.text_mac_address)).setText("MAC: " + device.getAddress());

        return convertView;
    }
}
