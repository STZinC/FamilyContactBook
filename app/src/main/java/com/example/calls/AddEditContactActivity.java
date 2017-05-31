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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.bitmap;
import static android.R.attr.popupCharacters;

public class AddEditContactActivity extends AppCompatActivity {
    private EditText editName;
    private EditText editPhoneNumber;
    private Spinner editRelationship;
    private ImageView avatorView;
    private MyDataBaseHelper dbHelper;
    private String index;
    private boolean isAvatarChange = false;
    private final int  ALBUM_OK = 1, CAMERA_OK = 2,CUT_OK = 3;
    private Intent avatarData;
    private File file;
    private File filesave;
    private Uri imageUri;
    private Uri imageSaveUri;
    private Integer photoId;

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
        avatorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 来自相册
                Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
                /**
                 * 下面这句话，与其它方式写是一样的效果，如果：
                 * intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                 * intent.setType(""image/*");设置数据类型
                 * 要限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
                 */
                albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(albumIntent, ALBUM_OK);

//                // 来自相机
//
//                Log.d("initially file exit","exit");
//                Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
//                // 下面这句指定调用相机拍照后的照片存储的路径
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                startActivityForResult(cameraIntent, CAMERA_OK);// CAMERA_OK是用作判断返回结果的标识

            }
        });
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
                photoId = qcursor.getInt(qcursor.getColumnIndex("photoId"));
                if(photoId != 0){
                    String filename = photoId.toString()+".jpg";
                    file = new File(Environment.getExternalStorageDirectory(), filename);
                    imageUri = FileProvider.getUriForFile(AddEditContactActivity.this,"com.yanyangma.FamilyPhoneBook.fileprovider",file);
                    if(file.exists()) {
                        try {
                            Bitmap photo1 = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            avatorView.setImageBitmap(photo1);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            avatorView.setImageResource(R.drawable.avatar_boy);
                        }
                    }
                    else;
                }
                else{
                    avatorView.setImageResource(R.drawable.avatar_boy);
                }
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
        Integer photoId = (Name+PhoneNumber).hashCode();
        if(!Name.isEmpty() && !PhoneNumber.isEmpty()) {
            if(isAvatarChange){
                filesave =  new File(Environment.getExternalStorageDirectory(), photoId.toString()+".jpg");
                try {
                    if (filesave.exists()) {
                        filesave.delete();
                    }
                    filesave.createNewFile();
                } catch (IOException e){
                    e.printStackTrace();
                }
                FileOutputStream fos = null;
                try {
                    Bundle extras = avatarData.getExtras();
                    Bitmap photo = extras.getParcelable("data");
                    fos = new FileOutputStream(filesave);
                    photo.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                            Log.d("Save","Save successfully");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                db.execSQL("insert into People (name, phoneNumber1, relationship, isBlack, photoId) values(?,?,?,?,?)",
                        new String[]{Name, PhoneNumber, RelationShip, "0", photoId.toString()});
            }
            else {
                db.execSQL("insert into People (name, phoneNumber1, relationship,isBlack) values(?,?,?,?)",
                        new String[]{Name, PhoneNumber, RelationShip, "0"});
            }
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
        if(photoId == 0){
            photoId = (Name+PhoneNumber).hashCode();
            db.execSQL("update People set name = ?, phoneNumber1 = ?, relationship = ?, photoId = ? where id = ?",
                    new String[]{Name, PhoneNumber, RelationShip, photoId.toString(), index.toString()});
        }
        else{
            db.execSQL("update People set name = ?, phoneNumber1 = ?, relationship = ? where id = ?",
                    new String[]{Name, PhoneNumber, RelationShip, index.toString()});
        }
        if(!Name.isEmpty() && !PhoneNumber.isEmpty()) {
            filesave =  new File(Environment.getExternalStorageDirectory(), photoId.toString()+".jpg");
            try {
                if (filesave.exists()) {
                    filesave.delete();
                }
                filesave.createNewFile();
            } catch (IOException e){
                e.printStackTrace();
            }
            FileOutputStream fos = null;
            try {
                Bundle extras = avatarData.getExtras();
                Bitmap photo = extras.getParcelable("data");
                fos = new FileOutputStream(filesave);
                photo.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        Log.d("Save","Save successfully");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "修改失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("requestCode = " + requestCode);
        switch (requestCode) {
            // 如果是直接从相册获取
            case ALBUM_OK:
                //从相册中获取到图片了，才执行裁剪动作
                if (data != null) {
                    clipPhoto(data.getData());
                }
                break;
            // 如果是调用相机拍照时
            case CAMERA_OK:
                // 当拍照到照片时进行裁减，否则不执行操作
                if (file.exists()) {
                    Log.d("file exit","exit!!");
                    clipPhoto(imageUri);//开始裁减图片
                }
                else Log.d("file exit","no");
                break;
            // 取得裁剪后的图片，这里将其设置到imageview中
            case CUT_OK:
                /**
                 * 非空判断大家一定要验证，如果不验证的话， 在剪裁之后如果发现不满意，
                 * 要重新裁剪，丢弃 当前功能时，会报NullException
                 */
                //if(file.exists()) Log.d("Final file exit","exist");
                if (data != null) {
                    setPicToView(data);
                    Log.d("Cut not null","not null");
                }
                //else Log.d("Cut not null","null");
                break;
            default:
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪图片方法实现
     * @param uri
     */
    public void clipPhoto(Uri uri) {
        Log.d("clip uri","hi");
        Log.d("clip uri",uri.toString());
        Intent intent = new Intent("com.android.camera.action.CROP");

        List<ResolveInfo> resInfoList = this.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            this.grantUriPermission("com.yanyangma.FamilyPhoneBook", imageSaveUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // 下面这个crop = true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);// 去黑边
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        // aspectX aspectY 是宽高的比例，这里设置的是正方形（长宽比为1:1）
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 200);//输出X方向的像素
        intent.putExtra("outputY", 200);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageSaveUri);
        intent.putExtra("return-data", true);//设置为不返回数据
        startActivityForResult(intent, CUT_OK);
    }

    /**
     * 保存裁剪之后的图片数据 将图片设置到imageview中
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            avatarData = picdata;
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(photo);
            avatorView.setImageDrawable(drawable);
            isAvatarChange = true;

        }
    }


}
