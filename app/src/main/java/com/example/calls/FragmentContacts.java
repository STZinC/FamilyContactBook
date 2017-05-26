/**************************************************************************************************
 * Copyright (C) 2016 WickerLabs. inc - All Rights Reserved.                                      *
 *                                                                                                *
 * NOTICE:  All information contained herein is, and remains the property of WickerLabs,          *
 * The intellectual and technical concepts contained herein are proprietary to WickerLabs.        *
 * Dissemination of getContext() information or reproduction of getContext() material                             *
 * is strictly forbidden unless prior permission is obtained from WickerLabs. inc                 *
 *                                                                                                *
 **************************************************************************************************/
package com.example.calls;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.calls.Adapter.PeopleAdapter;

import java.util.ArrayList;
import java.util.List;

public class FragmentContacts extends Fragment {

    private ArrayList<People> peopleList = new ArrayList<>();
    private PeopleAdapter adapter;
    private DrawerLayout mDrawerLayout;
    private MyDataBaseHelper dbHelper;
    private View view;
    public Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                        Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_contacts,container,false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.contacts);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }

        dbHelper = new MyDataBaseHelper(getContext(),"PeopleStore.db",null,1);
        iniPeoples();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PeopleAdapter(peopleList);
        recyclerView.setAdapter(adapter);

        return view;
    }


    //以下为mainactivity内容
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.clear();
        inflater.inflate(R.menu.toolbar,menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.backup:
                Toast.makeText(getContext(),"You clicked Backup",Toast.LENGTH_SHORT).show();
                break;
            case R.id.add:
                Intent intent = new Intent(getContext(),AddEditContactActivity.class);
                startActivity(intent);
                break;
            case R.id.settings:
                Toast.makeText(getContext(),"You clicked Settings",Toast.LENGTH_SHORT).show();
                break;
            case R.id.import_from_system:
                importFromSystem();
                break;
            case android.R.id.home:
                //drawer
                break;

            default:
        }
        return true;
    }

    private void iniPeoples(){
        peopleList.clear();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("People",null,null,null,null,null,null);
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

    private void importFromSystem(){
        dbHelper.getWritableDatabase();
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS}, 4);
        } else {
            importFromSystemBegin();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){
        switch(requestCode){
            case 4:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    importFromSystemBegin();
                }else{
                    Toast.makeText(getContext(), "You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private void importFromSystemBegin(){
        Cursor sysContactsCursor = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            sysContactsCursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,null,null,null);
            if(sysContactsCursor != null){
                boolean newcontact = false;
                while(sysContactsCursor.moveToNext()){
                    String Name = sysContactsCursor.getString(
                            sysContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String Number = sysContactsCursor.getString(
                            sysContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Number = Number.replaceAll("[ -]", "");
                    Cursor qcursor = db.rawQuery("select * from People where name = ? and phoneNumber1 = ?",
                            new String[]{Name,Number});
                    if(qcursor.moveToFirst()){
                        qcursor.close();
                    }else {
                        qcursor.close();
                        db.execSQL("insert into People (name,phoneNumber1,isBlack) values(?,?,?)",
                                new String[]{Name, Number,"0"});
                        newcontact = true;
                    }
                }
                if(newcontact){
                    iniPeoples();
                    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
                    GridLayoutManager layoutManager = new GridLayoutManager(getContext(),1);
                    recyclerView.setLayoutManager(layoutManager);
                    adapter = new PeopleAdapter(peopleList);
                    recyclerView.setAdapter(adapter);
                    Toast.makeText(getContext(),"Import suecceeded",Toast.LENGTH_SHORT).show();
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(sysContactsCursor != null){
                sysContactsCursor.close();
            }
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        iniPeoples();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PeopleAdapter(peopleList);
        recyclerView.setAdapter(adapter);
    }


}
