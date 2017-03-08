package com.jiadu.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.jiadu.mapdemo.util.LogUtil;

import org.json.JSONException;
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

        LogUtil.debugLog("广播接收的消息："+msg);

        try {
            JSONObject jsonObject = new JSONObject(msg);

            String type = (String) jsonObject.get("type");
            
            if ("command".equals(type)){//接收到的是命令

               handleCommandMessage(jsonObject);

            }else if ("state".equals(type)){//接收到的是状态

                handleStateMessage(jsonObject);

            }else if ("param".equals(type)){//接收到的是参数

                handleParamMessage(jsonObject);

            }

        } catch (JSONException e) {
            e.printStackTrace();
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
