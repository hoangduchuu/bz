package com.ping.android.data.mappers;

import com.ping.android.model.Conversation
import com.ping.android.model.User
import javax.inject.Inject

/**
 * Created by Huu Hoang on 20/12/2018
 */
class UserMapper @Inject constructor() {

    /**
     * @return nick name, if available in group
     * @fullName instead if nick name not available
     */
    fun getUserDisPlay(user: User, conversation: Conversation): String? {
        val map = conversation.nickNames
        if (conversation.nickNames != null && map.containsKey(user.key) && map.get(user.key)!!.isNotEmpty()){
            return map.get(user.key)
        }
        return "${user.firstName } ${user.lastName}"

    }
}