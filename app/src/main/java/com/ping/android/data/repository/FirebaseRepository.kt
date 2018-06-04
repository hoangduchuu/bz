package com.ping.android.data.repository

import com.google.firebase.database.FirebaseDatabase

abstract class FirebaseRepository {
    protected open val database = FirebaseDatabase.getInstance()


    companion object {
        const val CHILD_CONVERSATION: String = "conversations"
    }
}