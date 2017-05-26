package com.example.calls;

import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

/**
 * 黑名单电话服务
 * @author liuyazhuang
 *
 */
public class BlackNumberService extends Service {

    private TelephonyManager tm;
    private MyPhoneStateListener listener;
    private MyDataBaseHelper dbHelper;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        listener = new MyPhoneStateListener();
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        dbHelper = new MyDataBaseHelper(this,"PeopleStore.db",null,1);
        Log.d("Service:","Black number service start: "+Thread.currentThread().getName());
    }


    public boolean isBlackNumber(String inComingNumber){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor qcursor = db.rawQuery("select * from People where phoneNumber1 = ? and isBlack = 1",
                new String[]{inComingNumber});
        if(qcursor.moveToFirst()){
            qcursor.close();
            return true;
        }
        else return false;
    }

    private final class MyPhoneStateListener extends PhoneStateListener{

        private long startTime = 0;
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // TODO Auto-generated method stub
            super.onCallStateChanged(state, incomingNumber);
            Log.d("IncomingNumber","State change:" + state );
            Log.d("CALL_STATE_RINGING",": "+TelephonyManager.CALL_STATE_RINGING);
            Log.d("IncomingNumber",incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d("IncomingNumber",incomingNumber);
                    boolean isBlackNumber = isBlackNumber(incomingNumber);
                    if(isBlackNumber){
                        Log.d("isBlack: ","true");
                        endCall(incomingNumber);
                        return;
                    }
                    else {
                        Log.d("isBlack: ","false");
                    }
                    startTime = System.currentTimeMillis();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:

                    break;
                case TelephonyManager.CALL_STATE_IDLE:
//                    long endTime = System.currentTimeMillis();
//                    //来电一声响
//                    if(endTime - startTime < 3000){
//                        //发送通知
//                        Notification notification = new Notification(android.R.drawable.stat_notify_missed_call, "拦截到来电一声响", System.currentTimeMillis());
//                        Intent intent = new Intent(getApplicationContext(),BlackNumberListActivity.class);
//                        intent.putExtra("number", incomingNumber);
//                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 100, intent, 0);
//                        notification.setLatestEventInfo(getApplicationContext(), "来电一声响", "拦截到来电一声响", contentIntent);
//                        notification.flags = Notification.FLAG_AUTO_CANCEL;
//                        nm.notify(100, notification);
//                    }
                    break;

                default:
                    Log.d("IncomingNumber",incomingNumber);
                    break;
            }
        }

    }

    //挂断电话
    private void endCall(String incomingNumber){
        try {
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            Method method = clazz.getMethod("getService", String.class);
            IBinder ibinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
            ITelephony iTelephony = ITelephony.Stub.asInterface(ibinder);
            iTelephony.endCall();

            //删除通话记录 通话记录的保存是一个异步的操作，需要使用ContentObserver技术来实现
            Uri uri = Calls.CONTENT_URI;
            getContentResolver().registerContentObserver(uri, true, new MyContentObserver(new Handler(),incomingNumber));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private final class MyContentObserver extends ContentObserver{

        private String incomingNumber;
        public MyContentObserver(Handler handler, String incomingNumber) {
            super(handler);
            // TODO Auto-generated constructor stub
            this.incomingNumber = incomingNumber;
        }

        @Override
        public void onChange(boolean selfChange) {
            // TODO Auto-generated method stub
            super.onChange(selfChange);
            Uri uri = Calls.CONTENT_URI;
            String where = Calls.NUMBER + " = ?";
            String[] selectionArgs = new String[]{incomingNumber};
            getContentResolver().delete(uri, where, selectionArgs);

            //解除监听
            getContentResolver().unregisterContentObserver(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d("Service:","Black number service end");
        //取消状态监听
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
    }
}
