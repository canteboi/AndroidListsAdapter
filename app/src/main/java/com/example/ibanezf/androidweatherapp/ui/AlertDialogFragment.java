package com.example.ibanezf.androidweatherapp.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.example.ibanezf.androidweatherapp.R;

/**
 * Created by ibanezf on 11/29/2015.
 */
public class AlertDialogFragment extends DialogFragment {
    //region Properties
    private String AlertTitle;
    private String AlertMessage;
    private String PositiveBtnText;
    private Boolean IsCustomAlert = false;

    public Boolean getIsCustomAlert() {
        return IsCustomAlert;
    }

    public void setIsCustomAlert(Boolean isCustomAlert) {
        IsCustomAlert = isCustomAlert;
    }

    public String getAlertTitle() {

        return AlertTitle;
    }

    public void setAlertTitle(String alertTitle) {
        AlertTitle = alertTitle;
    }

    public String getAlertMessage() {
        return AlertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        AlertMessage = alertMessage;
    }

    public String getPositiveBtnText() {
        return PositiveBtnText;
    }

    public void setPositiveBtnText(String positiveBtnText) {
        PositiveBtnText = positiveBtnText;
    }


    //endregion

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (IsCustomAlert){
            builder.setTitle(AlertTitle)
                    .setMessage(AlertMessage)
                    .setPositiveButton(R.string.dialog_positive_confirm, null);
        }
        else   {
            builder.setTitle(R.string.error_title)
                    .setMessage(R.string.error_message)
                    .setPositiveButton(R.string.dialog_positive_confirm, null);
        }

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
