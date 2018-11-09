package com.ping.android.presentation.view.custom.newSticker

import android.icu.util.Calendar
import com.ping.android.data.db.AppDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

@Table(name = "items", database = AppDatabase::class)
class Item : BaseModel() {

    @PrimaryKey()
    @Column(name = "path")
    var path = ""

    @Column(name = "time")
    var time: Long = 0


}