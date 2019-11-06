package com.nicholas.floppydriveplayer;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.os.ParcelUuid;
import android.view.Gravity;
import android.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends Activity implements BluetoothDialog.BluetoothDialogListener, BluetoothSession.BluetoothSessionListener {

    private final String TAG = "MainActivity";

    private BluetoothAdapter bluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;

    private final int BT_TASK_NONE = 0;
    private final int BT_TASK_CONNECT_SECURE = 1;
    private final int BT_TASK_CONNECT_INSECURE = 2;
    private final int BT_TASK_START_CONNECTION = 3;

    private int requestedBluetoothTask = BT_TASK_NONE;

    private BluetoothDevice deviceToConnect = null;
    private boolean startAsSecure = true;
    private BluetoothSession session = null;

    private PianoRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("LifeCycle", "onCreate");
        setContentView(R.layout.activity_main);

        //Create toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setActionBar(toolbar);

        //Check to see if bluetooth is supported.
        //By finishing the activity in case it's not supported, we can guarantee
        //that bluetooth can be used through out all areas.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //If null, then bluetooth is not supported.  Display an error.
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Bluetooth is not supported on your device.",
                    Toast.LENGTH_LONG);
            toast.show();
            finish();
        }

        renderer = new PianoRenderer(getApplicationContext(), 7);
        //Allow the piano renderer to control when it needs to be updated.
        renderer.setRenderMode(GLTextureViewRenderer.RENDERMODE_WHEN_DIRTY);

        TextureView textureView = (TextureView) findViewById(R.id.texture_piano);
        textureView.setSurfaceTextureListener(renderer);
        textureView.setOnTouchListener(renderer);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seek_octave);
        seekBar.setOnSeekBarChangeListener(renderer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_disconnect).setEnabled(session != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connect_secure:
                if (enableBluetooth()) {
                    executeRequestedBluetoothTask(BT_TASK_CONNECT_SECURE);
                } else {
                    //Set global so that the onActivityResult can execute the task.
                    requestedBluetoothTask = BT_TASK_CONNECT_SECURE;
                }
                return true;
            case R.id.action_connect_insecure:
                if (enableBluetooth()) {
                    executeRequestedBluetoothTask(BT_TASK_CONNECT_INSECURE);
                } else {
                    //Set global so that the onActivityResult can execute the task.
                    requestedBluetoothTask = BT_TASK_CONNECT_INSECURE;
                }
                return true;
            case R.id.action_disconnect:
                if (session != null) {
                    session.stop();
                    session.removeListener(this);
                    session.removeListener(renderer);
                }
                session = null;
                invalidateOptionsMenu();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("LifeCycle", "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("LifeCycle", "onResume");

        renderer.start();
        SeekBar seekBar = (SeekBar) findViewById(R.id.seek_octave);
        seekBar.setProgress(seekBar.getProgress());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("LifeCycle", "onPause");

        if (session != null) {
            session.removeListener(this);
            session.stop();
            session.removeListener(renderer);
        }
        session = null;

        renderer.stop();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("LifeCycle", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("LifeCycle", "onDestroy");
    }

    public void startSequencerMode(View view) {
        Log.i(TAG, "Starting sequencer mode.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //Process whichever task needed bluetooth to be enabled
                executeRequestedBluetoothTask(requestedBluetoothTask);
            }
        }
    }

    /**
     * This function checks to see if bluetooth is enabled.
     * If it is not enabled, then the user will be requested to enable it.
     *
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

    /**
     * This function is called whenever a bluetooth task needs to be executed.
     * Since bluetooth can be disabled by the user at anytime, it is required to
     * request it to be enabled before attempting to use the bluetooth adapter.
     * Therefore, when the bluetooth adapter is enabled by the user when requested
     * by this application, this function should be called with the appropriate
     * task that needs to be executed.
     * This function should not be called while bluetooth is not enabled.
     */
    private void executeRequestedBluetoothTask(int taskId) {
        BluetoothDialog dialog;

        switch (taskId) {
            case BT_TASK_CONNECT_SECURE:
                dialog = new BluetoothDialog();
                dialog.show(getFragmentManager(), null);
                startAsSecure = true;
                break;
            case BT_TASK_CONNECT_INSECURE:
                dialog = new BluetoothDialog();
                dialog.show(getFragmentManager(), null);
                startAsSecure = false;
                break;
            case BT_TASK_START_CONNECTION:
                startConnection();
                break;
        }
    }

    @Override
    public void onBluetoothDialogPositiveClick(DialogFragment dialog, BluetoothDevice device) {
        deviceToConnect = device;
        if (enableBluetooth()) {
            executeRequestedBluetoothTask(BT_TASK_START_CONNECTION);
        } else {
            requestedBluetoothTask = BT_TASK_START_CONNECTION;
        }
    }

    public void startConnection() {
        //Make sure the bluetooth device is still available.
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceToConnect.getAddress());

        //Only connect to serial boards.
        UUID bluetoothSerialBoardUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            if (startAsSecure) {
                ParcelUuid[] uuids = device.getUuids();
                if (uuids != null) {
                    for (ParcelUuid uuid : uuids) {
                        Log.i("UUID", uuid.getUuid().toString());
                    }
                }
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(bluetoothSerialBoardUUID);
                socket.connect(); //Blocks until a connection is established (or fails with an exception).
                session = new BluetoothSession(socket);
                session.addListener(this);
                session.addListener(renderer);
                session.start();
                invalidateOptionsMenu();
            } else {
                ParcelUuid[] uuids = device.getUuids();
                if (uuids != null) {
                    for (ParcelUuid uuid : uuids) {
                        Log.i("UUID", uuid.getUuid().toString());
                    }
                }
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(bluetoothSerialBoardUUID);
                socket.connect(); //Blocks until a connection is established (or fails with an exception).
                session = new BluetoothSession(socket);
                session.addListener(this);
                session.addListener(renderer);
                session.start();
                invalidateOptionsMenu();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to device: " + device.getName() + ", MAC: " + device.getAddress() +
                "\n" + e.getMessage());
            Toast toast = Toast.makeText(getApplicationContext(), "Error connecting to device.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public void onBluetoothSessionFailed(BluetoothSession session, final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), "Error connecting to device.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    @Override
    public void onBluetoothSessionStarted(BluetoothSession session, final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            public void run() {
                String name = device.getName();
                if (name == null || name.isEmpty() ) {
                    name = "(null)";
                }
                Toast toast = Toast.makeText(getApplicationContext(), "Connected to " + name, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    @Override
    public void onBluetoothSessionEnded(BluetoothSession session, final BluetoothDevice device, boolean fromUser) {
        //Only notify if the user didn't stop.
        if(!fromUser) {

        }

        runOnUiThread(new Runnable() {
            public void run() {
                String name = device.getName();
                if (name == null || name.isEmpty()) {
                    name = "(null)";
                }
                Toast toast = Toast.makeText(getApplicationContext(), "Disconnected from " + name, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }
}
