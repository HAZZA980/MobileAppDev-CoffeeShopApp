package com.example.mobilecafeapplication.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobilecafeapplication.DatabaseLinker
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.Users

class LoginScreen : AppCompatActivity() {

    private val database: DatabaseLinker = DatabaseLinker(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginScreen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Check to see if the email address has been passed through the intent
        val userEmail=intent.getStringExtra("emailAddress")
        val displayedEmail=findViewById<EditText>(R.id.loginScreenEmail)
        displayedEmail.setText(userEmail)

        //Stop the Back Button from working
        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true){override fun handleOnBackPressed(){}})
    }

    /**
     * btnRegisterUser
     *
     * Checks to see if the email address has been entered and if it is valid.
     * Then passes it through the intent to the RegisterScreen.
     */
    fun btnRegisterUser(view: View){

        // Grab intent to change to register view
        val registerScreen=Intent(this,RegisterScreen::class.java)

        // Check if an email has already been filled in
        val txtEmail=findViewById<EditText>(R.id.loginScreenEmail)
        if(txtEmail.text.isNotEmpty()&& Users.checkEmailValid(txtEmail.text.toString())){
            // Grab the email and pass it through to the Register Screen
            registerScreen.putExtra("emailAddress",txtEmail.text.toString())
        }

        startActivity(registerScreen)
    }

    /**
     * btnLoginUser
     *
     * Checks the details that the user has entered are valid.
     * Then attempts to read the users information from the database.
     * If no user is returned then raises and error.
     * If user is returned then moves to the HomeScreen
     */
    fun btnLoginUser(view:View){
        //Grab entered email and password for database search
        val fieldEmail=findViewById<EditText>(R.id.loginScreenEmail)
        val fieldPassword=findViewById<EditText>(R.id.loginScreenPassword)

        val toastMessage:String

        //Validate there is data there and in the correct format before searching the database
        if(fieldEmail.text.isNotEmpty()&&Users.checkEmailValid(fieldEmail.text.toString())){
            if(fieldPassword.text.isNotEmpty()){
                val currentUser=database.getUser(fieldEmail.text.toString(),fieldPassword.text.toString())
                if(currentUser==null){
                    toastMessage=getString(R.string.invalidLogin)
                }else{
                    val homeScreen=Intent(this,HomeScreen::class.java)
                    homeScreen.putExtra("userInstance",currentUser)
                    startActivity(homeScreen)
                    return
                }
            }else{
                toastMessage=getString(R.string.enterPassword)
            }
        }else{
            toastMessage=getString(R.string.enterEmail)
        }

        Toast.makeText(this,toastMessage,Toast.LENGTH_SHORT).show()
    }
}