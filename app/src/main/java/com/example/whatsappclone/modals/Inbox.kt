package com.example.whatsappclone.modals

import java.util.*

data class Inbox(
    val msg: String,
    val from : String,
    var name : String,
    var image: String,
    val time : Date = Date(),
    var count : Int
){
    // for firebase we have to create a empty constructor everytime
    constructor():this("","","","",Date(),0)
}
