package insa_project.bananarchy.utils;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import insa_project.bananarchy.activities.SettingsActivity;
import insa_project.bananarchy.activities.SyncActivity;
import insa_project.bananarchy.bdd.GroupDAO;
import insa_project.bananarchy.bdd.LevelDAO;
import insa_project.bananarchy.bdd.RessourcesDAO;
import insa_project.bananarchy.model.Group;
import insa_project.bananarchy.model.Level;

import static android.support.v4.app.ActivityCompat.startActivityForResult;


/**
 * Created by pierre on 17/12/17.
 */

public class CustomJobService extends JobService {

    JobParameters params = null;
    private OutputStream bluetoothOutputStream;
    private InputStream bluetoothInStream;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket = null;
    Handler bluetoothIn;
    Handler error;
    private static ConnectBluetoothThread mConnectedThread = null;
    final int handlerState = 0;

    @Override
    public boolean onStartJob(final JobParameters params) {
        this.params = params;
        if(Looper.myLooper()==null)
            Looper.prepare();
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Toast.makeText(getApplicationContext(),msg.toString(),Toast.LENGTH_LONG).show();
            }
        };

        error = new Handler(){
            public void handleMessage(android.os.Message msg){
                Log.d("MESSAGE","Test");
            }
        };
        AsyncTaskJob asyncTaskJob = new AsyncTaskJob();
        asyncTaskJob.execute();

        return true;


    }

    @Override
    public boolean onStopJob(final JobParameters params) {
        return false;
    }


    private class AsyncTaskJob extends AsyncTask<String,Void,Boolean> {

        @Override
        protected Boolean doInBackground(String ... params) {
            if(mConnectedThread == null) {
                try {
                    init();
                    Log.d("BANA","INIT DONE");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }


        // Méthode exécutée à la fin de l'execution de la tâche asynchrone
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.d("JOBBANA","JOB DONE");
            jobFinished(params, true);
        }
    }


    private class ConnectBluetoothThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectBluetoothThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    Log.d("MESSAGE","RECU");
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                Log.d("WRITEBLUE",input);
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }
    private void init() throws IOException {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                //Toast.makeText(getBaseContext(), "VEUILLEZ VOUS CONNECTER AU THINGZ", Toast.LENGTH_LONG).show();
                Log.d("ERROR","VEUILLEZ VOUS CONNECTER AU THINGZ");
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                BluetoothDevice myDevice = null;
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    if (device.getAddress().equals("20:16:11:07:31:71"))
                        myDevice = device;
                    Log.d("NAME", deviceName);
                    Log.d("ADDRESS", deviceHardwareAddress);
                }
                if (myDevice != null) {
                    try {
                        btSocket = createBluetoothSocket(myDevice);
                    } catch (IOException e) {
                        //Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
                        Log.d("CONNECTION error","socket creation failed");
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        btSocket.connect();
                        //Toast.makeText(getBaseContext(), "Connection done", Toast.LENGTH_LONG).show();
                        Log.d("CONNECTION DONE","connection");
                    } catch (IOException e) {
                        try {
                            btSocket.close();
                        } catch (IOException e2) {
                            //insert code to deal with this
                        }
                    }
                    mConnectedThread = new ConnectBluetoothThread(btSocket);
                    mConnectedThread.start();

                    //I send a character when resuming.beginning transmission to check device is connected
                    //If it is not an exception will be thrown in the write method and finish() will be called
                    mConnectedThread.write("AGENDA 150");
                }
                else {
                    //Toast.makeText(getBaseContext(), "Thingz introuvable", Toast.LENGTH_LONG).show();
                    Log.d("ERROR","thingz introuvable");
                    error.obtainMessage(handlerState, 25, -1, "test").sendToTarget();
                }
            }
        }
    }

}
