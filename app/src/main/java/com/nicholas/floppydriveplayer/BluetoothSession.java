package com.nicholas.floppydriveplayer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Nicholas on 9/19/2016.
 */
public class BluetoothSession implements Runnable {

    public interface BluetoothSessionListener {
        void onBluetoothSessionFailed(BluetoothSession session, BluetoothDevice device);
        void onBluetoothSessionStarted(BluetoothSession session, BluetoothDevice device);
        void onBluetoothSessionEnded(BluetoothSession session, BluetoothDevice device, boolean fromUser);
    }

    private static final int MAX_TRANSACTION_LENGTH = 1024;

    private final String TAG = "BluetoothSession";

    private BluetoothSocket socket = null;
    private ArrayList<BluetoothSessionListener> listeners = new ArrayList<>();
    private Thread thread = null;
    private boolean running = false;

    private InputStream instream = null;
    private OutputStream outstream = null;

    private ConcurrentLinkedQueue<Byte> readBuffer = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Byte> writeBuffer = new ConcurrentLinkedQueue<>();

    public BluetoothSession(BluetoothSocket socket) {
        this.socket = socket;
    }

    public void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        running = false;
        thread = null;
        broadcastSessionEnded(true);
    }

    private void stopInternal() {
        running = false;
        thread = null;
        broadcastSessionEnded(false);
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        //Obtain the input and output streams.
        try {
            instream = socket.getInputStream();
            outstream = socket.getOutputStream();
        } catch(IOException e) {
            if (instream != null) {
                try {
                    instream.close();
                } catch (IOException ex) {}
            }
            if (outstream != null) {
                try {
                    outstream.close();
                } catch (IOException ex) {}
            }
            broadcastSessionFailed();
            stopInternal();
        }

        broadcastSessionStarted();

        while (running) {
            try {
                //Write up to 1024 bytes to the output stream.
                int counter = 0;
                Byte current;
                byte[] buffer = new byte[MAX_TRANSACTION_LENGTH];
                while(counter < MAX_TRANSACTION_LENGTH && (current = writeBuffer.poll()) != null) {
                    Log.i("OUTSTREAM", String.format("0x%02X", current));
                    buffer[counter++] = current;
                }
                outstream.write(buffer, 0, counter);
                outstream.flush();

                //Read up to 1024 bytes from the input stream.
                int numToRead = Math.min(instream.available(), MAX_TRANSACTION_LENGTH);
                buffer = new byte[numToRead];
                instream.read(buffer, 0, numToRead);
                for (int index = 0; index < buffer.length; index++) {
                    readBuffer.add(buffer[index]);
                }

            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to close BluetoothSocket.");
                } finally {
                    stopInternal();
                    return;
                }
            }
        }

        //Clean-up if stopped normally.
        try {
            socket.close();
        } catch (IOException ex) {
            Log.e(TAG, "Failed to close BluetoothSocket.");
        }
    }

    public byte[] read(int length) {
        int numRead = 0;
        Byte current = null;
        byte[] data = new byte[length];
        while (numRead < length && (current = readBuffer.poll()) != null) {
            data[numRead++] = current;
        }
        return data;
    }

    public void write(byte[] data) {
        for (int index = 0; index < data.length; index++) {
            writeBuffer.add(data[index]);
        }
    }

    public void addListener(BluetoothSessionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(BluetoothSessionListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private void broadcastSessionFailed() {
        for (BluetoothSessionListener listener : listeners) {
            listener.onBluetoothSessionFailed(this, socket.getRemoteDevice());
        }
    }

    private void broadcastSessionStarted() {
        for (BluetoothSessionListener listener : listeners) {
            listener.onBluetoothSessionStarted(this, socket.getRemoteDevice());
        }
    }

    private void broadcastSessionEnded(boolean fromUser) {
        for (BluetoothSessionListener listener : listeners) {
            listener.onBluetoothSessionEnded(this, socket.getRemoteDevice(), fromUser);
        }
    }
}
