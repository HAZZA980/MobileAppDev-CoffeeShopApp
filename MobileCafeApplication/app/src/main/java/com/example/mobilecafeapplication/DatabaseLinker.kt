package com.example.mobilecafeapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.ContactsContract.Data
import com.example.mobilecafeapplication.model.Notifications
import android.util.Log
import com.example.mobilecafeapplication.model.NotificationStatus
import com.example.mobilecafeapplication.model.OrderItems
import com.example.mobilecafeapplication.model.Orders
import com.example.mobilecafeapplication.model.Product
import com.example.mobilecafeapplication.model.Users
import com.google.gson.Gson
import java.util.Collections
import java.util.Date

private const val DatabaseName="HotStuffDatabase"
private val version:Int=1

class DatabaseLinker(context:Context):SQLiteOpenHelper(context,DatabaseName,null,version){

    //Users Table
    private val usersTableName="Users"
    private val usersColumnId="UserId"
    private val usersColumnEmail="UserEmail"
    private val usersColumnPasswordHash="UserPasswordHash"
    private val usersColumnSalt1="PrimarySalt"
    private val usersColumnSalt2="SecondarySalt"
    private val usersColumnFirstName="UserFirstName"
    private val usersColumnSurname="UserSurname"
    private val usersColumnPhone="UserPhoneNumber"
    private val usersColumnAdmin="UserAdmin"
    private val usersColumnActive="UserActive"

    //Default Admin User
    val defaultAdminEmail="admin@admin.com"

    //Orders Table
    private val ordersTableName="Orders"
    private val ordersColumnNumber="OrderNumber"
    private val ordersColumnDate="OrderDate"
    private val ordersColumnItems="OrderedItems"
    private val ordersColumnUserId="UserId"

    //Products Table
    private val productsTableName = "Products"
    private val productsColumnId = "ProductId"
    private val productsColumnName = "ProductName"
    private val productsColumnDescription = "ProductDescription"
    private val productsColumnPrice = "ProductPrice"
    private val productsColumnStock="ProductStock"
    private val productsColumnInStock="InStock"
    private val productsColumnsIsHidden="IsHidden"

    //Notification Table
    private val notificationTableName = "Notifications"
    private val notificationID = "NotificationID"
    private val notificationTitle = "NotificationTitle"
    private val notificationDescription = "NotificationDescription"
    private val notificationTo="NotificationTo"
    private val notificationFrom="NotificationFrom"
    private val notificationIsOrder = "NotificationIsOrder"
    private val notificationStatus = "NotificationStatus"

    /**
     * onCreate
     *
     * Creation process for the tables that will be in use by the application, this is run
     * if the tables are not already present in the applications backend.
     *
     * Users table - where all the users information is stored for the application
     * Orders table - where user orders containing the items that they brought at what price are stored
     *
     * @param db the database the tables should be added too
     */
    override fun onCreate(db: SQLiteDatabase?){
        var sqlCreationCommand:String="CREATE TABLE $usersTableName ($usersColumnId INTEGER PRIMARY KEY,$usersColumnEmail TEXT NOT NULL, $usersColumnPasswordHash BLOB NOT NULL, $usersColumnSalt1 BLOB NOT NULL, $usersColumnSalt2 BLOB NOT NULL, $usersColumnFirstName TEXT NOT NULL, $usersColumnSurname TEXT NOT NULL, $usersColumnPhone TEXT, $usersColumnAdmin INTEGER NOT NULL, $usersColumnActive INTEGER NOT NULL)"
        db?.execSQL(sqlCreationCommand)

        sqlCreationCommand="CREATE TABLE $ordersTableName ($ordersColumnNumber INTEGER PRIMARY KEY AUTOINCREMENT,$ordersColumnDate TEXT NOT NULL,$ordersColumnItems TEXT NOT NULL,$ordersColumnUserId INTEGER,FOREIGN KEY ($ordersColumnUserId) REFERENCES $usersTableName($usersColumnId))"
        db?.execSQL(sqlCreationCommand)

        // Create Products Table
        sqlCreationCommand = """
        CREATE TABLE $productsTableName (
            $productsColumnId INTEGER PRIMARY KEY AUTOINCREMENT,
            $productsColumnName TEXT NOT NULL,
            $productsColumnPrice REAL NOT NULL,
            $productsColumnDescription TEXT,
            $productsColumnStock INTEGER NOT NULL,
            $productsColumnInStock INTEGER NOT NULL,
            $productsColumnsIsHidden INTEGER NOT NULL
        )
    """
        db?.execSQL(sqlCreationCommand)

        sqlCreationCommand = """
            CREATE TABLE $notificationTableName (
            $notificationID INTEGER PRIMARY KEY AUTOINCREMENT,
            $notificationTitle TEXT NOT NULL,
            $notificationDescription TEXT NOT NULL,
            $notificationTo INTEGER NOT NULL,
            $notificationFrom INTEGER NOT NULL,
            $notificationIsOrder BOOL NOT NULL,
            $notificationStatus INTEGER NOT NULL
            ) 
        """
        db?.execSQL(sqlCreationCommand)
    }

