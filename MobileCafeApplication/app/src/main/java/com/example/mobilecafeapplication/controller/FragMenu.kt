package com.example.mobilecafeapplication.controller

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mobilecafeapplication.DatabaseLinker
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.Orders
import com.example.mobilecafeapplication.model.Product
import com.example.mobilecafeapplication.model.Users

class FragMenu(private val _orderInformation:Orders,private val user:Users):Fragment(){

    private lateinit var context:Context
    private lateinit var database:DatabaseLinker
    private var currentlyAdmin:Boolean=false
    private var backPressed=0

    override fun onCreateView(inflater:LayoutInflater,container:ViewGroup?,savedInstanceState:Bundle?):View? {

        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.frag_menu,container,false)
        context=requireContext()
        database=DatabaseLinker(context)

        // Prevent items from appearing above the notification line on the phone
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.fragMenu)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Set status bar colour
        requireActivity().window.statusBarColor=resources.getColor(R.color.topMenuBar)

        //Grab the order store passed in, this could be from basket so could have data
        val orderInformation=_orderInformation
        val productsArray=database.getProducts(false)

        //Assign Buttons
        val adminBtn=view.findViewById<Button>(R.id.adminButton)
        val basketBtn=view.findViewById<Button>(R.id.menuScreenBasketBtn)

        basketBtn.setOnClickListener{basketBtn(view,orderInformation,productsArray)}
        adminBtn.setOnClickListener{adminBtn(view,orderInformation)}

        //Check if admin and make button visible
        if(user.isAdmin){
            adminBtn.visibility=View.VISIBLE
        }

        //Get ListView
        val productsList=view.findViewById<ListView>(R.id.productsListview)
        val productsAdapter= ProductsAdapter(context,productsArray,orderInformation,false)
        productsList!!.adapter=productsAdapter

        //Custom Back Button Handler
        requireActivity().onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed(){
                if(backPressed>0){
                    (activity as HomeScreen).changeBinding(FragHome(user),R.id.fragHome)
                }else{
                    Toast.makeText(context,"Leaving this screen will erase your current basket",Toast.LENGTH_SHORT).show()
                    backPressed++
                }
            }
        })

        return view // Return the inflated view
    }

    /**
     * adminBtn
     *
     * If the user is an admin of the application then allow them to change the menu view
     * to edit the information about products, change stock, remove and add items to the products
     * database.
     * This is the button that causes the transition between the 2 states that the menu can be in,
     * one for customers and one for admins.
     *
     * @param orderInformation the information about what has been ordered and what has not, needed
     * for the transition back to customer mode.
     */
    private fun adminBtn(view:View,orderInformation:Orders){
        val productsList=view.findViewById<ListView>(R.id.productsListview)
        var productsAdapter: ProductsAdapter

        if(currentlyAdmin){
            currentlyAdmin=false

            //Store changes Admin made
            productsAdapter=productsList.adapter as ProductsAdapter
            database.updateProducts(productsAdapter.returnUpdatedDetails())

            val basketBtn=view.findViewById<Button>(R.id.menuScreenBasketBtn)
            basketBtn.setOnClickListener{basketBtn(view,orderInformation,productsAdapter.returnUpdatedDetails())}

            val productsArray=database.getProducts(false)
            productsAdapter= ProductsAdapter(context,productsArray,orderInformation,false)
        }else{
            currentlyAdmin=true

            val basketBtn=view.findViewById<Button>(R.id.menuScreenBasketBtn)
            basketBtn.setOnClickListener{plusBtn(view)}

            val productsArray=database.getProducts(true)
            productsAdapter= ProductsAdapter(context,productsArray,orderInformation,true)
        }
        productsList!!.adapter=productsAdapter
    }

    /**
     * plusBtn
     *
     * Used by the admin of the application to add a new item to the products database.
     * Checks are in place to make sure the necessary details have been filled in before the
     * product is submitted into the database.
     */
    private fun plusBtn(view:View){
        val builder= AlertDialog.Builder(context)
        val dialogInflater=layoutInflater
        val dialogBox=dialogInflater.inflate(R.layout.admin_add_item,null)

        val itemTitle=dialogBox.findViewById<EditText>(R.id.addItemTxtName)
        val itemDescription=dialogBox.findViewById<EditText>(R.id.addItemTxtDescription)
        val itemPrice=dialogBox.findViewById<EditText>(R.id.addItemTxtPrice)
        val itemStock=dialogBox.findViewById<EditText>(R.id.addItemTxtStock)

        with(builder){
            setTitle(R.string.newItem)
            setPositiveButton(R.string.btnAdd){_,_ ->
                if(itemTitle.text.isNotEmpty()){
                    if(itemPrice.text.isNotEmpty()){
                        val stock:Int=if(itemStock.text.isEmpty()) 0 else itemStock.text.toString().toInt()
                        val inStock:Boolean=stock>0
                        val returnCode=database.addProduct(Product(0,itemTitle.text.toString(),itemDescription.text.toString(),itemPrice.text.toString().toFloat(),stock,inStock,true))
                        Log.d("Database Return Code","$returnCode")
                    }else{
                        Toast.makeText(context,R.string.itemNeedsPrice,Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(context,R.string.itemNeedsTitle,Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton(R.string.btnCancel){_,_-> Toast.makeText(context,R.string.cancelled,Toast.LENGTH_SHORT).show()}
            setView(dialogBox)
            show()
        }
    }

    /**
     * basketBtn
     *
     * Move from the menu fragment to the basket fragment passing in the order that the user has placed.
     * Products are also passed in so that the stock information can be updated if the payment
     * process is successful.
     */
    private fun basketBtn(view:View,orderInformation:Orders,products:ArrayList<Product>){
        (activity as HomeScreen).changeBinding(FragBasket(orderInformation,user,products),R.id.fragMenu)
    }
}