package com.example.mobilecafeapplication.controller

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mobilecafeapplication.DatabaseLinker
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.NotificationStatus
import com.example.mobilecafeapplication.model.Notifications

class NotifAdapter(private val context: Context, private val notifList: ArrayList<Notifications>) :
    BaseAdapter() {
    private val inflater: LayoutInflater=context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val database: DatabaseLinker=DatabaseLinker(context)

    override fun getCount(): Int {
        return notifList.size
    }

    override fun getItem(i: Int): Notifications {
        return notifList[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        var view: View? = view
        view = inflater.inflate(R.layout.notification_item, parent, false)

        val notifInstance=view.findViewById<LinearLayout>(R.id.notificationItem)
        val notifTitle = view.findViewById<TextView>(R.id.notificationItemHeading)
        val notifDesc = view.findViewById<TextView>(R.id.notificationItemContent)

        val currentNotif = getItem(position)

        if(currentNotif.isOrder&&currentNotif.status!=NotificationStatus.COLLECTED){
            notifInstance.setOnClickListener{
                val builder= AlertDialog.Builder(context)
                val dialogInflater=inflater
                val dialogBox=dialogInflater.inflate(R.layout.notification_admin,null)
                val orderDescription=dialogBox.findViewById<TextView>(R.id.orderDescription)
                orderDescription.text=currentNotif.description

                if(currentNotif.status==NotificationStatus.IS_ACCEPTED){
                    with(builder){
                        setTitle("Order Ready")
                        setPositiveButton("Order Ready"){ _, _->
                            currentNotif.status=NotificationStatus.READY_TO_COLLECT
                            database.updateNotification(currentNotif)
                            database.addNotification(Notifications(0,"Order Ready Collect","${currentNotif.description}\nIs ready for collection. See you soon.",currentNotif.fromUser,0,false,NotificationStatus.NOT_ORDER))
                        }
                        setNegativeButton("Order Collected"){ _, _->
                            currentNotif.status=NotificationStatus.COLLECTED
                            database.updateNotification(currentNotif)
                            database.addNotification(Notifications(0,"Collected Order","Thank you for collecting your order.\nWe hope to see you again soon",currentNotif.fromUser,0,false,NotificationStatus.NOT_ORDER))
                        }
                        setView(dialogBox)
                        show()
                    }
                }else{
                    with(builder){
                        setTitle("Accept Order")
                        setPositiveButton(R.string.accept){ _, _->
                            currentNotif.status=NotificationStatus.IS_ACCEPTED
                            database.updateNotification(currentNotif)
                            database.addNotification(Notifications(0,"Order Accepted","${currentNotif.description}\nHas been accepted.",currentNotif.fromUser,0,false,NotificationStatus.NOT_ORDER))
                        }
                        setNegativeButton(R.string.reject){ _, _->
                            currentNotif.status=NotificationStatus.NOT_ACCEPTED
                            database.updateNotification(currentNotif)
                            database.addNotification(Notifications(0,"Order Rejected","${currentNotif.description}\nHas been rejected.",currentNotif.fromUser,0,false,NotificationStatus.NOT_ORDER))
                        }
                        setView(dialogBox)
                        show()
                    }
                }
            }
        }

        notifTitle.text = currentNotif.title
        notifDesc.text = currentNotif.description

        return view
    }
}