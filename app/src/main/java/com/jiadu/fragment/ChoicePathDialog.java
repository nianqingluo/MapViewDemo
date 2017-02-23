package com.jiadu.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.jiadu.mapdemo.R;

/**
 * Created by Administrator on 2017/2/23.
 */
public class ChoicePathDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("选择路线")
                .setSingleChoiceItems(R.array.pathspinner, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked on a radio button do some stuff */
                        MapFragment map = (MapFragment) getActivity().getFragmentManager().findFragmentByTag("map");

                        map.setPath(whichButton+1);

                        dismiss();
                    }
                })
                .create();
    }
}
