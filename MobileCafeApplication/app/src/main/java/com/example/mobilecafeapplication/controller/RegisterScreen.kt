package com.example.mobilecafeapplication.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.mobilecafeapplication.DatabaseErrors
import com.example.mobilecafeapplication.DatabaseLinker
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.Users

class RegisterScreen : AppCompatActivity() {

    private val database:DatabaseLinker= DatabaseLinker(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerScreen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Check to see if the email address has been passed through the intent
        val userEmail=intent.getStringExtra("emailAddress")
        val displayedEmail=findViewById<EditText>(R.id.registerScreenEmail)
        displayedEmail.setText(userEmail)

        //Password change listener - validate the password being entered by the user
        val password=findViewById<EditText>(R.id.registerScreenPassword)
        password.doOnTextChanged {text,_,_,_ ->
            Users.checkPasswordString(text.toString(),this)
        }

        //PasswordConfirmation change listener - validate the passwords are the same
        val passwordConfirmation=findViewById<EditText>(R.id.registerScreenConfirmPassword)
        passwordConfirmation.doOnTextChanged {text,_,_,_ ->
            Users.checkPasswordsMatch(password.text.toString(),text.toString(),this)
        }
    }

    /**
     * btnCancel
     *
     * Checks to see if an email haas already been entered and passes
     * it through the intent to the LoginScreen. Starts the LoginScreen
     * activity.
     */
    fun btnCancel(view:View) {
        val loginScreen=Intent(this,LoginScreen::class.java)

        // Check if an email has already been filled in
        val txtEmail=findViewById<EditText>(R.id.registerScreenEmail)
        if(txtEmail.text.isNotEmpty()&&Users.checkEmailValid(txtEmail.text.toString())){
            // Grab the email and pass it through to the Register Screen
            loginScreen.putExtra("emailAddress",txtEmail.text.toString())
        }

        startActivity(loginScreen)
    }

    /**
     * btnRegister
     *
     * Check all the fields on the Register Screen to see if any fields have been left out by the user.
     * This relies on other methods (checkPasswordString and checkPasswordsMatch) to complete operations.
     * Appropriate errors are displayed to the user should all the fields not be completed.
     */
    fun btnRegister(view:View){
        //Check all fields on the screen
        val fieldFullName=findViewById<EditText>(R.id.registerScreenFullName)
        val fieldEmail=findViewById<EditText>(R.id.registerScreenEmail)
        val fieldPhoneNumber=findViewById<EditText>(R.id.registerScreenPhoneNumber)
        val fieldPassword=findViewById<EditText>(R.id.registerScreenPassword)
        val fieldPasswordConfirmation=findViewById<EditText>(R.id.registerScreenConfirmPassword)
        val termsAndConditions=findViewById<CheckBox>(R.id.registerScreenTermsAndConditionsBox)

        var toastMessage=""

        if(fieldFullName.text.isNotEmpty()){
            if(fieldEmail.text.isNotEmpty()&&Users.checkEmailValid(fieldEmail.text.toString())){
                if(Users.checkPasswordString(fieldPassword.text.toString(),this) && Users.checkPasswordsMatch(fieldPassword.text.toString(),fieldPasswordConfirmation.text.toString(),this)){
                    if(termsAndConditions.isChecked){
                        //Separate Full name
                        val fullName=fieldFullName.text.toString()
                        val firstName=fullName.substring(0,fullName.indexOf(" "))
                        val surname=fullName.substring(fullName.indexOf(" ")+1)

                        //Generate Password Hash for user
                        val passwordSalt=Users.generateSalt()
                        val passwordHash=Users.encryptPassword(fieldPassword.text.toString(),passwordSalt)
                        val newUser=Users(0,fieldEmail.text.toString(),passwordHash,passwordSalt,Users.generateSalt(),firstName,surname,fieldPhoneNumber.text.toString())
                        val returnCode=database.addUsers(newUser)

                        //Check return code to make sure data was saved to the database
                        toastMessage = when (returnCode) {
                            DatabaseErrors.SUCCESS -> {
                                btnCancel(view)
                                return
                            }
                            DatabaseErrors.EMAIL -> getString(R.string.emailExists)
                            else -> getString(R.string.databaseError)
                        }
                    }else{
                        toastMessage=getString(R.string.readTermsAndConditions)
                    }
                }
            }else{
                toastMessage=getString(R.string.enterEmail)
            }
        }else{
            toastMessage=getString(R.string.enterName)
        }

        Toast.makeText(this,toastMessage,Toast.LENGTH_SHORT).show()
    }
}