package insa_project.bananarchy.utils;

/**
 * Created by pierre on 18/10/17.
 */

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import insa_project.bananarchy.model.Group;

/**
 * Created by Pierre on 26/09/2016.
 */
public class APIConnexion {

    URL url;
    HttpsURLConnection connection;
    public static final String URL_CALENDAR = "https://edt.adrien-thebault.fr/api/calendar.json";
    public static final String URL_GRP = "https://koikege.pierre-boulch.fr/apiGr-v2.json";
    public static final String URL_SETTINGS = "http://bananarchy.adrien-thebault.fr/settings/";
    public static final String URL_ALL_DATA = "http://bananarchy.adrien-thebault.fr/data/all";
    public static final String URL_SETTINGS_AGENDA = URL_SETTINGS+"agenda/";
    public static final String URL_SETTINGS_TRANSPORTATION = URL_SETTINGS+"transportation/";
    public static final String URL_SETTINGS_LOCATION = URL_SETTINGS+"location/";
    public static final String URL_SEND_MAIL = "http://bananarchy.adrien-thebault.fr/data/send_mail";




    //On initialise la connexion vers le serveur
    private InputStream initConnection(String url) throws IOException {
        this.url = new URL(url);
        connection = (HttpsURLConnection) this.url.openConnection();
        connection.setReadTimeout(10000);
        InputStream reponse = connection.getInputStream();
        if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            return connection.getInputStream();
        }
        return null;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static HashMap<String,ArrayList<Group>> getGroups(String response) throws IOException
    {
        HashMap<String,ArrayList<Group>> ret = new HashMap<>();
            try
            {
                JSONArray jsonArr = new JSONArray(response);
                for(int i=1; i<jsonArr.length();i++)
                {
                    JSONObject jsonObj = jsonArr.getJSONObject(i);
                    Iterator<?> keys = jsonObj.keys();

                    while( keys.hasNext() ) {
                        String key = (String) keys.next();
                        JSONArray jsonArrayGr = jsonObj.getJSONArray(key);
                        ArrayList<Group> retour = new ArrayList<>();
                        for(int k=0;k<jsonArrayGr.length();k++)
                        {
                            JSONObject jsonGroupe = jsonArrayGr.getJSONObject(k);
                            Iterator<?> keysGr = jsonGroupe.keys();
                            while(keysGr.hasNext())
                            {
                                String key2 = (String) keysGr.next();
                                JSONArray jsonRessource = new JSONArray(jsonGroupe.getString(key2));
                                ArrayList<String> listRes = new ArrayList<>();
                                for(int j=0;j<jsonRessource.length();j++)
                                {
                                    listRes.add(jsonRessource.getString(j));
                                }

                                retour.add(new Group(key2,listRes));
                            }
                        }
                        ret.put(key,retour);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return ret;
    }





}

