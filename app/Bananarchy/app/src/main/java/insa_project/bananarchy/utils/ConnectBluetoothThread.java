package insa_project.bananarchy.utils;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by pierre on 18/12/17.
 */

public class ConnectBluetoothThread extends Thread {

    private static InputStream mmInStream = null;
    private static OutputStream mmOutStream = null;
    private static Context context;

    //creation of the connect thread
    public ConnectBluetoothThread(BluetoothSocket socket, Context context) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            //Create I/O streams for connection
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        this.context = context;
    }

    public static boolean isInit(){
        return (mmInStream!=null && mmOutStream != null && context != null);
    }
    public void run() {
        byte[] buffer = new byte[512];
        int bytes;

        // Keep looping to listen for received messages
        while (true) {
            try {
                bytes = mmInStream.read(buffer);            //read bytes from input buffer
                String readMessage = new String(buffer, 0, bytes);
                // Send the obtained bytes to the UI Activity via handler
                //bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                Log.d("MESSAGE RECU",readMessage);
                readMessage = readMessage.replaceAll("\\s+","");
                if(readMessage.equals("P")){
                    Log.d("BANANARCHY","DONE");
                    UTF8StringRequest stringRequest = new UTF8StringRequest(Request.Method.POST, APIConnexion.URL_SEND_MAIL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("BANANARCHY","MAIL ENVOYÉ");
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("That didn't work!","ERROR");
                        }
                    });
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    RequestQueue queue = Volley.newRequestQueue(context);
                    queue.add(stringRequest);
                }
            } catch (IOException e) {
                break;
            }
        }
    }
    //write method
    public static void write(String input) {
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
        try {
            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            Log.d("WRITEBLUE",input);
        } catch (IOException e) {
            //if you cannot write, close the application
            e.printStackTrace();
            Log.d("BANANARCHY","BLUETOOTH WRITE ERROR");
        }
    }
}