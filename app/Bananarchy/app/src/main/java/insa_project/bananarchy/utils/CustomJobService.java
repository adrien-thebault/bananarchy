package insa_project.bananarchy.utils;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


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
    public static ConnectBluetoothThread mConnectedThread = null;
    final int handlerState = 0;

    protected static String agenda = null;
    protected static String weather = null;
    protected static String travelTime = null;
    protected static String preparationTime = null;
    protected static boolean timestampSent = false;

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
                Log.d("MESSAGE",msg.toString());
                Toast.makeText(getApplicationContext(),"BANANARCHY - "+msg.toString(),Toast.LENGTH_LONG).show();
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



        protected void getAndSendData(){
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, APIConnexion.URL_ALL_DATA,null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            // Display the first 500 characters of the response string.
                            Log.d("BANANARCHY",response.toString());
                            try {
                                final String[] bluetoothRequest = {""};
                                JSONObject agendaJSON = response.getJSONObject("agenda");
                                JSONObject weatherJSON = response.getJSONObject("weather");
                                int travelTimeJSON = response.getInt("travel_time");

                                String agendaRequest = "AGENDA " + agendaJSON.getInt("beginning") + ";" + agendaJSON.getString("summary") + ";" + agendaJSON.getString("location");
                                String weatherRequest = "WEATHER "+weatherJSON.getString("weather")+";"+weatherJSON.getInt("temp");
                                String travelTimeRequest = "TRAVEL_TIME "+travelTimeJSON;

                                if(!timestampSent){
                                    bluetoothRequest[0] = "TIMESTAMP "+response.getInt("timestamp");
                                    timestampSent = true;
                                }
                                /*else if(!agendaRequest.equals(CustomJobService.this.agenda)) {
                                    bluetoothRequest = agendaRequest;
                                    CustomJobService.this.agenda = agendaRequest;
                                }*/ else if (!weatherRequest.equals(CustomJobService.this.weather)){
                                    bluetoothRequest[0] = weatherRequest;
                                    CustomJobService.this.weather = weatherRequest;
                                } else if(!travelTimeRequest.equals(CustomJobService.this.travelTime)){
                                    bluetoothRequest[0] = travelTimeRequest;
                                    CustomJobService.this.travelTime = travelTimeRequest;
                                } else {
                                    JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, APIConnexion.URL_SETTINGS, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response1) {
                                                    try {
                                                        String preparationTimeValue = "";
                                                        for(int i =0; i<response1.length(); i++){
                                                            JSONObject currentObj = response1.getJSONObject(i);
                                                            if(currentObj.getString("name").equals("preparation_time")){
                                                                preparationTimeValue = currentObj.getString("value");
                                                            }
                                                        }
                                                        Log.d("TEST RECEPTION",response1.toString());
                                                        if(!preparationTimeValue.equals("")) {
                                                            String preparationTimeRequest = "PREPARATION_TIME " + preparationTimeValue;
                                                            if (!preparationTimeRequest.equals(CustomJobService.preparationTime)) {
                                                                bluetoothRequest[0] = preparationTimeRequest;
                                                                CustomJobService.this.preparationTime = preparationTimeRequest;
                                                                mConnectedThread.write(bluetoothRequest[0]);
                                                            }
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {

                                        }
                                    });
                                    Log.d("BANANARCHY","preparation time");
                                    RequestQueue queue1 = Volley.newRequestQueue(getApplicationContext());
                                    queue1.add(jsonRequest);

                                }

                                if(!bluetoothRequest[0].equals(""))
                                    mConnectedThread.write(bluetoothRequest[0]);
                                    //Log.d("BANANARCHY",bluetoothRequest);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("That didn't work!","ERROR");
                }
            });
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(stringRequest);
        }

        @Override
        protected Boolean doInBackground(String ... params) {
            if(mConnectedThread == null) {
                try {
                    if(init()){
                        getAndSendData();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                getAndSendData();
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




    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }
    private boolean init() throws IOException {
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
                    /*if (device.getAddress().equals("20:16:11:07:31:71"))
                        myDevice = device;
                        */
                    if(device.getName().equals("Xperia Z2"))
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
                    mConnectedThread = new ConnectBluetoothThread(btSocket,getApplicationContext());
                    mConnectedThread.start();

                    //I send a character when resuming.beginning transmission to check device is connected
                    //If it is not an exception will be thrown in the write method and finish() will be called
                    return true;
                }
                else {
                    //Toast.makeText(getBaseContext(), "Thingz introuvable", Toast.LENGTH_LONG).show();
                    Log.d("ERROR","thingz introuvable");
                    error.obtainMessage(handlerState, 25, -1, "test").sendToTarget();
                    return false;
                }
            }
        }
        return false;
    }

}
