package com.jiadu.mapdemo.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

	public static Toast toast;
	
	public static void makeToast(Context context,String content){
		
		if(toast==null){
			
			toast=Toast.makeText(context, content, Toast.LENGTH_SHORT);
			
		}else{
			toast.setText(content);
		}

		toast.show();
		
	}
}
