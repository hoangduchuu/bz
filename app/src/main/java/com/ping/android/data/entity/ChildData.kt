package com.ping.android.data.entity

import com.bzzzchat.rxfirebase.database.ChildEvent

/**
 * Created by tuanluong on 1/28/18.
 */

class ChildData<T> {
    var data: T
    var type: ChildData.Type = Type.CHILD_ADDED

    constructor(data: T, type: ChildData.Type) {
        this.data = data
        this.type = type
    }

    constructor(data: T, type: ChildEvent.Type) {
        this.data = data
        this.type = Type.from(type)
    }

    enum class Type {
        CHILD_ADDED, CHILD_CHANGED, CHILD_MOVED, CHILD_REMOVED;

        companion object {
            @JvmStatic
            fun from(type: ChildEvent.Type): Type {
                return when(type) {
                    ChildEvent.Type.CHILD_ADDED -> CHILD_ADDED
                    ChildEvent.Type.CHILD_CHANGED -> CHILD_CHANGED
                    ChildEvent.Type.CHILD_REMOVED -> CHILD_REMOVED
                    ChildEvent.Type.CHILD_MOVED -> CHILD_MOVED
                }
            }
        }
    }
}
