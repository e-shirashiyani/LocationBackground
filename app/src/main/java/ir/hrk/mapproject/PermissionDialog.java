package ir.hrk.mapproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

public class PermissionDialog {


    public static void showRequestLocationPermissionDialog(Context context, ICallBackDialogPermission iCallBack) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_access_location, null, false);
        dialog.setView(view);
        dialog.setCancelable(false);

        AppCompatButton btnAccept = view.findViewById(R.id.btn_Accept);
        AppCompatTextView txtDecline = view.findViewById(R.id.txt_decline);


        AlertDialog alertDialog = dialog.create();

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                iCallBack.onAccept();
            }
        });

        txtDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                iCallBack.onDecline();
            }
        });


        alertDialog.show();
    }


    public interface ICallBackDialogPermission {

        void onAccept();

        void onDecline();

    }

}
