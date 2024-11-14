package com.example.mobilecafeapplication.model

/**
 * OrderItems
 *
 * Data class to store the details of a specific item for an Order
 *
 * @param itemName the name of the item
 * @param itemPrice the price of the item at time of purchase
 * @param itemQuantity the amount of that specific item that were purchased
 */
class OrderItems(val itemName:String,val itemPrice:Float,var itemQuantity:Int){

    override fun toString():String{
        return "Item: $itemName, Quantity: $itemQuantity"
    }
}