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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;

public class AddEditContactActivity extends AppCompatActivity {
    private EditText editName;
    private EditText editPhoneNumber;
    private Spinner editRelationship;
    private ImageView avatorView;
    private MyDataBaseHelper dbHelper;
    private String index;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_edit_contact);
        setSupportActionBar(toolbar);
        setTitle("编辑联系人");
        dbHelper = new MyDataBaseHelper(this,"PeopleStore.db",null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        avatorView = (ImageView) findViewById(R.id.add_contact_avatar);
        avatorView.setImageResource(R.drawable.avatar_boy);
        editName = (EditText) findViewById(R.id.edit_name);
        editPhoneNumber = (EditText) findViewById(R.id.edit_phone_number);
        editRelationship = (Spinner) findViewById(R.id.edit_relationship);
        dbHelper = new MyDataBaseHelper(this,"PeopleStore.db",null,1);
        Intent intent = getIntent();
        index = intent.getStringExtra("index");
        if(index!=null){
            Cursor qcursor = db.rawQuery("select * from People where id = ?",
                    new String[]{index});
            Log.d("ID: ",index);
            //If there exist such phone number in the database.
            if(qcursor.moveToFirst()) {
                String name = qcursor.getString(qcursor.getColumnIndex("name"));
                String phone = qcursor.getString(qcursor.getColumnIndex("phoneNumber1"));
                String relationship = qcursor.getString(qcursor.getColumnIndex("relationship"));
                int resID = R.drawable.avatar_boy;//getResources().getIdentifier("avatar_boy", "drawable", "com.example.calls");
                qcursor.close();
                String[] rel = getResources().getStringArray(R.array.relationship);
                editName.setText(name);
                editPhoneNumber.setText(phone);
                Integer pos = 0;
                for(int i = 0;i<rel.length;i++){
                    if(rel[i].equals(relationship)){
                        pos = i;
                        break;
                    }
                }
                Log.d("Position",pos.toString());
                editRelationship.setSelection(pos);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_add_edit_contact,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.edit_commit:
                if(index!=null) editContact();
                else createNewContact();
                finish();
            case R.id.edit_cancel:
                finish();
            default:
        }
        return true;
    }

    private void createNewContact(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String Name = editName.getText().toString();
        String PhoneNumber = editPhoneNumber.getText().toString();
        String RelationShip = editRelationship.getSelectedItem().toString();
        if(!Name.isEmpty() && !PhoneNumber.isEmpty()) {
            db.execSQL("insert into People (name, phoneNumber1, relationship,isBlack) values(?,?,?,?)",
                    new String[]{Name, PhoneNumber, RelationShip, "0"});
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
        }

    }

    private void editContact(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String Name = editName.getText().toString();
        String PhoneNumber = editPhoneNumber.getText().toString();
        String RelationShip = editRelationship.getSelectedItem().toString();
        if(!Name.isEmpty() && !PhoneNumber.isEmpty()) {
            db.execSQL("update People set name = ?, phoneNumber1 = ?, relationship = ? where id = ?",
                    new String[]{Name, PhoneNumber, RelationShip, index.toString()});
            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "修改失败", Toast.LENGTH_SHORT).show();
        }
    }

}
