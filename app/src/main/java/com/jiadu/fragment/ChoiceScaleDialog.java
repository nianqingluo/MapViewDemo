package com.jiadu.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jiadu.mapdemo.MainActivity;
import com.jiadu.mapdemo.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/2/23.
 */
public class ChoiceScaleDialog extends DialogFragment {

    private MainActivity activity = null;
    private View mView;
    private RadioGroup mRg;
    private RadioButton mRb0;
    private RadioButton mRb1;
    private RadioButton mRb2;
    private RadioButton mRb3;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();
        if (mView==null){

            mView=activity.getLayoutInflater().inflate(R.layout.dialog_scale,null);

            initView();

            mRg = (RadioGroup) mView.findViewById(R.id.rg_scale);

            mRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                 
                    switch (checkedId){
                        case R.id.rb0:
                          
                            activity.setMapScale(0);
                            mRb0.setChecked(true);
                        break;
                        case R.id.rb1:
                            mRb1.setChecked(true);
                            activity.setMapScale(1);

                        break;
                        case R.id.rb2:
                            mRb2.setChecked(true);
                            activity.setMapScale(2);
                        break;
                        case R.id.rb3:
                            mRb3.setChecked(true);
                            activity.setMapScale(3);

                        break;
                        default:
                        break;
                    }
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            dismiss();

                        }
                    },300);
                }
            });
        }

        return new AlertDialog.Builder(getActivity(), android.support.v7.appcompat.R.style.Base_Theme_AppCompat_Light_Dialog_Alert)
//                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setIcon(android.R.drawable.star_big_on)
                .setTitle("选择比例尺")
//                .setMessage("提示：地图上一个小格代表10cm")
//                .setSingleChoiceItems(R.array.scale, activity.getMapScale(), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        /* User clicked on a radio button do some stuff */

//                    }
//                })
                .setView(mView)
                .create();
    }

    private void initView() {
        mRb0 = (RadioButton) mView.findViewById(R.id.rb0);
        mRb1 = (RadioButton) mView.findViewById(R.id.rb1);
        mRb2 = (RadioButton) mView.findViewById(R.id.rb2);
        mRb3 = (RadioButton) mView.findViewById(R.id.rb3);

        switch (activity.getMapScale()){
            case 0:

                mRb0.setChecked(true);
            break;
            case 1:
                mRb1.setChecked(true);

            break;
            case 2:
                mRb2.setChecked(true);

            break;
            case 3:

                mRb3.setChecked(true);
            break;
            default:
            break;
        }


    }

}
