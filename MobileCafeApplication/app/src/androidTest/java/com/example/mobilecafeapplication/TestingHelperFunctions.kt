package com.example.mobilecafeapplication

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId

class TestingHelperFunctions {
    companion object{

        /**
         * fillInLoginDetails
         *
         * Function to fill in all the details on the LoginScreen to speed up testing
         *
         * @param email the email to login to the app with
         * @param password the password to login to the app with
         */
        fun fillInLoginDetails(email:String,password:String){
            onView(withId(R.id.loginScreenEmail)).perform(replaceText(email))
            onView(withId(R.id.loginScreenPassword)).perform(replaceText(password))
            onView(withId(R.id.loginScreenBtnLogin)).perform(click())
        }

        /**
         * fillInUserForm
         *
         * Function to fill in all the details on the RegisterScreen to speed up testing
         *
         * @param name the name to enter to the form
         * @param email the email to enter to the form
         * @param password the password to enter to the form
         */
        fun fillInUserForm(name:String,email:String,password:String){
            onView(withId(R.id.registerScreenFullName)).perform(replaceText(name))
            onView(withId(R.id.registerScreenEmail)).perform(replaceText(email))
            onView(withId(R.id.registerScreenPassword)).perform(replaceText(password))
            onView(withId(R.id.registerScreenConfirmPassword)).perform(replaceText(password))
            onView(withId(R.id.registerScreenTermsAndConditionsBox)).perform(click())
        }

        /**
         * createUserAndLogin
         *
         * Create a user for the test environment and log that user into the application
         *
         * @param name the name of the user to create
         * @param email the email for the created user
         * @param password the password to assign to the created user account
         */
        fun createUserAndLogin(name:String,email:String,password:String){
            onView(withId(R.id.loginScreenBtnRegister)).perform(click())
            fillInUserForm(name,email,password)
            onView(withId(R.id.registerScreenBtnRegister)).perform(click())
            fillInLoginDetails(email,password)
        }
    }
}