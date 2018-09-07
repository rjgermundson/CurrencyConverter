package com.example.riley.currencyconverter.LoadingDialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Constructs a LoadingDialog as an async task
 */
public class LoadingAsyncTask extends AsyncTask<Activity, Void, Void> {
    private WeakReference<DialogFragment> dialogFragment;

    @Override
    public Void doInBackground(Activity... params) {
        DialogFragment alert = new LoadingDialog();
        this.dialogFragment = new WeakReference<>(alert);
        alert.show(params[0].getFragmentManager(), null);
        return null;
    }

    @Override
    public void onPostExecute(Void result) {
        dialogFragment.get().dismiss();
    }
}
