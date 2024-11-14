package com.example.mobilecafeapplication.controller

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mobilecafeapplication.DatabaseLinker
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.databinding.ActivityMainBinding
import com.example.mobilecafeapplication.model.Orders
import com.example.mobilecafeapplication.model.Users
import java.util.Date

class HomeScreen : AppCompatActivity() {

    private val database: DatabaseLinker = DatabaseLinker(this)
    private lateinit var binding: ActivityMainBinding
    private lateinit var user:Users

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user=intent.getSerializableExtra("userInstance") as Users

        if(database.defaultAdminEmail==user.userEmail&&user.isAdmin){
            fragmentInstances(FragProfile(user))
        }else{
            fragmentInstances(FragHome(user))

            binding.bottomNavBar.setOnItemSelectedListener{
                when(it.itemId){
                    R.id.navHome -> fragmentInstances(FragHome(user))
                    R.id.navMenu -> fragmentInstances(FragMenu(Orders(0, Date(),user.userId),user))
                    R.id.navProfile -> fragmentInstances(FragProfile(user))
                    R.id.navNotifications -> fragmentInstances(FragNotifications(user))
                    else -> {}
                }
                true
            }
        }
    }

    private fun fragmentInstances(frag:Fragment){
        val fragTransaction=supportFragmentManager.beginTransaction()
        fragTransaction.replace(R.id.homeScreenFrame,frag)
        fragTransaction.commit()
    }

    fun changeBinding(frag:Fragment,itemId:Int){
        binding.bottomNavBar.selectedItemId=itemId
        fragmentInstances(frag)
    }
}