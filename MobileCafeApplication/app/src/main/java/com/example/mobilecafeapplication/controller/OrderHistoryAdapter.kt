package com.example.mobilecafeapplication.controller

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.mobilecafeapplication.DatabaseLinker
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.NotificationStatus
import com.example.mobilecafeapplication.model.Notifications
import com.example.mobilecafeapplication.model.Orders

class OrderHistoryAdapter(private val context:Context,private val orders:ArrayList<Orders>): BaseAdapter() {

    private val database: DatabaseLinker = DatabaseLinker(context)
    private val inflater:LayoutInflater=context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return orders.size
    }

    override fun getItem(i:Int): Orders {
        return orders[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(position:Int,view: View?,parent:ViewGroup?): View {
        var view:View?=view
        view=inflater.inflate(R.layout.order_history_list,parent,false)

        val orderNumber=view.findViewById<TextView>(R.id.orderNumber)
        val orderDate=view.findViewById<TextView>(R.id.orderDate)
        val orderItems=view.findViewById<ListView>(R.id.orderItemsList)

        orderNumber.text=orders[position].orderNumber.toString()
        orderDate.text=orders[position].orderDate.toString()

        val itemsList= OrderItemsAdapter(context,orders[position].orderedItems)
        orderItems!!.adapter=itemsList

        //Button Addition
        val wholeView=view.findViewById<LinearLayout>(R.id.orderHistoryListItem)
        wholeView.setOnClickListener{onClickReview(view,position)}

        return view
    }

    /**
     * onClickReview
     *
     * When clicking on the order make it so that the user can leave a review of the shop.
     */
    private fun onClickReview(view:View,position:Int){
        val builder=AlertDialog.Builder(context)
        val dialogInflater=inflater
        val dialogBox=dialogInflater.inflate(R.layout.review_order,null)

        val review=dialogBox.findViewById<EditText>(R.id.userReview)

        with(builder){
            setTitle(R.string.reviewOrder)
            setPositiveButton(R.string.review){_,_->
                if(review.text.isNotEmpty()){
                    database.addNotification(Notifications(0,"OrderReview: Order Number ${getItem(position).orderNumber}",review.text.toString(),0,getItem(position).userId,false,NotificationStatus.NOT_ORDER))
                    Toast.makeText(context,"Thank You for Your Review",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context,R.string.reviewEmpty,Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton(R.string.btnCancel){_,_-> Toast.makeText(context,R.string.cancelled,Toast.LENGTH_SHORT).show()}
            setView(dialogBox)
            show()
        }
    }
}