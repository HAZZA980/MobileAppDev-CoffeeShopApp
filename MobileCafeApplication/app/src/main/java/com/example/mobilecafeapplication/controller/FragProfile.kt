package com.example.mobilecafeapplication.controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.mobilecafeapplication.DatabaseErrors
import com.example.mobilecafeapplication.DatabaseLinker
import com.example.mobilecafeapplication.R
import com.example.mobilecafeapplication.model.Users


class FragProfile(private val user:Users):Fragment() {

    private lateinit var context:Context
    private lateinit var database:DatabaseLinker

    override fun onCreateView(inflater:LayoutInflater,container:ViewGroup?,savedInstanceState:Bundle?):View? {
        val view=inflater.inflate(R.layout.frag_profile, container, false)
        context=requireContext()
        database=DatabaseLinker(context)

        // Prevent items from appearing above the notification line on the phone
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.fragProfile)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Get all the fields to pre-populate values
        val fullNameField=view.findViewById<EditText>(R.id.edtTextUserFullName)
        val emailField=view.findViewById<EditText>(R.id.edtTextUserEmail)
        val phoneNumber=view.findViewById<EditText>(R.id.edtTextPhoneNo)

        val userFullName="${user.userFirstName} ${user.userSurname}"
        fullNameField.setText(userFullName)
        emailField.setText(user.userEmail)

        if(user.userPhoneNumber.isNotEmpty()){
            phoneNumber.setText(user.userPhoneNumber)
        }

        //Work out if admin fields should be viewable
        if(user.isAdmin){
            val adminArea=view.findViewById<LinearLayout>(R.id.adminArea)
            adminArea.visibility=View.VISIBLE
        }

        //Attach password change listeners for updating password
        val password=view.findViewById<EditText>(R.id.edtTxtNewPassword)
        password.doOnTextChanged {text,_,_,_ ->
            Users.checkPasswordString(text.toString(),context)
        }

        val passwordConfirmation=view.findViewById<EditText>(R.id.edtTxtConfirmPassword)
        passwordConfirmation.doOnTextChanged {text,_,_,_ ->
            Users.checkPasswordsMatch(password.text.toString(),text.toString(),context)
        }

        //Add click listeners to buttons
        val updateDetailsBtn=view.findViewById<Button>(R.id.btnUpdateDetails)
        updateDetailsBtn.setOnClickListener{btnUpdateDetails(view)}

        val changePasswordBtn=view.findViewById<Button>(R.id.btnChangePassword)
        changePasswordBtn.setOnClickListener{btnUpdatePassword(view)}

        val deleteAccountBtn=view.findViewById<Button>(R.id.profileScreenBtnDeleteAccount)
        deleteAccountBtn.setOnClickListener{btnDeleteAccount(view)}

        val makeAdminBtn=view.findViewById<Button>(R.id.btnAdmitUser)
        makeAdminBtn.setOnClickListener{btnMakeAccountAdmin(view)}

