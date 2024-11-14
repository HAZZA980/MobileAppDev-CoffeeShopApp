package com.example.mobilecafeapplication.model

import java.io.Serializable
import java.util.Date

/**
 * Orders
 *
 * Data class to store the details of a specific Order
 *
 * @param orderNumber the identification number of the order
 * @param orderDate the date the order was placed
 * @param orderedItems an ArrayList storing instances of OrderItems for the order
 */
class Orders(val orderNumber:Int, val orderDate: Date, val orderedItems:ArrayList<OrderItems>,val userId:Int):Serializable{

    constructor(orderNumber:Int,orderDate:Date,userId:Int):this(orderNumber,orderDate,ArrayList<OrderItems>(),userId)

    /**
     * orderTotal
     *
     * The total amount in the order price wise.
     *
     * @return the total price of the order
     */
    fun orderTotal():Float{
        var orderTotal=0.0F
        for(item in orderedItems){
            orderTotal+=item.itemPrice*(item.itemQuantity.toFloat())
        }
        return orderTotal
    }

    override fun toString():String{
        var orderString:String="Order Details:\n"

        for(item in orderedItems){
            orderString+="${item.toString()}\n"
        }

        orderString+="Total: ${this.orderTotal()}"

        return orderString
    }
}