package com.example.mobilecafeapplication.controller

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mobilecafeapplication.DatabaseLinker
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.NotificationStatus
import com.example.mobilecafeapplication.model.Notifications
import com.example.mobilecafeapplication.model.Users
import com.google.android.material.button.MaterialButton

class FragNotifications(val user:Users) : Fragment() {

    private lateinit var context: Context
    private lateinit var database: DatabaseLinker

    override fun onCreateView(inflater:LayoutInflater,container:ViewGroup?,savedInstanceState:Bundle?):View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.frag_notifications, container, false)
        context=requireContext()
        database=DatabaseLinker(context)

        // Prevent items from appearing above the notification line on the phone
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.fragNotifications)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val notifBtn = view.findViewById<MaterialButton>(R.id.newNotificationBtn)

        if (user.isAdmin) {
            notifBtn.visibility = View.VISIBLE
        } else {
            notifBtn.visibility = View.GONE
        }

        notifBtn.setOnClickListener{onClickNewNotification(view)}

        val notifList=view.findViewById<ListView>(R.id.notificationsList)
        val notif=database.getNotifications(user.userId,user.isAdmin)

        if(notif.size != 0){
            val notifAdapter= NotifAdapter(context,notif)
            notifList!!.adapter=notifAdapter
        }
        return view
    }

    private fun onClickNewNotification(view:View){
        val builder= AlertDialog.Builder(context)
        val dialogInflater=layoutInflater
        val dialogBox=dialogInflater.inflate(R.layout.notificationreview,null)

        val notifTitle=view.findViewById<TextView>(R.id.notifTitle)
        val notifDesc = view.findViewById<TextView>(R.id.notifDesc)
        with(builder){
            setTitle(R.string.notificaionTitle)
            setPositiveButton(R.string.btnAdd){ _, _->
                if(notifTitle.text.isNotEmpty() && notifDesc.text.isNotEmpty()){
                    database.addNotification(
                        Notifications(0, notifTitle.text.toString(), notifDesc.text.toString(), 1,user.userId,false,
                            NotificationStatus.NOT_ACCEPTED)
                    )
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, R.string.notificationsError, Toast.LENGTH_SHORT).show()
                }
            }

            setNegativeButton(R.string.btnCancel){ _, _-> Toast.makeText(context,
                R.string.cancelled,
                Toast.LENGTH_SHORT).show()}
            setView(dialogBox)
            show()
        }
    }
}