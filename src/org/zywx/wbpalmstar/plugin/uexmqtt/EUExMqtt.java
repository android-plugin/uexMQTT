package org.zywx.wbpalmstar.plugin.uexmqtt;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import android.text.TextUtils;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.ExtendedListener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.plugin.uexmqtt.vo.ConnectVO;
import org.zywx.wbpalmstar.plugin.uexmqtt.vo.LWTVO;
import org.zywx.wbpalmstar.plugin.uexmqtt.vo.NewMessageVO;
import org.zywx.wbpalmstar.plugin.uexmqtt.vo.PublishVO;
import org.zywx.wbpalmstar.plugin.uexmqtt.vo.ResultVO;
import org.zywx.wbpalmstar.plugin.uexmqtt.vo.StatusChangeVO;
import org.zywx.wbpalmstar.plugin.uexmqtt.vo.SubscribeVO;

import java.net.URISyntaxException;

public class EUExMqtt extends EUExBase{

    private MQTT mqtt;
    private CallbackConnection mConnection;
    private int mStatus=-1;
    private static final String BUNDLE_DATA = "data";

    public EUExMqtt(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
    }

    @Override
    protected boolean clean() {
        return false;
    }
    

    @Override
    public void onHandleMessage(Message message) {
        if(message == null){
            return;
        }
        Bundle bundle=message.getData();
        switch (message.what) {

        default:
                super.onHandleMessage(message);
        }
    }

    public boolean init(String[] params) {
        mqtt=new MQTT();
        mStatus=0;
        callbackStatus();
        callBackJsObjectOnUIThread(JsConst.CALLBACK_INIT, "");
        return true;
    }

