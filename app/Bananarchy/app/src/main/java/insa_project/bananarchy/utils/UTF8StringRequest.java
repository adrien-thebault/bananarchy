package insa_project.bananarchy.utils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * Created by pierre on 14/12/17.
 */

public class UTF8StringRequest extends StringRequest {


    public UTF8StringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public UTF8StringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    protected Response<String> parseNetworkResponse (NetworkResponse response) {
        try {
            String utf8String = new String(response.data, "UTF-8");
            return Response.success(new String(utf8String), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            // log error
            return Response.error(new ParseError(e));
        }
    }
}
