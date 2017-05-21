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

public class People {
    private int id;
    private String Name;
    private String PhoneNumber;
    public People(String Name, String PhoneNumber, int id){
        this.id = id;
        this.Name = Name;
        this.PhoneNumber = PhoneNumber;
    }
    public String getName(){
        return Name;
    }
    public String getPhoneNumber(){return PhoneNumber;}
    public int getId() {return id;}
}
