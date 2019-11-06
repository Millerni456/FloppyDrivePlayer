package com.nicholas.floppydriveplayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.Set;

/**
 * Created by Nicholas on 9/19/2016.
 */
public class BluetoothDialog extends DialogFragment {

    public interface BluetoothDialogListener {
        void onBluetoothDialogPositiveClick(DialogFragment dialog, BluetoothDevice device);
    }

    public static final String EXTRA_DEVICE = "com.nicholas.floppydriveplayer.extra.DEVICE";

    private final String TAG = "BluetoothDialog";

    private BluetoothAdapter bluetoothAdapter = null;
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_COARSE_LOCATION = 1;

    private BluetoothDeviceAdapter pairedDevicesAdapter = null;
    private BluetoothDeviceAdapter otherDevicesAdapter = null;
    private View view = null;

    private BluetoothDialogListener listener = null;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                ListView list = (ListView)view.findViewById(R.id.list_other_devices);
                list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (foundDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    //Only add devices which haven't been paired.
                    otherDevicesAdapter.add(foundDevice);
                }
            } else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                view.findViewById(R.id.progress_scanning).setVisibility(View.VISIBLE);
                view.findViewById(R.id.button_scan).setEnabled(false);
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                view.findViewById(R.id.progress_scanning).setVisibility(View.GONE);
                view.findViewById(R.id.button_scan).setEnabled(true);
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Get bluetooth adapter.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
            throw new RuntimeException("BluetoothDialog should only be used if Bluetooth is supported.");
        else if (!bluetoothAdapter.isEnabled())
            throw new RuntimeException("BluetoothDialog should only be used if Bluetooth is enabled.");

        try {
            listener = (BluetoothDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BluetoothDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //If bluetooth discovery stayed on, turn it off.
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

        //Clean up when done.
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_bluetooth_devices, null);

        //Apply ArrayAdapter to paired devices ListView.
        ListView listPairedDevices = (ListView)view.findViewById(R.id.list_paired_devices);
        pairedDevicesAdapter = new BluetoothDeviceAdapter(getActivity());
        if (listPairedDevices != null) {
            listPairedDevices.setAdapter(pairedDevicesAdapter);
            listPairedDevices.setOnItemClickListener(new ListView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (bluetoothAdapter.isDiscovering()) {
                        Log.i(TAG, "Cancelling discovery.");
                        bluetoothAdapter.cancelDiscovery();
                    }
                    BluetoothDevice device = (BluetoothDevice)((ListView)parent).getAdapter().getItem(position);
                    listener.onBluetoothDialogPositiveClick(BluetoothDialog.this, device);
                    dismiss();
                }
            });
        }

        //Get the paired devices and add them to the adapter.
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            pairedDevicesAdapter.add(device);
        }

        //Apply ArrayAdapter to other devices ListView.
        final ListView listOtherDevices = (ListView)view.findViewById(R.id.list_other_devices);
        listOtherDevices.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
        otherDevicesAdapter = new BluetoothDeviceAdapter(getActivity());
        if (listOtherDevices != null) {
            listOtherDevices.setAdapter(otherDevicesAdapter);
            listOtherDevices.setOnItemClickListener(new ListView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (bluetoothAdapter.isDiscovering()) {
                        Log.i(TAG, "Cancelling discovery.");
                        bluetoothAdapter.cancelDiscovery();
                    }
                    BluetoothDevice device = (BluetoothDevice)((ListView)parent).getAdapter().getItem(position);
                    listener.onBluetoothDialogPositiveClick(BluetoothDialog.this, device);
                    dismiss();
                }
            });
        }

        //Hook-up OnClickListener for scan button.
        Button buttonScan = (Button)view.findViewById(R.id.button_scan);
        if (buttonScan != null) {
            buttonScan.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    otherDevicesAdapter.clear();
                    listOtherDevices.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
                    if (enableBluetooth()) {
                        scanForDevices();
                    }
                }
            });
        }

        builder.setView(view);
        buttonScan.callOnClick();
        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                //Scan for the devices if bluetooth is enabled.
                scanForDevices();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_COARSE_LOCATION) {
            //If the permission request wasn't cancelled and permission was granted.
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDiscovery();
            }
        }
    }

    /**
     * This function checks to see if bluetooth is enabled.
     * If it is not enabled, then the user will be requested to enable it.
     * @return true if bluetooth is currently enabled, false otherwise.
     */
    public boolean enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
            return false;
        } else {
            return true;
        }
    }

    private void scanForDevices() {
        //Enable discovery mode to find devices in proximity.
        //Beginning with Android 6.0 (API Level 23), it is required to obtain permission
        //to access approximate location.
        //This is a dangerous permission and requires explicit runtime permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Check to see if the application already has permission.
            int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                //Request permission from user.
                Log.i(TAG, "ACCESS_COARSE_LOCATION permission denied.  Requesting permission.");
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_COARSE_LOCATION);
            } else {
                Log.i(TAG, "ACCESS_COARSE_LOCATION permission granted.");
                startDiscovery();
            }
        } else {
            Log.i(TAG, "ACCESS_COARSE_LOCATION permission granted.");
            startDiscovery();
        }
    }

    /**
     * This function start bluetooth discovery mode.
     * If discovery mode was already active, it is first cancelled, then restarted.
     */
    private void startDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            Log.i(TAG, "Cancelling discovery.");
            bluetoothAdapter.cancelDiscovery();
        }
        Log.i(TAG, "Starting discovery.");
        bluetoothAdapter.startDiscovery();
    }
}
