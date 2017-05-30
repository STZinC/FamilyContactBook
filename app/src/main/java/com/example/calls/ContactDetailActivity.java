/**************************************************************************************************
 * Copyright (C) 2016 WickerLabs. inc - All Rights Reserved.                                      *
 *                                                                                                *
 * NOTICE:  All information contained herein is, and remains the property of WickerLabs,          *
 * The intellectual and technical concepts contained herein are proprietary to WickerLabs.        *
 * Dissemination of this information or reproduction of this material                             *
 * is strictly forbidden unless prior permission is obtained from WickerLabs. inc                 *
 *                                                                                                *
 **************************************************************************************************/
package com.example.calls;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calls.Adapter.WeatherInfo;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ContactDetailActivity extends AppCompatActivity {
    private MyDataBaseHelper dbHelper;
    String index;
    Integer id;
    ImageView avatorView;
    TextView nameView;
    TextView phoneView;
    TextView relationshipView;
    Button moveBlack;
    WeatherInfo weatherInfo;
    TextView locationView;
    private static final String TAG = "ContactDetailActivity";
    //头像文件
    private File filesave;
    private Uri imageSaveUri;
    //定义异步更新UI，实现线程中更新UI
    public static final int UPDATE_LOACTION = 1;
    public static final int UPDATE_WEATHER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_contact_detail);
        setSupportActionBar(toolbar);
        setTitle("联系人");
        avatorView = (ImageView) findViewById(R.id.contact_detail_avatar);
        nameView = (TextView) findViewById(R.id.contact_detail_name);
        phoneView = (TextView) findViewById(R.id.contact_detail_phone_number);
        relationshipView = (TextView) findViewById(R.id.contact_detail_relationship);
        moveBlack = (Button) findViewById(R.id.move_to_out_black);
        locationView = (TextView) findViewById(R.id.contact_detail_phone_location);

        filesave =  new File(Environment.getExternalStorageDirectory(), "save.jpg");
        if(Build.VERSION.SDK_INT >= 24){
            imageSaveUri = FileProvider.getUriForFile(this,"com.yanyangma.FamilyPhoneBook.fileprovider",filesave);
        }
        else{
            imageSaveUri = Uri.fromFile(filesave);
        }

        iniData();
        iniWeather();
        iniSweetMessageButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_contact_detail,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.delete_contact_detail:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("delete from People where id = ?",new String[]{id.toString()});
                Toast.makeText(this, "Delete succeeded", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case R.id.edit_contact_detail:
                Intent intent = new Intent(this,AddEditContactActivity.class);
                intent.putExtra("index",id.toString());
                startActivity(intent);
                finish();
                break;
            default:
        }
        return true;
    }

    private void iniData(){
        dbHelper = new MyDataBaseHelper(this,"PeopleStore.db",null,1);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        Intent intent = getIntent();
        index = intent.getStringExtra("index");
        String firstFourChar = index;

        if(index.length()>=4) {
            firstFourChar = index.substring(0, 4);
        }

        if(firstFourChar.equals("TEL:")){
            String phoneNumber = index.substring(4);
            Cursor qcursor = db.rawQuery("select * from People where phoneNumber1 = ?",
                    new String[]{phoneNumber});
            //If there exist such phone number in the database.
            if(qcursor.moveToFirst()){
                String name = qcursor.getString(qcursor.getColumnIndex("name"));
                final String phone = qcursor.getString(qcursor.getColumnIndex("phoneNumber1"));
                String relationship = qcursor.getString(qcursor.getColumnIndex("relationship"));
                final Integer isBlack = qcursor.getInt(qcursor.getColumnIndex("isBlack"));
                int resID;
                resID = R.drawable.avatar_boy;
                //resID = getResources().getIdentifier("avatar_boy", "drawable", "com.example.calls");
                String location = qcursor.getString(qcursor.getColumnIndex("phoneLocation"));
                qcursor.close();
                //avatorView.setImageResource(resID);

                try {
                Bitmap photo1 = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageSaveUri));
                avatorView.setImageBitmap(photo1);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }

                nameView.setText(name);
                phoneView.setText(phone);
                relationshipView.setText(relationship);
                locationView.setText(location);
                if(location==null) {
                    findLocation(phone);
                }

                if(isBlack == 1) moveBlack.setText("移出黑名单");

                moveBlack.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        if(isBlack == 1){
                            db.execSQL("update People set isBlack = ? where phoneNumber1 = ?",new
                            String[] {"0",phone});
                            moveBlack.setText("加入黑名单");
                        }
                        else{
                            db.execSQL("update People set isBlack = ? where phoneNumber1 = ?",new
                                    String[] {"1",phone});
                            moveBlack.setText("移出黑名单");
                        }
                    }
                });
            }
            else{
                qcursor.close();
                Unknown();
            }

        }
        else {
            id = Integer.parseInt(index);
            Cursor qcursor = db.rawQuery("select * from People where id = ?",
                    new String[]{id.toString()});
            qcursor.moveToFirst();
            String name = qcursor.getString(qcursor.getColumnIndex("name"));
            final String phone = qcursor.getString(qcursor.getColumnIndex("phoneNumber1"));
            String relationship = qcursor.getString(qcursor.getColumnIndex("relationship"));
            final Integer isBlack = qcursor.getInt(qcursor.getColumnIndex("isBlack"));
            int resID;
            //resID = R.drawable.avatar_boy;
            resID = getResources().getIdentifier("avatar_boy", "drawable", "com.yanyangma.FamilyPhoneBook");
            String location = qcursor.getString(qcursor.getColumnIndex("phoneLocation"));
            qcursor.close();
            locationView.setText(location);
            if(location==null) {
                findLocation(phone);
            }
            //avatorView.setImageResource(resID);
            try {
                Bitmap photo1 = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageSaveUri));
                avatorView.setImageBitmap(photo1);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
            nameView.setText(name);
            phoneView.setText(phone);
            relationshipView.setText(relationship);
            Integer ha = (name+phone).hashCode();
            Log.d("Hash name and phone",ha.toString());
            if(isBlack == 1) moveBlack.setText("移出黑名单");

            moveBlack.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(isBlack == 1){
                        db.execSQL("update People set isBlack = ? where phoneNumber1 = ?",new
                                String[] {"0",phone});
                        moveBlack.setText("加入黑名单");
                        Toast.makeText(getBaseContext(),"成功移出黑名单",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else{
                        db.execSQL("update People set isBlack = ? where phoneNumber1 = ?",new
                                String[] {"1",phone});
                        moveBlack.setText("移出黑名单");
                        Toast.makeText(getBaseContext(),"成功加入黑名单",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }
    }

    private void Unknown(){
        int resID = R.drawable.avatar_boy;//getResources().getIdentifier("avatar_boy", "drawable", "com.example.calls");
        avatorView.setImageResource(resID);
        nameView.setText("Unknown");
        phoneView.setText(index.substring(4));
        relationshipView.setText("");
        moveBlack.setVisibility(View.INVISIBLE);

    }

    private android.os.Handler handler = new android.os.Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case UPDATE_LOACTION:
                    TextView locationView = (TextView) findViewById(R.id.contact_detail_phone_location);
                    String location = (String) msg.obj;
                    locationView.setText(location);
                    iniWeather();
                    break;
                case UPDATE_WEATHER:
                    TextView weatherView = (TextView) findViewById(R.id.contact_detail_weather);
                    weatherView.setText(weatherInfo.getWeather());
                    break;
                default:
                    break;
            }
        }
    };
    private void iniSweetMessageButton(){
        FloatingActionButton sendMessage = (FloatingActionButton) findViewById(R.id.contact_detail_send_sweet_message);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView phoneNumView = (TextView)findViewById(R.id.contact_detail_phone_number);
                final String phoneNum = (String) phoneNumView.getText();
                TextView locationView = (TextView)findViewById(R.id.contact_detail_phone_location);
                final String location = (String)locationView.getText();
                TextView weatherView = (TextView)findViewById(R.id.contact_detail_weather);
                final String weather = (String)weatherView.getText();
                if(location.equals("")) {
                    Toast.makeText(ContactDetailActivity.this, "找不到号码归属地，天气查询失败", Toast.LENGTH_SHORT).show();
                    findLocation(phoneNum);
                    return;
                }else if(weather.equals("")){
                    Toast.makeText(ContactDetailActivity.this, "正在获取天气信息，请稍后再试", Toast.LENGTH_SHORT).show();
                    refreshWeather();
                    return;
                } else{
                    generateMessage(phoneNum, location);
                }

            }
        });
    }
    private  void iniWeather(){
        TextView locationView = (TextView)findViewById(R.id.contact_detail_phone_location);
        final String location = (String)locationView.getText();
        if(location.equals(""))
            return;
        weatherInfo = new WeatherInfo(location);
        //一段时间后刷新
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(400);
                    Message msg = new Message();
                    msg.what = UPDATE_WEATHER;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private  void refreshWeather(){
        if(weatherInfo == null){
            iniWeather();
            return;
        }
        Log.d(TAG, "iniWeahter: "+weatherInfo.getWeather());
        TextView weatherView = (TextView) findViewById(R.id.contact_detail_weather);
        weatherView.setText(weatherInfo.getWeather());
    }

    private void findLocation(final String phoneNum){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(ContextCompat.checkSelfPermission(ContactDetailActivity.this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ContactDetailActivity.this, new String[]{android.Manifest.permission.INTERNET}, 10);
                }else {
                    try {
                        //360API接口
                        String API = "http://cx.shouji.360.cn/phonearea.php?number=";
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(API+phoneNum)
                                .build();
                        Response response = client.newCall(request).execute();
                        String responseData = response.body().string();
                        String location = parseLocation(responseData);

                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("phoneLocation", location);
                        db.update("People", values, "phoneNumber1=?", new String[]{phoneNum});
                        Message msg = new Message();

                        msg.what = UPDATE_LOACTION;
                        msg.obj = location;
                        handler.sendMessage(msg);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private String  parseLocation(String jsonData) {
        try {
            JSONObject jObj = new JSONObject(jsonData);
            JSONObject data = jObj.getJSONObject("data");
            Log.d(TAG, "parseLocation: "+data.getString("city"));

            return data.getString("city");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private  void generateMessage(final String phoneNum, String location){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor qcursor = db.rawQuery("select * from People where phoneNumber1 = ?",
                        new String[]{phoneNum});
                //If there exist such phone number in the database.
                String name = null;
                String relationship = null;
                if(qcursor.moveToFirst()) {
                    name = qcursor.getString(qcursor.getColumnIndex("name"));
                    relationship = qcursor.getString(qcursor.getColumnIndex("relationship"));
                }
                final String finalName = name;
                final String finalRelationship = relationship;
                //联网查找天气并生成对应预报短信
                String sweetMessage = new String();
                if (finalRelationship!=null)
                    sweetMessage+=finalRelationship;
                else if(finalName!=null)
                    sweetMessage+=finalName;
                String weatherMessage = weatherInfo.getWeatherMessage();
                if (weatherMessage == null){
                    Toast.makeText(ContactDetailActivity.this, "未查找到相关天气信息，请重试",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                sweetMessage+= ","+weatherMessage;
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNum));
                intent.putExtra("sms_body", sweetMessage);
                startActivity(intent);
            }
        }).start();
    }


}
