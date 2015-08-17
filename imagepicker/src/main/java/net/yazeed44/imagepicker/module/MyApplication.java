package net.yazeed44.imagepicker.module;

import android.content.Context;
import android.widget.Toast;

import javax.inject.Inject;

/**
 * Created by yazeed44 on 8/15/15.
 */
public class MyApplication extends android.app.Application {


    @Inject
    Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();


        Toast.makeText(this, appContext.toString(), Toast.LENGTH_SHORT).show();
    }
}
