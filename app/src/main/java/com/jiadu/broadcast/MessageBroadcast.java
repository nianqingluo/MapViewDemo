package com.jiadu.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/3.
 */
public class MessageBroadcast extends BroadcastReceiver {

    public void setListener(MessageListener listener) {

        if (listener==null){
            return;
        }

        this.listener = listener;
    }

    private MessageListener listener=null;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();

        String msg = bundle.getString("msg");

        if (listener!=null){
            listener.receiveMessage(msg);
        }
    }

    /**
     * 处理参数的函数
     * @param jsonObject:json数据
     */
    private void handleParamMessage(JSONObject jsonObject) {

    }

    /**
     * 处理状态的函数
     * @param jsonObject:json数据
     */
    private void handleStateMessage(JSONObject jsonObject) {

    }

    /**
     * 处理命令的函数
     * @param jsonObject:json数据
     */
    private void handleCommandMessage(JSONObject jsonObject) {

    }

    public interface MessageListener{

        public void receiveMessage(String message);

    }
}
