package com.example.mobilecafeapplication

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobilecafeapplication.controller.RegisterScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationUITesting {

    @get:Rule
    val activityRule= ActivityScenarioRule(RegisterScreen::class.java)

    /**
     * pressRegisterButton
     *
     * Function to click the Register button on the registration screen
     */
    private fun pressRegisterButton(){
        onView(withId(R.id.registerScreenBtnRegister)).perform(click())
    }

    /**
     * testEmailValidation
     *
     * Check that when entering a string without an @ it is classified as an
     * invalid email. Validate that the correct error is displayed when the email
     * is classified as incorrect.
     */
    private fun testEmailValidation(){
        val validEmail="testing@testing.com"
        val invalidEmail="testing"

        onView(withId(R.id.registerScreenEmail)).perform(typeText(invalidEmail))
        pressRegisterButton()
        onView(withText(R.string.enterEmail)).inRoot(ToastMatcher().apply{matches(isDisplayed())})
        onView(withId(R.id.registerScreenEmail)).perform(replaceText(validEmail))
        pressRegisterButton()
    }

    /**
     * enterPasswordString
     *
     * Pass a string to the password field on the Registration screen
     *
     * @param value the value to write to the password field
     */
    private fun enterPasswordString(value:String){
        onView(withId(R.id.registerScreenPassword)).perform(replaceText(value))
    }

    /**
     * passwordValidation
     *
     * Check that as the password is enter the correct errors are displayed on
     * the registration error section for the password field and password confirmation
     * field.
     */
    @Test
    fun passwordValidation(){
        val validPassword="1lLhello"
        enterPasswordString("@")

        //Check that Password demands a digit be entered
        onView(withText(R.string.passwordContainNumber)).inRoot(ToastMatcher().apply{matches(isDisplayed())})
        enterPasswordString("@1")

        //Check that password asks for lowercase letter
        onView(withText(R.string.passwordContainLowercase)).inRoot(ToastMatcher().apply{matches(isDisplayed())})
        enterPasswordString("@1l")

        //Check that password asks for uppercase letter
        onView(withText(R.string.passwordContainUppercase)).inRoot(ToastMatcher().apply{matches(isDisplayed())})
        enterPasswordString("@1lL")

        //Check that password asks to be 8 characters long
        onView(withText(R.string.passwordLength)).inRoot(ToastMatcher().apply{matches(isDisplayed())})
        enterPasswordString(validPassword)

        //Verify that passwords must match
        onView(withId(R.id.registerScreenConfirmPassword)).perform(typeText("@1l"))
        onView(withText(R.string.passwordsMatch)).inRoot(ToastMatcher().apply{matches(isDisplayed())})

        onView(withId(R.id.registerScreenConfirmPassword)).perform((replaceText(validPassword)))
    }

    /**
     * completeRegistrationIncrementally
     *
     * Check that as each section of the registration form is completed the correct
     * error is displayed prompting the user to enter the next value.
     */
    @Test
    fun completeRegistrationIncrementally(){
        //Test that Name error appears first
        pressRegisterButton()
        onView(withText(R.string.enterName)).inRoot(ToastMatcher().apply{matches(isDisplayed())})
        onView(withId(R.id.registerScreenFullName)).perform(typeText("Daniel Judd"))

        //Test Email validation works
        testEmailValidation()

        //Possible Phone number verification

        //Check Password verification and validation
        onView(withText(R.string.passwordContainNumber)).inRoot(ToastMatcher().apply{matches(isDisplayed())})
        passwordValidation()

        //Check Terms & Conditions CheckBox
        onView(withId(R.id.registerScreenTermsAndConditionsBox)).check(matches(isNotChecked()))
        onView(withId(R.id.registerScreenTermsAndConditionsBox)).perform(click())
        onView(withId(R.id.registerScreenTermsAndConditionsBox)).check(matches(isChecked()))
    }

    /**
     * completeRegistrationStraight
     *
     * Complete the form all in one go and check that the errors are blank and
     * that when register is pressed no errors are displayed.
     */
    @Test
    fun completeRegistrationStraight(){
        val validPassword="1lLhello"

        TestingHelperFunctions.fillInUserForm("Daniel James","daniel.james@gmail.com",validPassword)
        onView(withId(R.id.registerScreenFullName)).perform(typeText("Daniel Judd"))

        pressRegisterButton()
        onView(withId(R.id.loginScreen)).check(matches(isDisplayed()))
    }

    /**
     * attemptCreateSameUser
     *
     * Creating a user with the same details as a user already within the database should result
     * in an error being displayed to the user.
     */
    @Test
    fun attemptCreateSameUser(){
        val name="Daniel Judd"
        val email="testing@testing.com"
        val password="P4ssw0rd"

        //Create user with details
        TestingHelperFunctions.fillInUserForm(name,email,password)
        pressRegisterButton()

        //Navigate back to RegisterScreen
        onView(withId(R.id.loginScreenBtnRegister)).perform(click())

        //Try create same user
        TestingHelperFunctions.fillInUserForm(name,email,password)
        pressRegisterButton()
        onView(withText(R.string.emailExists)).inRoot(ToastMatcher().apply{matches(isDisplayed())})
    }
}