        return view
    }

    /**
     * btnUpdateDetails
     *
     * Check that the details filled in have been changed and are not the same as the ones already
     * stored. If the details have changed update them in the user instance and then call the
     * updateUser function within the DatabaseLinker to update any possible values that might have
     * changed within the database.
     */
    private fun btnUpdateDetails(view:View){
        if(database.defaultAdminEmail==user.userEmail&&user.isAdmin){
            Toast.makeText(context,R.string.modifyDefaultAdmin,Toast.LENGTH_SHORT).show()
        }else{
            val fullNameField=view.findViewById<EditText>(R.id.edtTextUserFullName)
            val emailField=view.findViewById<EditText>(R.id.edtTextUserEmail)
            val phoneNumber=view.findViewById<EditText>(R.id.edtTextPhoneNo)

            val fullName=fullNameField.text.toString()
            val firstName=fullName.substring(0,fullName.indexOf(" "))
            val surname=fullName.substring(fullName.indexOf(" "))

            if(emailField.text.isNotEmpty()&&Users.checkEmailValid(emailField.text.toString())&&(emailField.text.toString()!=user.userEmail)){
                user.userEmail=emailField.text.toString()
            }else if((emailField.text.isEmpty()||!Users.checkEmailValid(emailField.text.toString()))&&(emailField.text.toString()!=user.userEmail)){
                Toast.makeText(context,R.string.enterEmail,Toast.LENGTH_SHORT).show()
            }

            if(fullNameField.text.isNotEmpty()){
                if(user.userFirstName!=firstName){
                    user.userFirstName=firstName
                }
                if(user.userSurname!=surname){
                    user.userSurname=surname
                }
            }

            if(phoneNumber.text.isNotEmpty()&&phoneNumber.text.toString()!=user.userPhoneNumber){
                user.userPhoneNumber=phoneNumber.text.toString()
            }

            database.updateUser(user)
        }
    }

    /**
     * btnUpdatePassword
     *
     * Verify that the old password that they have entered matches their current password.
     * Check that the new password conforms to the password rules for the application.
     * Check that the password confirmation matches the new password.
     * Providing all details are correct update the password in the user instance and then push to
     * the database. Once the database has been updated log out the user so they can log back in
     * with the new password.
     */
    private fun btnUpdatePassword(view:View){
        val oldPassword=view.findViewById<EditText>(R.id.edtTxtOldPassword)
        val newPassword=view.findViewById<EditText>(R.id.edtTxtNewPassword)
        val newPasswordConfirmation=view.findViewById<EditText>(R.id.edtTxtConfirmPassword)

        //Confirm Old Password
        val hashedPassword=Users.encryptPassword(oldPassword.text.toString(),user.saltPrimary)
        if(hashedPassword==user.passwordHash){
            if(Users.checkPasswordString(newPassword.text.toString(),context)&&Users.checkPasswordsMatch(newPassword.text.toString(),newPasswordConfirmation.text.toString(),context)){
                if(!Users.checkPasswordsMatch(oldPassword.text.toString(),newPassword.text.toString(),context)){
                    val passwordHash=Users.encryptPassword(newPassword.text.toString(),user.saltPrimary)
                    user.passwordHash=passwordHash
                    database.updateUser(user)
                    val loginScreen=Intent(context,LoginScreen::class.java)
                    startActivity(loginScreen)
                    return
                }else{
                    Toast.makeText(context,R.string.newOldPasswordMatch,Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            Toast.makeText(context,R.string.invalidPassword, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * btnDeleteAccount
     *
     * Delete the user account and then log them out of the application.
     */
    private fun btnDeleteAccount(view:View){
        if(database.defaultAdminEmail==user.userEmail&&user.isAdmin){
            Toast.makeText(context,R.string.deleteDefaultAdmin,Toast.LENGTH_SHORT).show()
        }else{
            database.deleteUser(user)
            val loginScreen=Intent(context,LoginScreen::class.java)
            startActivity(loginScreen)
        }
        return
    }

    /**
     * btnMakeAccountAdmin
     *
     * Using the specified email address update the associated account and make
     * them an admin of the application.
     */
    private fun btnMakeAccountAdmin(view:View){
        val accountEmail=view.findViewById<EditText>(R.id.edtAdmitEmail)

        if(Users.checkEmailValid(accountEmail.text.toString())){
            val returnCode=database.makeUserAdmin(accountEmail.text.toString())
            when (returnCode) {
                DatabaseErrors.EMAIL -> Toast.makeText(context,R.string.userDoesNotExist,Toast.LENGTH_SHORT).show()
                DatabaseErrors.UNKNOWN_ERROR -> Toast.makeText(context,R.string.databaseError,Toast.LENGTH_SHORT).show()
                else -> {
                    if(database.defaultAdminEmail==user.userEmail&&user.isAdmin){
                        val loginScreen=Intent(context,LoginScreen::class.java)
                        startActivity(loginScreen)
                    }
                    return
                }
            }
        }else{
            Toast.makeText(context,R.string.enterEmail,Toast.LENGTH_SHORT).show()
        }
    }
}