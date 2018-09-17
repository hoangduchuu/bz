package com.ping.android.managers

import com.ping.android.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationManager {
    @Inject lateinit var userManager: UserManager

    fun checkUserUpdate(currentUser: User, user: User) {
        val conversationID = if (currentUser.key.compareTo(user.key) > 0) currentUser.key + user.key else user.key + currentUser.key

    }
}