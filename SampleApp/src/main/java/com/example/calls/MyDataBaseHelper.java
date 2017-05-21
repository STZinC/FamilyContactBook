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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by yanyangma on 09/05/2017.
 */

public class MyDataBaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_PEOPLE = "create table People ("
            + "id integer primary key autoincrement,"
            + "name text,"
            + "phoneNumber1 text,"
            + "phoneNumber2 text,"
            + "birthday text,"
            + "relationship text,"
            + "email1 text,"
            + "email2 text,"
            + "phoneLocation text,"
            + "Location text,"
            + "extra text,"
            + "photoId integer)";

    private Context mContext;

    public MyDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_PEOPLE);
        Toast.makeText(mContext,"Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("drop table if exists People");
        onCreate(db);
    }
}