    public boolean connect(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        int callbackId=-1;
        if (params.length>1){
            callbackId= Integer.parseInt(params[1]);
        }
        if (mqtt==null){
            mqtt=new MQTT();
        }
        String json = params[0];
        ConnectVO connectVO= DataHelper.gson.fromJson(json,ConnectVO.class);
        try {
            mqtt.setHost(connectVO.server,connectVO.port);
            if (!TextUtils.isEmpty(connectVO.username)) {
                mqtt.setUserName(connectVO.username);
            }
            if (!TextUtils.isEmpty(connectVO.password)) {
                mqtt.setPassword(connectVO.password);
            }
            if (!TextUtils.isEmpty(connectVO.clientId)) {
                mqtt.setClientId(connectVO.clientId);
            }
            if (connectVO.keepAliveInterval!=-1) {
                mqtt.setKeepAlive((short) connectVO.keepAliveInterval);
            }
            if (connectVO.LWT!=null&&connectVO.LWT.enable){
                LWTVO lwtvo=connectVO.LWT;
                if (lwtvo.topic!=null){
                    mqtt.setWillTopic(lwtvo.topic);
                }
                if (lwtvo.qos!=-1){
                    mqtt.setWillQos(getQos(lwtvo.qos));
                }
                if (lwtvo.data!=null){
                    mqtt.setWillMessage(lwtvo.data);
                }
                mqtt.setWillRetain(lwtvo.retainFlag);
            }

            mConnection =mqtt.callbackConnection();
            mConnection.listener(new ExtendedListener() {
                @Override
                public void onPublish(UTF8Buffer topic, Buffer body, Callback<Callback<Void>> ack) {
                    NewMessageVO messageVO=new NewMessageVO();
                    messageVO.topic=topic.toString();
                    messageVO.data=new String(body.toByteArray());
                    BDebug.i(DataHelper.gson.toJson(messageVO));
                    ack.onSuccess(new Callback<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            BDebug.i("callback success...");
                        }

                        @Override
                        public void onFailure(Throwable value) {
                            BDebug.i("callback failed...");
                        }
                    });
                    callBackJsObjectOnUIThread(JsConst.ON_NEW_MESSAGE,DataHelper.gson.toJsonTree(messageVO));
                }

                @Override
                public void onConnected() {
                    BDebug.i("onConnected...");
                    mStatus=2;
                    callbackStatus();
                }

                @Override
                public void onDisconnected() {
                    BDebug.i("onDisconnected...");
                    mStatus=4;
                    callbackStatus();
                }

                @Override
                public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
                    BDebug.i("onPublish...");
                    ack.run();
                }

                @Override
                public void onFailure(Throwable value) {
                    BDebug.i("onFailure...");
                }
            });
            final ResultVO resultVO=new ResultVO();
            final int finalCallbackId = callbackId;
            mConnection.connect(new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    BDebug.i("connect success...");
                    mStatus=2;
                    resultVO.isSuccess=true;
                    if(finalCallbackId !=-1){
                        callbackToJs(finalCallbackId,false,0);
                    }else{
                        callBackJsObjectOnUIThread(JsConst.CALLBACK_CONNECT,DataHelper.gson.toJsonTree(resultVO));
                    }
                }

                @Override
                public void onFailure(Throwable value) {
                    resultVO.isSuccess=false;
                    mStatus=4;
                    if(finalCallbackId !=-1){
                        callbackToJs(finalCallbackId,false,1,value.getMessage());
                    }else{
                        callBackJsObjectOnUIThread(JsConst.CALLBACK_CONNECT,DataHelper.gson.toJsonTree(resultVO));
                    }
                }
            });
        } catch (URISyntaxException e) {
            if (BDebug.DEBUG) {
                e.printStackTrace();
            }
        }
        mStatus=1;
        return true;
    }

    private void callbackStatus(){
        StatusChangeVO statusChangeVO=new StatusChangeVO();
        statusChangeVO.status=mStatus;
        callBackJsObjectOnUIThread(JsConst.ON_STATUS_CHANGE,DataHelper.gson.toJsonTree(statusChangeVO));
    }

    public boolean subscribe(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        if (mConnection==null){
            return false;
        }
        int callbackId=-1;
        if (params.length>1){
            callbackId= Integer.parseInt(params[1]);
        }
        String json = params[0];
        SubscribeVO subscribeVO=DataHelper.gson.fromJson(json,SubscribeVO.class);
        Topic[] topics=new Topic[1];
        topics[0]=new Topic(subscribeVO.topic,getQos(subscribeVO.qos));
        final ResultVO resultVO=new ResultVO();
        resultVO.topic =subscribeVO.topic;
        final int finalCallbackId = callbackId;
        mConnection.subscribe(topics, new Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] value) {
                BDebug.i("subscribe success:",new String(value));
                resultVO.isSuccess=true;
                if(finalCallbackId !=-1){
                    callbackToJs(finalCallbackId,false,0,resultVO.topic);
                }else{
                    callBackJsObjectOnUIThread(JsConst.CALLBACK_SUBSCRIBE,DataHelper.gson.toJsonTree(resultVO));
                }
            }

            @Override
            public void onFailure(Throwable value) {
                BDebug.i("subscribe onFailure:",value.getMessage());
                resultVO.isSuccess=false;
                 if(finalCallbackId !=-1){
                    callbackToJs(finalCallbackId,false,1,value.getMessage());
                }else{
                     callBackJsObjectOnUIThread(JsConst.CALLBACK_SUBSCRIBE,DataHelper.gson.toJsonTree(resultVO));
                 }
            }
        });
        return true;
    }

    private QoS getQos(int qos){
        switch (qos){
            case 0:
                return QoS.AT_MOST_ONCE;
            case 1:
                return QoS.AT_LEAST_ONCE;
            case 2:
                return QoS.EXACTLY_ONCE;
            default:
                return QoS.AT_MOST_ONCE;
        }
    }

    public void unsubscribe(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        int callbackId=-1;
        if (params.length>1){
            callbackId= Integer.parseInt(params[1]);
        }
        String json = params[0];
        SubscribeVO unsubscribeVO=DataHelper.gson.fromJson(json,SubscribeVO.class);
        UTF8Buffer[] utf8Buffers=new UTF8Buffer[1];
        utf8Buffers[0]=UTF8Buffer.utf8(unsubscribeVO.topic);
        final ResultVO resultVO=new ResultVO();
        resultVO.topic = unsubscribeVO.topic;
        final int finalCallbackId = callbackId;
        mConnection.unsubscribe(utf8Buffers, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                resultVO.isSuccess=true;
                if(finalCallbackId !=-1){
                     callbackToJs(finalCallbackId,false,0,resultVO.topic);
                }else{
                     callBackJsObjectOnUIThread(JsConst.CALLBACK_UN_SUBSCRIBE,DataHelper.gson.toJsonTree(resultVO));
                 }
            }

            @Override
            public void onFailure(Throwable value) {
                resultVO.isSuccess=false;
                if(finalCallbackId !=-1){
                    callbackToJs(finalCallbackId,false,1,value.getMessage());
                }else{
                    callBackJsObjectOnUIThread(JsConst.CALLBACK_UN_SUBSCRIBE,DataHelper.gson.toJsonTree(resultVO));
                }
            }
        });
    }

    public void publish(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        int callbackId=-1;
        if (params.length>1){
            callbackId= Integer.parseInt(params[1]);
        }
        String json = params[0];
        PublishVO publishVO=DataHelper.gson.fromJson(json,PublishVO.class);
        final ResultVO resultVO=new ResultVO();
        resultVO.id =publishVO.id;
        resultVO.topic=publishVO.topic;
        resultVO.data=publishVO.data;
        final int finalCallbackId = callbackId;
        mConnection.publish(publishVO.topic, publishVO.data.getBytes(), getQos(publishVO.qos),
                publishVO.retainFlag, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        resultVO.isSuccess=true;
                         if(finalCallbackId !=-1){
                            callbackToJs(finalCallbackId,false,0,resultVO.topic);
                        }else{
                             callBackJsObjectOnUIThread(JsConst.CALLBACK_PUBLISH,DataHelper.gson.toJsonTree(resultVO));
                         }
                    }

                    @Override
                    public void onFailure(Throwable value) {
                        resultVO.isSuccess=false;
                         if(finalCallbackId !=-1){
                            callbackToJs(finalCallbackId,false,1,value.getMessage());
                        }else{
                             callBackJsObjectOnUIThread(JsConst.CALLBACK_PUBLISH,DataHelper.gson.toJsonTree(resultVO));
                         }
                    }
                });
    }

    public void disconnect(String[] params) {
        int callbackId=-1;
        if (params.length>0){
            callbackId= Integer.parseInt(params[0]);
        }
        mStatus=3;
        callbackStatus();
        if (mConnection!=null){
            final ResultVO resultVO=new ResultVO();
            final int finalCallbackId = callbackId;
            mConnection.disconnect(new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    resultVO.isSuccess=true;
                    mStatus=4;
                    callbackStatus();
                    mqtt=null;
                    if(finalCallbackId !=-1){
                        callbackToJs(finalCallbackId,false,0);
                    }else{
                        callBackJsObjectOnUIThread(JsConst.CALLBACK_DISCONNECT, DataHelper.gson.toJsonTree(resultVO));
                    }
                }

                @Override
                public void onFailure(Throwable value) {
                    resultVO.isSuccess=false;
                    mqtt=null;
                    if(finalCallbackId !=-1){
                        callbackToJs(finalCallbackId,false,1,value.getMessage());
                    }else{
                        callBackJsObjectOnUIThread(JsConst.CALLBACK_DISCONNECT, DataHelper.gson.toJsonTree(resultVO));
                    }
                }
            });
        }

    }


    private void callBackJsObjectOnUIThread(final String methodName, final Object js){
        if (mContext!=null&&mContext instanceof Activity){
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callBackJsObject(methodName,js);
                }
            });
        }else{
            BDebug.e("context is null or context is not a Activity");
        }
    }

}
