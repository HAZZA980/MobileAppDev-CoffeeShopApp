package com.example.mobilecafeapplication.controller

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.OrderItems

class OrderItemsAdapter(private val context: Context,private val orderItems:ArrayList<OrderItems>):BaseAdapter() {
    private val inflater: LayoutInflater =context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return orderItems.size
    }

    override fun getItem(i:Int): Any {
        return orderItems[i]
    }

    override fun getItemId(i:Int): Long {
        return i.toLong()
    }

    override fun getView(position:Int,view:View?,parent:ViewGroup?): View {
        var view:View?=view
        view=inflater.inflate(R.layout.order_items_list,parent,false)

        val orderedItem=view.findViewById<TextView>(R.id.orderItem)
        val itemQuantity=view.findViewById<TextView>(R.id.orderItemQuantity)
        val itemPrice=view.findViewById<TextView>(R.id.orderItemPrice)
        val itemTotal=view.findViewById<TextView>(R.id.orderItemTotal)

        orderedItem.text=orderItems[position].itemName
        itemQuantity.text=orderItems[position].itemQuantity.toString()
        itemPrice.text=orderItems[position].itemPrice.toString()
        itemTotal.text=(orderItems[position].itemQuantity.toFloat()*orderItems[position].itemPrice).toString()

        return view
    }
}