package com.example.mobilecafeapplication.controller

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mobilecafeapplication.DatabaseErrors
import com.example.mobilecafeapplication.DatabaseLinker
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.NotificationStatus
import com.example.mobilecafeapplication.model.Notifications
import com.example.mobilecafeapplication.model.Orders
import com.example.mobilecafeapplication.model.Product
import com.example.mobilecafeapplication.model.Users
import java.util.Date


class FragBasket(private val orderInformation:Orders,private val user:Users,private val products:ArrayList<Product>) : Fragment() {

    // Reference to the database
    private lateinit var context: Context
    private lateinit var database:DatabaseLinker

    override fun onCreateView(inflater:LayoutInflater,container:ViewGroup?,savedInstanceState:Bundle?):View?{

        val view=inflater.inflate(R.layout.frag_basket, container, false)

        context=requireContext()
        database=DatabaseLinker(context)

        // Prevent items from appearing above the notification line on the phone
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.fragBasket)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Custom Back Button Handler
        requireActivity().onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed(){
                val fragTransaction=requireActivity().supportFragmentManager.beginTransaction()
                fragTransaction.replace(R.id.homeScreenFrame,FragMenu(orderInformation,user))
                fragTransaction.commit()
            }
        })

        //Set status bar colour
        requireActivity().window.statusBarColor=resources.getColor(R.color.topMenuBar)

        //Create the listview
        val itemList=view.findViewById<ListView>(R.id.listViewProducts)
        val itemAdapter= OrderItemsAdapter(context,orderInformation.orderedItems)
        itemList!!.adapter=itemAdapter

        //Set the total price
        val totalPrice=view.findViewById<TextView>(R.id.editTextTotalAmount)
        totalPrice.isEnabled=false
        totalPrice.text=orderInformation.orderTotal().toString()

        //Set Button Function
        val paymentBtn=view.findViewById<Button>(R.id.paymentButton)
        val backBtn=view.findViewById<Button>(R.id.cancelButton)

        paymentBtn.setOnClickListener{makePaymentBtn(view)}
        backBtn.setOnClickListener{requireActivity().onBackPressedDispatcher.onBackPressed()}

        return view
    }

    /**
     * makePaymentBtn
     *
     * Button on the basket view to verify the information the user has entered to complete
     * the payment is correct.
     * Entered card details are checked to make sure they conform to the standard. If the card
     * details are accepted then the order is added to the database and the stock of the projects
     * purchased are updated to remove the quantity that the customer purchased.
     *
     * If there are any errors then a Toast message is displayed to the user updating them on
     * what the error is.
     */
    private fun makePaymentBtn(view:View){
        //Card Details
        val nameOnCard=view.findViewById<TextView>(R.id.editTextCardHolderName)
        val cardNumbers=view.findViewById<TextView>(R.id.editTextCardDetails)
        val cardCvc=view.findViewById<TextView>(R.id.editTextCVC)

        if(nameOnCard.text.isNotEmpty()){
            if(cardCvc.text.toString().length==3){
                if(cardNumbers.text.toString().length==16){
                    val errorCode=database.addOrder(orderInformation,user.userId)

                    if(errorCode==DatabaseErrors.SUCCESS){
                        //Update product stock
                        for(prod in products){
                            for(item in orderInformation.orderedItems){
                                if(prod.productName==item.itemName){
                                    prod.productStock=-item.itemQuantity
                                }
                            }
                        }
                        database.updateProducts(products)

                        //Send notification to admins to accept
                        database.addNotification(Notifications(0,"New Order",orderInformation.toString(),0,user.userId,true,NotificationStatus.WAITING_FOR_STATE))

                        //Inform the user that their payment went through
                        Toast.makeText(context,R.string.paymentAccepted,Toast.LENGTH_SHORT).show()

                        //Go back to the menu screen without order information stored
                        val fragTransaction=requireActivity().supportFragmentManager.beginTransaction()
                        fragTransaction.replace(R.id.homeScreenFrame,FragMenu(Orders(0, Date(),user.userId),user))
                        fragTransaction.commit()
                    }else{
                        Toast.makeText(context,R.string.paymentError,Toast.LENGTH_SHORT).show()
                    }
                    Log.d("SubmittingOrder DatabaseError","$errorCode")
                }else{
                    Toast.makeText(context,R.string.carNumbDigits,Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context,R.string.cardCVC,Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(context,R.string.cardHolderName,Toast.LENGTH_SHORT).show()
        }
    }
}