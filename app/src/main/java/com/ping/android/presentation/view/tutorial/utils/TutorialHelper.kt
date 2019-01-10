package com.ping.android.presentation.view.tutorial.utils;

import com.ping.android.utils.SharedPrefsHelper
import javax.inject.Inject

/**
 * Created by Huu Hoang on 10/01/2019
 */
class TutorialHelper @Inject constructor() {
    private val TUTORIAL_01_NEWCHAT = "TUTORIAL_01_NEWCHAT"

    fun isTutorial01NewChatIconClicked():Boolean{
        return SharedPrefsHelper.getInstance().get(TUTORIAL_01_NEWCHAT,false)
    }

    fun markTutorial01NewChatClicked(){
        SharedPrefsHelper.getInstance().save(TUTORIAL_01_NEWCHAT,true)
    }

}