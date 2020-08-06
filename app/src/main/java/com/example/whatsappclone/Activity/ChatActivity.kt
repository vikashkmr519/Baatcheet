package com.example.whatsappclone.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappclone.Adapter.ChatAdapter
import com.example.whatsappclone.R
import com.example.whatsappclone.modals.Message
import com.example.whatsappclone.Util.User
import com.example.whatsappclone.Util.isSameDayAs
import com.example.whatsappclone.modals.ChatEvent
import com.example.whatsappclone.modals.DateHeader
import com.example.whatsappclone.modals.Inbox
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*


const val UID = "uid"
const val NAME = "name"
const val IMAGE = "photo"
class ChatActivity : AppCompatActivity() {

    private val friendId: String by lazy {
        intent.getStringExtra(UID)
    }

    private val name: String by lazy {
        intent.getStringExtra(NAME)
    }
    private val image: String by lazy {
        intent.getStringExtra(IMAGE)
    }

    private val mCurrentUid: String by lazy {
        FirebaseAuth.getInstance().uid!!
    }

    private val db : FirebaseDatabase by lazy{
        FirebaseDatabase.getInstance()
    }

    lateinit var currentUser : User

    private val messages = mutableListOf<ChatEvent>()

    lateinit var chatAdapter:ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //it should be abouve the setContentView otherwise it will get crashed
        EmojiManager.install(GoogleEmojiProvider())

        setContentView(R.layout.activity_chat)

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }

        chatAdapter = ChatAdapter(messages,mCurrentUid)
        msgRv.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        nameTv.text = name
        Picasso.get().load(image).into(userImgView)
        listenToMessages(){ msg,update ->
            if(update){
                updateMessage(msg)
            }else{
                addMessage(msg)
            }

        }

        sendBtn.setOnClickListener{
            msgEdtv.text?.let{
                if(it.isNotEmpty()){
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }


    }

    private fun listenToMessages(newMsg : (msg: Message,update :Boolean)->Unit)
    {
        getMessages(friendId)
            .orderByKey()
            .addChildEventListener(object : ChildEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildChanged(data: DataSnapshot, p1: String?) {
                    val msg = data.getValue(Message::class.java)!!
                    newMsg(msg, true)
                }

                override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
                    var msg =  snapshot.getValue(Message::class.java)!!
                    newMsg(msg,false)
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun addMessage(msg: Message) {
        val eventBefore =  messages.lastOrNull()
        
        if((eventBefore != null && !eventBefore.sentAt.isSameDayAs(msg.sentAt)) || eventBefore== null){
            messages.add(
                DateHeader(
                    msg.sentAt,context = this
                )
            )
        }


            messages.add(msg)


        chatAdapter.notifyItemInserted(messages.size)
        msgRv.scrollToPosition(messages.size+1)
    }

    private fun updateMessage(msg: Message) {
        val position = messages.indexOfFirst {
            when (it) {
                is Message -> it.msgId == msg.msgId
                else -> false
            }
        }
        messages[position] = msg

        chatAdapter.notifyItemChanged(position)
    }

    private fun sendMessage(msg: String) {
        val id = getMessages(friendId).push().key // unique key
        checkNotNull(id){"Cannot be Null"}
        val msgMap = com.example.whatsappclone.modals.Message(msg,mCurrentUid,id)
        getMessages(friendId).child(id).setValue(msgMap).addOnSuccessListener {

        }
        updateLastMessage(msgMap)
    }

    private fun updateLastMessage(message: Message) {
        val inboxmap = Inbox(
            message.msg,
            friendId,
            name,
            image,
            count = 0
        )

        // this outer getInbox is to set the values for cureent user
        getInbox(mCurrentUid,friendId).setValue(inboxmap).addOnSuccessListener {

            //this inner getInbox is to set the values for friend user
            getInbox(friendId,mCurrentUid).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    val value =  p0.getValue(Inbox::class.java)

                    inboxmap.apply{
                        from = message.senderId
                        name = currentUser.name
                        image=  currentUser.thumbImage
                        count = 1
                    }


                    // now we will check if value is not null and we have send the last  message
                    //then we will get its count and increase it with 1

                    // this also the case when its exists then we don't have any unread message
                    value?.let {
                        if(it.from == message.senderId){
                            inboxmap.count = value.count+1
                        }
                    }

                    // then again set the inbox map
                    getInbox(friendId,mCurrentUid).setValue(inboxmap)
                }

            })
        }
    }

    private fun markArRead(){
        getInbox(friendId,mCurrentUid).child("count").setValue(0)
    }

    private fun getMessages(friendId: String)= db.reference.child("messages/${getId(friendId)}")

    private fun getInbox(toUser:String, fromUser:String) =
        db.reference.child("chats/$toUser/$fromUser")


    //this will give the unique id whiich will be the mixture of the id of the both the users
    private fun  getId(friendId:String): String{
        return if(friendId > mCurrentUid){
            mCurrentUid + friendId
        }else{
            friendId + mCurrentUid
        }
    }
}