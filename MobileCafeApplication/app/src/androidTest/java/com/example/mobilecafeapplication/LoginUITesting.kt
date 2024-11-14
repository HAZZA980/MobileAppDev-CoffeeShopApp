package com.example.mobilecafeapplication

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobilecafeapplication.controller.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUITesting {

    @get:Rule
    val activityRule= ActivityScenarioRule(LoginScreen::class.java)

    /**
     * createValidAccount
     *
     * Create an Account for possible login
     *
     * @param name the name for the account
     * @param email the email for the account
     * @param password the password for the account
     */
    private fun createValidAccount(name:String,email:String,password:String){
        onView(withId(R.id.loginScreenBtnRegister)).perform(click())
        TestingHelperFunctions.fillInUserForm(name,email,password)
        onView(withId(R.id.registerScreenBtnRegister)).perform(click())
    }

    /**
     * createAccountAndLogin
     *
     * Creates a user account on the Register page and then logs in with those details
     */
    @Test
    fun createAccountAndLogin(){
        val name="John Doe"
        val email="johndoe@testing.com"
        val password="Toor2743"

        //Create user
        createValidAccount(name,email,password)

        //Enter user details
        TestingHelperFunctions.fillInLoginDetails(email,password)
        onView(withId(R.id.fragHome)).check(matches(isDisplayed()))
    }

    /**
     * attemptLoginWithInvalidAccount
     *
     * Try to login to the application when no accounts exist, and error should be raised.
     * Create an account and login with different details expecting the error to be raised again.
     */
    @Test
    fun attemptLoginWithInvalidAccount(){
        val name="John Dove"
        val email="johndove@gmail.com"
        val password="T3mpt1ngMe"

        //Attempt Login When No Accounts Exist
        TestingHelperFunctions.fillInLoginDetails("testing@testing.com","P4ssw0rd")

        //Check Error Generated
        onView(withText(R.string.invalidLogin)).inRoot(ToastMatcher().apply{matches(isDisplayed())})

        //Create Account
        createValidAccount(name,email,password)

        //Attempt to login with an account that doesn't exist
        TestingHelperFunctions.fillInLoginDetails("testing@testing.com","P4ssw0rd")

        //Check Error Generated=
        onView(withText(R.string.invalidLogin)).inRoot(ToastMatcher().apply{matches(isDisplayed())})
    }

    /**
     * attemptLoginInvalidDetails
     *
     * Create a valid user account.
     * Attempt to login with valid email but invalid password.
     * Attempt to login with valid password but invalid email.
     * Login with the correct details.
     */
    @Test
    fun attemptLoginInvalidDetails(){
        val name="Harry James"
        val email="james.harry@outlook.com"
        val password="P4ssw0rd"

        //Create Account
        createValidAccount(name,email,password)

        //Attempt Login Invalid Email
        TestingHelperFunctions.fillInLoginDetails("harry.james@gmail.com",password)
        onView(withText(R.string.invalidLogin)).inRoot(ToastMatcher().apply{matches(isDisplayed())})

        //Attempt Login Invalid Password
        TestingHelperFunctions.fillInLoginDetails(email,"dr0wss4P")
        onView(withText(R.string.invalidLogin)).inRoot(ToastMatcher().apply{matches(isDisplayed())})

        //Login Correct Details
        TestingHelperFunctions.fillInLoginDetails(email,password)
        onView(withId(R.id.fragHome)).check(matches(isDisplayed()))
    }
}