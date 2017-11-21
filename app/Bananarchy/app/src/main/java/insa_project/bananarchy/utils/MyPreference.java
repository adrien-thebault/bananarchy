package insa_project.bananarchy.utils;

import android.content.Context;
import android.preference.Preference;

/**
 * Created by pierre on 18/11/17.
 */

public class MyPreference extends Preference{


    private long value;

    public MyPreference(Context context) {
        super(context);
    }

    public MyPreference(Context context, long value){
        super(context);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }


}
