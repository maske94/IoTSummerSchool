package ch.supsi.dti.ssiot.shimmer;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Main application class
 */
public class MyApplication extends Application {

    public void onCreate() {

        super.onCreate();

        /**
         * Sets up Stetho
         */
        Stetho.initializeWithDefaults(this);
    }
}
