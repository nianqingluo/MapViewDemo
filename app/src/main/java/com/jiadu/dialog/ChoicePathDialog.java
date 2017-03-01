package com.jiadu.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.jiadu.fragment.MapFragment;
import com.jiadu.mapdemo.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/2/23.
 */
public class ChoicePathDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final MapFragment map = (MapFragment) getActivity().getFragmentManager().findFragmentByTag("map");

        return new AlertDialog.Builder(getActivity(),android.support.v7.appcompat.R.style.Base_Theme_AppCompat_Light_Dialog)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("选择路线")
                .setSingleChoiceItems(R.array.pathspinner, map.mPath-1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked on a radio button do some stuff */
                        map.setPath(whichButton+1);

                        Timer timer = new Timer();

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                dismiss();
                            }
                        },300);
                    }
                })
                .create();
    }
}
