package com.example.mobilecafeapplication

/**
 * DatabaseErrors
 *
 * Enum for the possible errors when sending/receiving data from the database
 *
 * @param value the integer value of the enum you are after
 */
enum class DatabaseErrors(val value:Int) {
    EMAIL(1),
    SUCCESS(2),
    UNKNOWN_ERROR(3);

    companion object{
        /**
         * dbError
         *
         * Cycle through all the enums until the one matching the integer value provided
         * is found.
         *
         * @param value the integer value of the enum you are after
         */
        fun dbError(value:Int)=entries.first{it.value==value}
    }
}