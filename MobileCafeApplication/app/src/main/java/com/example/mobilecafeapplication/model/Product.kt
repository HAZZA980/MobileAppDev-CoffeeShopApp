package com.example.mobilecafeapplication.model

data class Product(val productId:Int,var productName:String,var productDescription:String,var productPrice:Float,var productStock:Int,var inStock:Boolean,var isHidden:Boolean){

    /**
     * constructor
     *
     * Secondary constructor for the Products data class.
     * Sets price to 0, stock to 0, inStock to false and isHidden to true
     *
     * @param productId the id number of the product
     * @param productName the name of the product
     * @param productDescription the description of the product
     */
    constructor(productId:Int,productName:String,productDescription:String):this(productId,productName,productDescription,0.00F,0,false,true)
}