    /**
     * onUpgrade
     *
     * When changes have been made to the database and a user has upgraded to the new version, this
     * code allows the database changes to take place without errors.
     *
     * @param db the database to perform the operations on
     * @param oldVersion previous version number of the database
     * @param newVersion new version number of the database
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {} //No upgrade version yet

    // Users Table Functions

    /**
     * userExists
     *
     * Check to see if there is a user with a matching email already exists within the database.
     * This prevents a single user from creating multiple accounts with a single email address.
     *
     * @param email the email the user has provided to the application
     * @return boolean value representing whether the user exists (true) or not (false)
     */
    private fun userExists(email:String):Boolean{
        val dataBase:SQLiteDatabase=this.readableDatabase
        val sqlQuery="Select * FROM $usersTableName WHERE $usersColumnEmail LIKE '$email'"
        var userFound=false

        val databaseCursor:Cursor=dataBase.rawQuery(sqlQuery,null)
        if(databaseCursor.moveToFirst()){
            userFound=true
        }

        dataBase.close()
        databaseCursor.close()
        return userFound
    }

    /**
     * addUsers
     *
     * Process of adding a user to the database.
     * Check whether the user already exists within the database and return an appropriate error.
     *
     * @param user the user instance to create the database entry for.
     * @return enum error whether the user was successfully added or an error occurred.
     */
    fun addUsers(user:Users):DatabaseErrors{
        if(userExists(user.userEmail)){
            return DatabaseErrors.EMAIL
        }

        val dataBase:SQLiteDatabase=this.writableDatabase
        val information=ContentValues()

        information.put(usersColumnId,user.userId)
        information.put(usersColumnEmail,user.userEmail)
        information.put(usersColumnPasswordHash,user.passwordHash)
        information.put(usersColumnSalt1,user.saltPrimary)
        information.put(usersColumnSalt2,user.saltSecondary)
        information.put(usersColumnFirstName,user.encryptData(user.userFirstName))
        information.put(usersColumnSurname,user.encryptData(user.userSurname))
        information.put(usersColumnPhone,user.encryptData(user.userPhoneNumber))
        information.put(usersColumnAdmin,user.isAdmin)
        information.put(usersColumnActive,user.isActive)

        val returnCode=dataBase.insert(usersTableName,null,information)
        dataBase.close()

        if(returnCode == -1L){
            return DatabaseErrors.UNKNOWN_ERROR
        }
        return DatabaseErrors.SUCCESS
    }

