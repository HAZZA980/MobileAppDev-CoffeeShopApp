package com.example.mobilecafeapplication.model

/**
 * Notifications TODO
 */
class Notifications(
    val id: Int,
    val title: String,
    val description: String,
    val toUser:Int,
    val fromUser:Int,
    val isOrder: Boolean,
    var status: NotificationStatus
) {}
