package com.ping.android.data.mappers

import com.google.firebase.database.DataSnapshot
import com.ping.android.data.entity.CallEntity
import javax.inject.Inject

class CallEntityMapper @Inject constructor(){
    fun transform(dataSnapshot: DataSnapshot): CallEntity {
        val call = dataSnapshot.getValue(CallEntity::class.java)!!
        call.key = dataSnapshot.key!!
        return call
    }
}