package com.example.whatsappclone.Util

import com.google.firebase.firestore.FieldValue
import java.lang.reflect.Field

data class User(
    val name:String,
    val imageUrl:String,
    val thumbImage:String,
    val deviceToken:String,
    val status:String,
    val onlineStatus:String,
    val uid:String
){
    //whenever we create data class for the firebase, we have to create an empty constructor
    constructor() : this("","","","","","","")
    constructor(name:String, imageUrl: String,thumbImage: String,uid: String):this(
        name,imageUrl,thumbImage,"","Hey there I am using whatsapp","",uid)
}
