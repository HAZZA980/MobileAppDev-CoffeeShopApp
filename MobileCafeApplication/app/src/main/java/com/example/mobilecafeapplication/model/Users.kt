package com.example.mobilecafeapplication.model

import android.content.Context
import android.widget.Toast
import com.example.mobilecafeapplication.R
import java.io.Serializable
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class Users(private val _userId:Int,private var _userEmail:String, var passwordHash:String, var saltPrimary:ByteArray, val saltSecondary:ByteArray, private var _userFirstName:String, private var _userSurname:String, var userPhoneNumber:String, var isAdmin:Boolean, var isActive:Boolean):Serializable{

    /**
     * userId
     *
     * If the user does not already have a user ID automatically generate user ID that is stored
     * within the Database as the Primary Key. Once this has been generated it cannot be changed.
     */
    val userId:Int = if(_userId==0) "$_userEmail$passwordHash$saltPrimary$saltSecondary$_userFirstName$_userSurname".hashCode() else _userId

    /**
     * userEmail
     *
     * The users email for the database.
     * The user can change their email but the email can never be null.
     */
    var userEmail:String=_userEmail
        set(value){
            if(value.isNotBlank()){
                field=value
            }
        }

    /**
     * userFirstName
     *
     * The users first name for the database.
     * The user can change their name but the name can never be null.
     */
    var userFirstName:String=_userFirstName
        set(value){
            if(value.isNotBlank()){
                field=value
            }
        }

    /**
     * userSurname
     *
     * The users surname for the database
     * The user can change their name but the name can never be null.
     */
    var userSurname:String=_userSurname
        set(value){
            if(value.isNotBlank()){
                field=value
            }
        }

    /**
     * constructor
     *
     * User constructor for creating a new account for the application
     *
     * @param userId
     * @param userEmail email address of the user
     * @param passwordHash the password the user entered that has gone through the hashing process
     * @param saltPrimary the salt used to hash the password
     * @param saltSecondary salt to be used when encrypting and decrypting other items for the database
     * @param userFirstName the first name of the user
     * @param userSurname the surname of the user
     * @param userPhoneNumber the phone number of the user, can be null
     */
    constructor(userId:Int,userEmail:String,passwordHash:String,saltPrimary:ByteArray,saltSecondary:ByteArray,userFirstName:String,userSurname:String,userPhoneNumber:String)  : this(userId,userEmail,passwordHash,saltPrimary,saltSecondary,userFirstName,userSurname,userPhoneNumber,false,true)

    /**
     * decodeKey
     *
     * Turn the passwordHash string into a SecretKeySpec that can be used for encryption
     *
     * @return generated SecretKeySpec from the passwordHash
     */
    private fun decodeKey():SecretKeySpec{
        val decodedKey=Base64.getDecoder().decode(this.passwordHash)
        return SecretKeySpec(decodedKey,0,decodedKey.size,"AES")
    }

    /**
     * encryptData
     *
     * Process for encrypting data that is going to be sent to the database.
     * This is only used for the sensitive user information that is being added into the database.
     *
     * @param data the data to encrypt
     * @return the encrypted data string
     */
    fun encryptData(data: String): String {
        val ivParameters=IvParameterSpec(this.saltSecondary)
        val cipher=Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE,decodeKey(),ivParameters)
        val encodeData=cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encodeData)
    }

    /**
     * decryptData
     *
     * Process for decrypting the data that has gone through the encryption process.
     * Data being received from the database is encrypted and this process decrypts it.
     * Only used for sensitive user information that is being stored within the database.
     *
     * @param encryptedData the encrypted data from the database
     * @return the decrypted data
     */
    fun decryptData(encryptedData: String): String {
        val decodedData= Base64.getDecoder().decode(encryptedData)
        val ivParameters=IvParameterSpec(this.saltSecondary)
        val cipher=Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE,decodeKey(),ivParameters)
        return String(cipher.doFinal(decodedData))
    }

    companion object{
        private const val ALGORITHM:String="PBKDF2WithHmacSHA256"
        private const val ITERATIONS:Int=97672
        private const val KEY_LENGTH:Int=256

        /**
         * encryptPassword
         *
         * Process of hashing the password the user has entered using a generated salt.
         * This prevents the users password from ever being stored.
         *
         * @param password the password of the user to be hashes
         * @param salt the salt to be applied to the password before hashing
         * @return the hashed password
         */
        fun encryptPassword(password:String,salt:ByteArray):String{
            val passwordArray=password.toCharArray()
            val secretKeyGeneration=SecretKeyFactory.getInstance(ALGORITHM)
            val specification=PBEKeySpec(passwordArray,salt, ITERATIONS,KEY_LENGTH)
            val generateSpecification=secretKeyGeneration.generateSecret(specification)
            val hashKey=SecretKeySpec(generateSpecification.encoded,"AES")
            return Base64.getEncoder().encodeToString(hashKey.encoded)
        }

        /**
         * generateSalt
         *
         * Process of generating a random salt for the user.
         *
         * @return the salt generated
         */
        fun generateSalt():ByteArray{
            val salt=ByteArray(16)
            SecureRandom().nextBytes(salt)
            return salt
        }

        /**
         * checkEmailValid
         *
         * Validate that the string entered contains an '@'
         *
         * @param email the email string to validate
         * @return boolean of whether we consider the email valid (true) or not (false)
         */
        fun checkEmailValid(email:String):Boolean{
            return (email.indexOf('@')!=-1)&&(email.indexOf('.')!=-1)
        }

        /**
         * checkPassword
         *
         * Validates that the entered password is correct against the regex.
         * If the password does not pass the regex test then an error is displayed
         * to the passed TextView for the user
         *
         * @param password the password to check against the regex
         * @param context of the application to allow for strings utilisation
         * @return boolean whether the password is strong enough
         */
        fun checkPasswordString(password:String,context:Context):Boolean{
            //Password should contain one uppercase, one lowercase letter and one number
            //Should be at least 8 characters in length
            val passwordLengthRegex=Regex(".{8,}")
            val passwordUppercaseRegex=Regex(".*[A-Z].*")
            val passwordLowercaseRegex=Regex(".*[a-z].*")
            val passwordNumberRegex=Regex(".*[0-9].*")

            var toastMessage:String

            if(passwordNumberRegex.matches(password)){
                toastMessage = if(passwordLowercaseRegex.matches(password)){
                    if(passwordUppercaseRegex.matches(password)){
                        if(passwordLengthRegex.matches(password)){
                            return true
                        }else{
                            context.getString(R.string.passwordLength)
                        }
                    }else{
                        context.getString(R.string.passwordContainUppercase)
                    }
                }else{
                    context.getString(R.string.passwordContainLowercase)
                }
            }else{
                toastMessage=context.getString(R.string.passwordContainNumber)
            }
            Toast.makeText(context,toastMessage,Toast.LENGTH_SHORT).show()
            return false
        }

        /**
         * checkPasswordsMatch
         *
         * Check that the passwords entered match.
         * If they do not match raise an error to the passed TextView
         *
         * @param password the main password the user has entered.
         * @param passwordConfirmation repeat of the previous password to confirm the users intention.
         * @param context of the application to allow for strings utilisation
         * @return boolean whether the passwords match
         */
        fun checkPasswordsMatch(password:String,passwordConfirmation:String,context: Context):Boolean{
            if(password == passwordConfirmation){
                return true
            }
            Toast.makeText(context,context.getString(R.string.passwordsMatch),Toast.LENGTH_SHORT).show()
            return false
        }
    }
}
