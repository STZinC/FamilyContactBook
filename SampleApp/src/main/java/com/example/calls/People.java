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

/**
 * Created by yanyangma on 07/05/2017.
 */

public class People implements Comparable<People>{
    private int id;
    private String Name;
    private String PhoneNumber;
    private String pinyin;
    private char firstChar;

    public People(String Name, String PhoneNumber, int id){
        this.id = id;
        this.Name = Name;
        this.PhoneNumber = PhoneNumber;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
        String first = pinyin.substring(0, 1);
        if (first.matches("[A-Za-z]")) {
            firstChar = first.toUpperCase().charAt(0);
        } else {
            firstChar = '#';
        }
    }

    public String getName(){
        return Name;
    }
    public String getPhoneNumber(){return PhoneNumber;}
    public int getId() {return id;}
    public void setName(String name) {
        this.Name = name;
    }
    public void setId(int id) {
        this.id = id;
    }
    public char getFirstChar() {
        return firstChar;
    }

    @Override
    public int compareTo(People another) {
        return this.pinyin.compareTo(another.getPinyin());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof People) {
            return this.id == ((People) o).getId();
        } else {
            return super.equals(o);
        }
    }
}
