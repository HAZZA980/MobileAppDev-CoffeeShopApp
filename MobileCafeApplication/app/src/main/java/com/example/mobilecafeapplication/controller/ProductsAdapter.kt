package com.example.mobilecafeapplication.controller

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.OrderItems
import com.example.mobilecafeapplication.model.Orders
import com.example.mobilecafeapplication.model.Product

class ProductsAdapter(private val context:Context, private val products:ArrayList<Product>, private val order: Orders, private val isAdmin:Boolean): BaseAdapter() {
    private val inflater: LayoutInflater=context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount():Int{
        return products.size
    }

    override fun getItem(i:Int): Product {
        return products[i]
    }

    override fun getItemId(i:Int):Long{
        return i.toLong()
    }

    override fun getView(position:Int,view:View?,parent:ViewGroup?):View{
        var view:View?=view
        view=inflater.inflate(R.layout.menu_items,parent,false)

        //Grab fields to fill in
        val prodTitle=view.findViewById<EditText>(R.id.productTitle)
        val prodDescription=view.findViewById<EditText>(R.id.productDescription)
        val prodPrice=view.findViewById<EditText>(R.id.productPrice)
        val quantity=view.findViewById<TextView>(R.id.quantityEditText)

        val currentProduct=this.getItem(position)

        //Set title for fields
        prodTitle.setText(currentProduct.productName)
        prodDescription.setText(currentProduct.productDescription)
        prodPrice.setText(currentProduct.productPrice.toString())

        //Grab the buttons
        val subBtn=view.findViewById<ImageButton>(R.id.subtractButton)
        val addBtn=view.findViewById<ImageButton>(R.id.addButton)
        val addBasketBtn=view.findViewById<Button>(R.id.menuScreenBasketBtn)
        val removeItem=view.findViewById<Button>(R.id.RemoveItem)

        //Set the onclick listeners
        subBtn.setOnClickListener{subBtn(view)}
        addBtn.setOnClickListener{addBtn(view,currentProduct)}
        removeItem.setOnClickListener{removeItemBtn(view,currentProduct)}

        if(isAdmin){
            //If the user is an admin then there are some extra things we need to setup
            addBasketBtn.setOnClickListener{updateDetails(view,currentProduct)}
            addBasketBtn.text=context.getString(R.string.update_details)
            quantity.text=currentProduct.productStock.toString()

            removeItem.visibility=View.VISIBLE
            if(currentProduct.isHidden){
                removeItem.text=context.getString(R.string.restore)
            }

            prodPrice.isEnabled=true
            prodTitle.isEnabled=true
            prodDescription.isEnabled=true
        }else{
            addBasketBtn.setOnClickListener{addBasket(view,order,currentProduct)}

            removeItem.visibility=View.GONE

            prodPrice.isEnabled=false
            prodTitle.isEnabled=false
            prodDescription.isEnabled=false
        }

        //See if the product has already been added to the order
        for(item in order.orderedItems){
            if(item.itemName==currentProduct.productName){
                quantity.text=item.itemQuantity.toString()
            }
        }

        return view
    }

    /**
     * returnUpdatedDetails
     *
     * @return the arraylist of products for when they have been edited by the admin
     */
    fun returnUpdatedDetails():ArrayList<Product>{
        return products
    }

    /**
     * addBtn
     *
     * Button handler for updating the amount being added by the user or the stock amount being
     * edited by the admin.
     *
     * @param product the product being added to make sure if the customer is adding it that they
     * cannot add more than what is in stock.
     */
    private fun addBtn(view:View,product: Product){
        val amount=view.findViewById<TextView>(R.id.quantityEditText)
        var amountInt:Int=amount.text.toString().toInt()

        if(product.productStock>amountInt||isAdmin){
            amountInt++
            amount.text=amountInt.toString()
        }
    }

    /**
     * subBtn
     *
     * Button handler for removing 1 from the amount being edited. The amount cannot go below 0.
     */
    private fun subBtn(view:View){
        val amount=view.findViewById<TextView>(R.id.quantityEditText)
        var amountInt:Int=amount.text.toString().toInt()

        if(amountInt>0){
            amountInt--
            amount.text=amountInt.toString()
        }
    }

    /**
     * addBasket
     *
     * Button handler to add the selected item to the order information.
     * If the item is already in the order information then we find it again and edit that value
     * to prevent duplicates from being ordered by accident.
     *
     * @param order the users order that they are placing to add the item to
     * @param product the product the user is adding to their order
     */
    private fun addBasket(view:View, order: Orders, product: Product){
        val amount=view.findViewById<TextView>(R.id.quantityEditText).text.toString().toInt()
        val items=order.orderedItems
        var itemFound:Boolean=false

        if(amount>=0){
            for(prod in items){
                if(product.productName==prod.itemName){
                    itemFound=true
                    prod.itemQuantity=amount
                }
            }

            if(!itemFound){
                items.add(OrderItems(product.productName,product.productPrice, amount))
            }
        }
    }

    /**
     * updateDetails
     *
     * Button handler to update the product information based on what the admin has entered.
     * Will update the name, description, price and stock amount of the item.
     *
     * @param prod the product being updated
     */
    private fun updateDetails(view:View,prod: Product){
        val amount=view.findViewById<EditText>(R.id.quantityEditText).text.toString().toInt()
        val description=view.findViewById<EditText>(R.id.productDescription)
        val title=view.findViewById<EditText>(R.id.productTitle)
        val price=view.findViewById<EditText>(R.id.productPrice)

        prod.productName=title.text.toString()
        prod.productDescription=description.toString()
        prod.productPrice=price.text.toString().toFloat()
        prod.productStock=amount
    }

    /**
     * removeItemBtn
     *
     * Button handler for the admin to remove the item from being show to users.
     *
     * @param prod the product that is to be removed
     */
    private fun removeItemBtn(view:View,prod: Product){
        val removeItem=view.findViewById<Button>(R.id.RemoveItem)

        if(!prod.isHidden){
            prod.isHidden=true
            removeItem.text=context.getString(R.string.restore)
        }else{
            prod.isHidden=false
            removeItem.text=context.getString(R.string.remove)
        }
    }
}