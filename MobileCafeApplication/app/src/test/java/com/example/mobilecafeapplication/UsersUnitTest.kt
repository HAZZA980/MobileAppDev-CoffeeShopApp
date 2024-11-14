package com.example.mobilecafeapplication

import com.example.mobilecafeapplication.model.Users
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsersUnitTest {

    /**
     * primaryConstructor - Testing
     *
     * Check that the entered details are correctly stored within the Users instance.
     * Make sure that the entered data can be modified once created
     */
    @Test
    fun primaryConstructor(){
        var userEmail="testing@testing.com"
        val salt1=Users.generateSalt()
        val salt2=Users.generateSalt()
        val passwordEncrypt=Users.encryptPassword("P4ssw0rd",salt1)
        val firstName="Daniel"
        val surname="Judd"
        val userInstance=Users(0,userEmail,passwordEncrypt,salt1,salt2,firstName,surname,"",isAdmin=true,isActive=true)

        //Confirm Stored Values Correct
        assertEquals(userEmail,userInstance.userEmail)
        assertEquals(firstName,userInstance.userFirstName)
        assertEquals(surname,userInstance.userSurname)
        assertEquals("",userInstance.userPhoneNumber)
        assertTrue(userInstance.isAdmin)
        assertTrue(userInstance.isActive)

        //Check generated userId
        val sameUser=Users(0,userEmail,passwordEncrypt,salt1,salt2,firstName,surname,"",isAdmin=true,isActive=true)
        val differentUser=Users(0,userEmail,passwordEncrypt,salt1,Users.generateSalt(),firstName,surname,"",isAdmin=true,isActive=true)
        assertEquals(sameUser.userId,userInstance.userId)
        assertNotEquals(sameUser.userId,differentUser.userId)

        //Update Values & Confirm Updated
        userInstance.isAdmin=false
        assertFalse(userInstance.isAdmin)

        userEmail="daniel.judd@gmail.com"
        assertNotEquals(userInstance.userEmail,userEmail)
        userInstance.userEmail=userEmail
        assertEquals(userInstance.userEmail,userEmail)
    }

    /**
     * secondaryConstructor - Testing
     *
     * Check that the entered details are correctly stored within the Users instance.
     * Check that the default values set by the constructor. Change the values and make
     * sure they are updated.
     */
    @Test
    fun overloadConstructorWithPhone(){
        val userEmail="testing@testing.com"
        val salt1=Users.generateSalt()
        val salt2=Users.generateSalt()
        val passwordEncrypt=Users.encryptPassword("P4ssw0rd",salt1)
        val firstName="Daniel"
        val surname="Judd"

        val userInstance=Users(0,userEmail,passwordEncrypt,salt1,salt2,firstName,surname,"")

        //Check the values stored are the expected values
        assertEquals(userEmail,userInstance.userEmail)
//        assertEquals(passwordEncrypt.hashCode(),userInstance.passwordEncrypted.hashCode())
        assertEquals(firstName,userInstance.userFirstName)
        assertEquals(surname,userInstance.userSurname)
        assertEquals("",userInstance.userPhoneNumber)
        assertFalse(userInstance.isAdmin)

        //Update some of the default constructor values
        userInstance.isActive=false
        assertFalse(userInstance.isActive)

        //Return values to default
        userInstance.isActive=true
        assertTrue(userInstance.isActive)

        //Update email
        val newEmail="test@test.com"
        assertNotEquals(userInstance.userEmail,newEmail)
        userInstance.userEmail=newEmail
        assertEquals(userInstance.userEmail,newEmail)
    }

    /**
     * checkingEncryptedPasswordsMatch - Testing
     *
     * Check that when generating a salt and password combination the same hash is generated
     * if the same salt and password combination is entered.
     * Also check that with a different password and the same salt (and different salt same password)
     * that the encrypted password is different.
     */
    @Test
    fun checkingEncryptedPasswordsMatch(){
        val password="P4ssw0rd"
        val salt= Users.generateSalt()
        val encryptPassword=Users.encryptPassword(password,salt).hashCode()

        assertEquals(encryptPassword,Users.encryptPassword(password,salt).hashCode()) //Same Password & Salt
        assertNotEquals(encryptPassword,Users.encryptPassword(password,Users.generateSalt()).hashCode()) //Same Password different Salt
        assertNotEquals(encryptPassword,Users.encryptPassword("testing",salt).hashCode()) //Different Password same Salt
        assertNotEquals(encryptPassword,Users.encryptPassword("testing",Users.generateSalt()).hashCode()) //Different Password & Salt
    }

    /**
     * encryptingDataMatchesDecryptedData - Testing
     *
     * Checking that encrypting and decrypting data for Users works as expected.
     * Encrypted data does not match decrypted data
     * Data before encryption matches data after being decrypted
     */
    @Test
    fun encryptingDataMatchesDecryptedData(){
        val dataToEncrypt="This is very special data"
        val differentData="This is not special data"

        //Create User
        val passwordSalt=Users.generateSalt()
        val userInstance=Users(0,"testing@testing.com",Users.encryptPassword("P4ssw0rd",passwordSalt),passwordSalt,Users.generateSalt(),"Daniel","Judd","")

        //Confirm that encrypted data using same keys has same output
        val encryptedDataSpecial=userInstance.encryptData(dataToEncrypt)
        val encryptedData=userInstance.encryptData(differentData)
        assertNotEquals(encryptedDataSpecial,dataToEncrypt)
        assertNotEquals(encryptedDataSpecial,differentData)
        assertNotEquals(encryptedDataSpecial,encryptedData)
        assertEquals(encryptedDataSpecial,userInstance.encryptData(dataToEncrypt))

        //Confirm different users have different encrypted data
        val secondaryUser=Users(0,"testing@testing.com",Users.encryptPassword("P4ssw0rd",passwordSalt),passwordSalt,Users.generateSalt(),"Daniel","Judd","")
        assertNotEquals(secondaryUser.encryptData(dataToEncrypt),userInstance.encryptData(dataToEncrypt))
        assertNotEquals(secondaryUser.encryptData(differentData),userInstance.encryptData(dataToEncrypt))

        //Confirm that decrypted data using same keys has same output
        val decryptedData=userInstance.decryptData(encryptedDataSpecial)
        assertEquals(dataToEncrypt,decryptedData)
        assertNotEquals(encryptedData,dataToEncrypt)

    }
}