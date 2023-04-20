package com.filenko.conspectnote.common;

import android.content.Context;
import android.widget.Toast;

public class ShowMessage {
    public static void showMessage(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }
}
