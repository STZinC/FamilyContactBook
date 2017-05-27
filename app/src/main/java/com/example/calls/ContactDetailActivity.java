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

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactDetailActivity extends AppCompatActivity {
    private MyDataBaseHelper dbHelper;
    String index;
    Integer id;
    ImageView avatorView;
    TextView nameView;
    TextView phoneView;
    TextView relationshipView;
    Button moveBlack;
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
        iniData();
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
                int resID = R.drawable.avatar_boy;//getResources().getIdentifier("avatar_boy", "drawable", "com.example.calls");
                qcursor.close();
                avatorView.setImageResource(resID);
                nameView.setText(name);
                phoneView.setText(phone);
                relationshipView.setText(relationship);
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
            int resID = R.drawable.avatar_boy;//getResources().getIdentifier("avatar_boy", "drawable", "com.example.calls");
            qcursor.close();
            avatorView.setImageResource(resID);
            nameView.setText(name);
            phoneView.setText(phone);
            relationshipView.setText(relationship);
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

}
