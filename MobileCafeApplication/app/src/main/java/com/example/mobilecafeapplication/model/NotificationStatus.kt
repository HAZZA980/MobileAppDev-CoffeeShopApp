package com.example.mobilecafeapplication.model

enum class NotificationStatus(val value:Int) {
    NOT_ACCEPTED(0),
    IS_ACCEPTED(1),
    READY_TO_COLLECT(2),
    COLLECTED(3),
    WAITING_FOR_STATE(4),
    NOT_ORDER(5);

    companion object{
        fun getNotifStatus(value:Int)=entries.first{it.value==value}
    }
}
