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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.calls.Adapter.PeopleAdapter;

import java.util.ArrayList;
import java.util.List;

public class BlackListActivity extends AppCompatActivity {


    private ArrayList<People> peopleList = new ArrayList<>();
    private PeopleAdapter adapter;
    private DrawerLayout mDrawerLayout;
    private MyDataBaseHelper dbHelper;
    private int BoF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_list);
        Intent intent = getIntent();
        BoF = intent.getIntExtra("BoF",0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_black_list);
        setSupportActionBar(toolbar);
        if(BoF==0) {
            setTitle("黑名单");
        }
        else{
            setTitle("家人");
        }
        ActionBar actionBar = getSupportActionBar();

//        if(actionBar != null){
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
//        }


        dbHelper = new MyDataBaseHelper(this,"PeopleStore.db",null,1);
        iniPeoples();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.black_list_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PeopleAdapter(peopleList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume(){
        super.onResume();
        iniPeoples();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.black_list_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PeopleAdapter(peopleList);
        recyclerView.setAdapter(adapter);
    }

    private void iniPeoples(){
        peopleList.clear();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor;
        if(BoF==0) {
            cursor = db.rawQuery("select * from People where isBlack = ?",
                    new String[]{"1"});
        }
        else{
            cursor = db.rawQuery("select * from People where not relationship = ?",
                    new String[]{"无"});
        }
        if (cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String phoneNumber1 = cursor.getString(cursor.getColumnIndex("phoneNumber1"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                People person = new People(name,phoneNumber1,id);
                person.setPinyin(HanziToPinyin.getPinYin(person.getName()));
                peopleList.add(person);
            }while(cursor.moveToNext());
        }
        cursor.close();
    }
}