    /**
     * getUser
     *
     * Get a user and their information from the database.
     *
     * @param email the entered email address of the user to search the database for.
     * @param password the password the user entered to check that they are the right user.
     * @return user instance if a user was found or null if information was missing.
     */
    fun getUser(email:String,password:String):Users?{
        val dataBase:SQLiteDatabase=this.readableDatabase
        val sqlQuery="SELECT * FROM $usersTableName WHERE $usersColumnEmail LIKE '$email'"
        val databaseCursor:Cursor=dataBase.rawQuery(sqlQuery,null)

        if(databaseCursor.moveToFirst()){
            dataBase.close()

            val hashedPassword=Users.encryptPassword(password,databaseCursor.getBlob(3))
            if(hashedPassword==databaseCursor.getString(2)){
                val isAdmin=databaseCursor.getInt(8)>0
                val isActive=databaseCursor.getInt(9)>0

                val tmpUser=Users(databaseCursor.getInt(0),databaseCursor.getString(1),databaseCursor.getString(2),databaseCursor.getBlob(3),databaseCursor.getBlob(4),databaseCursor.getString(5),databaseCursor.getString(6),databaseCursor.getString(7),isAdmin,isActive)

                tmpUser.userFirstName=tmpUser.decryptData(tmpUser.userFirstName)
                tmpUser.userSurname=tmpUser.decryptData(tmpUser.userSurname)
                tmpUser.userPhoneNumber=tmpUser.decryptData(tmpUser.userPhoneNumber)

                if(tmpUser.isActive){
                    return tmpUser
                }
            }
        }

        dataBase.close()
        databaseCursor.close()
        return null
    }

    /**
     * makeUserAdmin
     *
     * Using the passed email update the associated account and make them an
     * admin of the application by updating the admin flag.
     * With another account having admin access now disable the default admin account.
     *
     * @param email the email of the account to update
     */
    fun makeUserAdmin(email:String):DatabaseErrors{
        if(!userExists(email)){
            return DatabaseErrors.EMAIL
        }

        val dataBase:SQLiteDatabase=this.writableDatabase
        val information=ContentValues()

        information.put(usersColumnAdmin,true)
        val returnCode=dataBase.update(usersTableName,information,"$usersColumnEmail = '$email'",null)
        dataBase.close()

        updateAdminUser(false)

        return if(returnCode == 0) DatabaseErrors.UNKNOWN_ERROR else DatabaseErrors.SUCCESS
    }

    /**
     * updateAdminUser
     *
     * Update whether the default admin should be enabled or disabled. If the default admin is
     * disabled it cannot be logged into by as user.
     *
     * @param enabled boolean true to enable the default admin and false to disable the default admin.
     */
    private fun updateAdminUser(enabled:Boolean):DatabaseErrors{
        val database:SQLiteDatabase=this.writableDatabase
        val information=ContentValues()

        information.put(usersColumnActive,enabled)
        val returnCode=database.update(usersTableName,information,"$usersColumnEmail = '$defaultAdminEmail'",null)

        database.close()
        return if(returnCode == 0) DatabaseErrors.UNKNOWN_ERROR else DatabaseErrors.SUCCESS
    }

    /**
     * updateUser
     *
     * Process of updating a user within the database.
     * Takes all the details that are stored within the user instance of the class to make sure
     * the database is up to date with everything stored locally.
     *
     * @param user the user to update the details on
     * @return success code on whether the details were updated
     */
    fun updateUser(user:Users):DatabaseErrors{
        val dataBase:SQLiteDatabase=this.writableDatabase
        val information=ContentValues()

        information.put(usersColumnEmail,user.userEmail)
        information.put(usersColumnPasswordHash,user.passwordHash)
        information.put(usersColumnFirstName,user.userFirstName)
        information.put(usersColumnSurname,user.encryptData(user.userSurname))
        information.put(usersColumnPhone,user.encryptData(user.userPhoneNumber))

        val returnCode=dataBase.update(usersTableName,information,"$usersColumnId = '${user.userId}'",null)
        dataBase.close()

        return if(returnCode == 0) DatabaseErrors.UNKNOWN_ERROR else DatabaseErrors.SUCCESS
    }

