package com.example.riley.currencyconverter.LoadingDialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.AsyncTask;

public class LoadingAsyncTask extends AsyncTask<Activity, Void, DialogFragment> {

    @Override
    public DialogFragment doInBackground(Activity... params) {
        DialogFragment alert = new LoadingDialog();
        alert.show(params[0].getFragmentManager(), null);
        return alert;
    }

    @Override
    public void onPostExecute(DialogFragment result) {
        result.dismiss();
    }
}
