package com.famoco.morphodemo.utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.famoco.morphodemo.R;

public class DialogUtils {

    public static AlertDialog createInfoDialog(Activity activity, String productInfo, String softwareInfo) {
        //TODO generaliser ce dialog pour matcher avec les dialog morpho errors
        LayoutInflater inflater = activity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_info_layout, null);
        ImageButton exit = alertLayout.findViewById(R.id.exit_button);
        TextView title = alertLayout.findViewById(R.id.title);
        title.setText("Product & Software Info");
        TextView content = alertLayout.findViewById(R.id.content);
        content.setText(activity.getString(R.string.DIALOG_PRODUCT_SOFTWARE_INFO, productInfo, softwareInfo));

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(alertLayout);
        final AlertDialog alertDialog = builder.create();
        //Needed to see the borders
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        return alertDialog;
    }

    public static AlertDialog createValidDialog(final Activity activity, String contentStr) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_valid_layout, null);
        ImageButton exit = alertLayout.findViewById(R.id.exit_button);
        TextView content = alertLayout.findViewById(R.id.content);
        content.setText(contentStr);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(alertLayout);
        final AlertDialog alertDialog = builder.create();
        //Needed to see the borders
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                activity.onBackPressed();
            }
        });
        return alertDialog;
    }

    public static AlertDialog createErrorDialog(final Activity activity, String contentStr) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_error_layout, null);
        ImageButton exit = alertLayout.findViewById(R.id.exit_button);
        TextView content = alertLayout.findViewById(R.id.content);
        content.setText(contentStr);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(alertLayout);
        final AlertDialog alertDialog = builder.create();
        //Needed to see the borders
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                activity.onBackPressed();
            }
        });
        return alertDialog;
    }
}