    /**
     * deleteUser
     *
     * Delete a specific user with a given ID from the database.
     * If that user is an admin make sure there is another active admin user within the database.
     * If not then enabled the default admin user.
     *
     * @param user the user to delete from the database
     * @return success code on whether the user was deleted
     */
    fun deleteUser(user:Users):DatabaseErrors{
        var dataBase:SQLiteDatabase=this.writableDatabase
        val returnCode=dataBase.delete(usersTableName,"$usersColumnId = '${user.userId}'",null)
        dataBase.close()

        if(user.isAdmin){
            //If this user was an admin make the database readable and see if there are any other active admin users within the database
            dataBase=this.readableDatabase
            val sqlQuery="SELECT * FROM $usersTableName WHERE $usersColumnActive != 0 AND $usersColumnAdmin != 0"
            val databaseCursor:Cursor=dataBase.rawQuery(sqlQuery,null)
            if(!databaseCursor.moveToFirst()){
                updateAdminUser(true)
            }
            databaseCursor.close()
            dataBase.close()
        }

        return if(returnCode == 0) DatabaseErrors.UNKNOWN_ERROR else DatabaseErrors.SUCCESS
    }

    // Orders Table Functions

    /**
     * getOrders
     *
     * Get all of the orders where the passed userId matches the userId column
     * of the orders table.
     *
     * @param userId the userId to match in the order row
     */
    fun getOrders(userId:Int):ArrayList<Orders>?{
        Log.d("DatabaseSearch","Getting Order")
        val dataBase:SQLiteDatabase=this.readableDatabase
        val sqlQuery="SELECT * FROM $ordersTableName WHERE $ordersColumnUserId LIKE '$userId'"

        val databaseCursor:Cursor=dataBase.rawQuery(sqlQuery,null)
        val orders = ArrayList<Orders>()

        while (databaseCursor.moveToNext()) {
            val orderNumb = databaseCursor.getInt(0)
            val orderDate = Date(databaseCursor.getString(1))
            val jsonString = databaseCursor.getString(2)
            val userId=databaseCursor.getInt(3)

            val itemList = Gson().fromJson(jsonString, Array<OrderItems>::class.java).toMutableList() as ArrayList<OrderItems>
            orders.add(Orders(orderNumb,orderDate,itemList,userId))
            databaseCursor.moveToNext()
        }
        databaseCursor.close()

        return if(orders.size==0) null else orders
    }

    /**
     * addOrder
     *
     * Add information tot he Orders dataBase after a new order has been placed.
     *
     * @param orderReceipt the class instance of Orders storing all of the information about the order
     * @param userId the ID of the user that made the purchase
     */
    fun addOrder(orderReceipt:Orders, userId:Int):DatabaseErrors{
        val dataBase:SQLiteDatabase=this.writableDatabase
        val information=ContentValues()

        val jsonString=Gson().toJson(orderReceipt.orderedItems)

        information.put(ordersColumnDate,orderReceipt.orderDate.toString())
        information.put(ordersColumnItems,jsonString)
        information.put(ordersColumnUserId,userId)

        val returnCode=dataBase.insert(ordersTableName,null,information)
        dataBase.close()

        if(returnCode == -1L){
            return DatabaseErrors.UNKNOWN_ERROR
        }
        return DatabaseErrors.SUCCESS
    }

    /**
     * getProducts
     *
     * Retrieves all products from the Products table.
     *
     * @return List of products.
     */
    fun getProducts(isAdmin:Boolean): ArrayList<Product> {
        val dataBase: SQLiteDatabase = this.readableDatabase
        val sqlQuery = "SELECT * FROM $productsTableName"
        val databaseCursor: Cursor = dataBase.rawQuery(sqlQuery, null)

        val products = ArrayList<Product>()

        while (databaseCursor.moveToNext()) {
            val productId = databaseCursor.getInt(0)
            val productName = databaseCursor.getString(1)
            val productDescription=databaseCursor.getString(3)
            val productPrice=databaseCursor.getFloat(2)
            val productStock=databaseCursor.getInt(4)
            val productInStock=databaseCursor.getInt(5)>0
            val productIsHidden=databaseCursor.getInt(6)>0

            if(!productIsHidden&&(productStock>0)){
                products.add(Product(productId,productName,productDescription,productPrice,productStock,productInStock,productIsHidden))
            }else if(isAdmin){
                products.add(Product(productId,productName,productDescription,productPrice,productStock,productInStock,productIsHidden))
            }
        }

        databaseCursor.close()
        dataBase.close()

        return products
    }

