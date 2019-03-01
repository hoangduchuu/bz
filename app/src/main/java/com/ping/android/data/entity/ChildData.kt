package com.ping.android.data.entity

import durdinapps.rxfirebase2.RxFirebaseChildEvent

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

    constructor(data: T, type: RxFirebaseChildEvent.EventType) {
        this.data = data
        this.type = Type.from(type)
    }

    enum class Type {
        CHILD_ADDED, CHILD_CHANGED, CHILD_MOVED, CHILD_REMOVED;

        companion object {
            @JvmStatic
            fun from(type: RxFirebaseChildEvent.EventType): Type {
                return when(type) {
                    RxFirebaseChildEvent.EventType.ADDED -> CHILD_ADDED
                    RxFirebaseChildEvent.EventType.CHANGED -> CHILD_CHANGED
                    RxFirebaseChildEvent.EventType.REMOVED -> CHILD_REMOVED
                    RxFirebaseChildEvent.EventType.MOVED -> CHILD_MOVED
                }
            }
        }
    }
}
