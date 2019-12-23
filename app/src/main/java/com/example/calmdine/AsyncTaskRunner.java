package com.example.calmdine;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncTaskRunner extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... strings) {
        Log.i("background----------", "result");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i("background", "result");
        }
//        return null;
    }
}