    /**
     * updateProducts
     *
     * Update the information of a product stored within the database
     *
     * @param products an arraylist of products to update
     */
    fun updateProducts(products:ArrayList<Product>){
        val dataBase:SQLiteDatabase=this.writableDatabase

        for(prod in products){
            val information=ContentValues()

            val stock=prod.productStock!=0

            information.put(productsColumnName,prod.productName)
            information.put(productsColumnDescription,prod.productDescription)
            information.put(productsColumnPrice,prod.productPrice)
            information.put(productsColumnStock,stock)
            information.put(productsColumnInStock,prod.inStock)
            information.put(productsColumnsIsHidden,prod.isHidden)

            dataBase.update(productsTableName,information,"$productsColumnId = '${prod.productId}'",null)
        }

        dataBase.close()
    }

    /**
     * addProduct
     *
     * Add a new product into the products database
     *
     * @param product the product to add into the database
     * @return error code of the database
     */
    fun addProduct(product:Product):DatabaseErrors{
        val dataBase:SQLiteDatabase=this.writableDatabase
        val information=ContentValues()

        information.put(productsColumnName,product.productName)
        information.put(productsColumnPrice,product.productPrice)
        information.put(productsColumnDescription,product.productDescription)
        information.put(productsColumnStock,product.productStock)
        information.put(productsColumnInStock,product.inStock)
        information.put(productsColumnsIsHidden,product.isHidden)

        val returnCode=dataBase.insert(productsTableName,null,information)
        dataBase.close()

        return if(returnCode==-1L) DatabaseErrors.UNKNOWN_ERROR else DatabaseErrors.SUCCESS
    }

    /**
     * getNotifications
     *
     * Pulls data from the Notifications database based on a specific user
     *
     * @param userId the user id of the user to get the notifications for
     * @param isAdmin is the current user an admin
     * @return arraylist of notifications pulled from the database
     */
    fun getNotifications(userId:Int,isAdmin:Boolean): ArrayList<Notifications> {
        val db: SQLiteDatabase = this.readableDatabase

        val selectQuery = if(isAdmin){
            "SELECT * FROM $notificationTableName WHERE $notificationTo LIKE '$userId' OR $notificationTo LIKE '0'"
        }else{
            "SELECT * FROM $notificationTableName WHERE $notificationTo LIKE '$userId' OR $notificationTo LIKE '1'"
        }

        val cursor: Cursor = db.rawQuery(selectQuery, null)

        val notifications = ArrayList<Notifications>()

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val title = cursor.getString(1)
            val description = cursor.getString(2)
            val toUser=cursor.getInt(3)
            val fromUser=cursor.getInt(4)
            val isOrder = cursor.getInt(5)>0
            val status = NotificationStatus.getNotifStatus(cursor.getInt(6))

            notifications.add(Notifications(id, title, description, toUser, fromUser, isOrder, status))
        }

        cursor.close()
        db.close()

        Collections.reverse(notifications)

        return notifications
    }

    /**
     * addNotification
     *
     * Adds a new notification to the database
     *
     * @param notif the notification to add to the database
     * @return whether the operation was successful or not
     */
    fun addNotification(notif:Notifications):DatabaseErrors {
        val db = this.writableDatabase
        val values = ContentValues()

        // Adding values to ContentValues
        values.put(notificationTitle, notif.title)
        values.put(notificationDescription, notif.description)
        values.put(notificationTo,notif.toUser)
        values.put(notificationFrom,notif.fromUser)
        values.put(notificationStatus, notif.status.value)
        values.put(notificationIsOrder, notif.isOrder)

        // Inserting the row into the Notifications table
        val returnCode = db.insert(notificationTableName, null, values)

        // Closing the database connection
        db.close()

        return if(returnCode==-1L) DatabaseErrors.UNKNOWN_ERROR else DatabaseErrors.SUCCESS
    }

    /**
     * updateNotification
     */
    fun updateNotification(notif:Notifications){
        val db = this.writableDatabase
        val values = ContentValues()

        // Adding values to ContentValues
        values.put(notificationStatus, notif.status.value)

        db.update(notificationTableName,values,"$notificationID = ${notif.id}",null)
        db.close()
    }
}