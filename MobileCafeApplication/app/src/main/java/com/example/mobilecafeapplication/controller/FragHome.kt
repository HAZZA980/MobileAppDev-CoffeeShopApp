package com.example.mobilecafeapplication.controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.mobilecafeapplication.DatabaseLinker
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.Users


class FragHome(private val user: Users):Fragment(){

    private lateinit var context: Context
    private lateinit var database: DatabaseLinker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.frag_home, container, false)
        context=requireContext()
        database=DatabaseLinker(context)

        // Prevent items from appearing above the notification line on the phone
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.fragHome)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Set status bar colour
        requireActivity().window.statusBarColor=resources.getColor(R.color.topMenuBar)

        requireActivity().onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed(){
                val loginScreen= Intent(context,LoginScreen::class.java)
                startActivity(loginScreen)
            }
        })

        //Generate list view for previous orders, will be blank if there are no previous orders
        val orderedList=view.findViewById<ListView>(R.id.fragHomeOrderHistory)
        val tmpOrders=database.getOrders(user.userId)

        if(tmpOrders!=null){
            val orderHistory= OrderHistoryAdapter(context,tmpOrders)
            orderedList!!.adapter=orderHistory
        }

        return view
    }
}