package com.ping.android.data.repository;

import com.ping.android.utils.SharedPrefsHelper
import javax.inject.Inject

/**
 * Created by Huu Hoang on 10/01/2019
 * Responsibilities: Check and save state of button tutorial click event;
 */

class TutorialHelper @Inject constructor() {
    private val TUTORIAL_01_NEWCHAT = "TUTORIAL_01_NEWCHAT"
    private val TUTORIAL_02_NAVIGATE_CONTACT = "TUTORIAL_02_NAVIGATE_CONTACT"
    private val TUTORIAL_03_ADD_FRIEND = "TUTORIAL_03_ADD_FRIEND"
    private val TUTORIAL_04_NAVIGATE_PROFILE = "TUTORIAL_04_NAVIGATE_PROFILE"
    private val TUTORIAL_05_TRANPHABET = "TUTORIAL_05_TRANPHABET"
    private val TUTORIAL_06_AVATAR = "TUTORIAL_06_AVATAR"
    private val TUTORIAL_07_CHATNAME = "TUTORIAL_07_CHATNAME"





    /**
     * new chat
     */
    fun isTutorial01NewChatIconClicked():Boolean{
        return SharedPrefsHelper.getInstance().get(TUTORIAL_01_NEWCHAT,false)
    }

    fun markTutorial01NewChatClicked(){
        SharedPrefsHelper.getInstance().save(TUTORIAL_01_NEWCHAT,true)
    }


    /**
     * contact navigation
     */

    fun isTutorial02ContactNavigationCLicked():Boolean{
        return SharedPrefsHelper.getInstance().get(TUTORIAL_02_NAVIGATE_CONTACT,false)
    }

    fun markTutorial02ContactNavigationCLicked(){
        SharedPrefsHelper.getInstance().save(TUTORIAL_02_NAVIGATE_CONTACT,true)
    }




    /**
     * add friend button
     */

    fun isTutorial03AddFriendCLicked():Boolean{
        return SharedPrefsHelper.getInstance().get(TUTORIAL_03_ADD_FRIEND,false)
    }

    fun markTutorial03AddFriendCLicked(){
        SharedPrefsHelper.getInstance().save(TUTORIAL_03_ADD_FRIEND,true)
    }


    /**
     * profile navigation
     */

    fun isTutorial04ProfileClicked():Boolean{
        return SharedPrefsHelper.getInstance().get(TUTORIAL_04_NAVIGATE_PROFILE,false)
    }

    fun marktutorial04ProfileClicked(){
        SharedPrefsHelper.getInstance().save(TUTORIAL_04_NAVIGATE_PROFILE,true)
    }


    /**
     * profile navigation
     */

    fun isTutorial05TranPhabetClicked():Boolean{
        return SharedPrefsHelper.getInstance().get(TUTORIAL_05_TRANPHABET,false)
    }

    fun markTutorial05TranPhabetClicked(){
        SharedPrefsHelper.getInstance().save(TUTORIAL_05_TRANPHABET,true)
    }



    /**
     * profile navigation
     */

    fun isTutorial06AvatarClicked():Boolean{
        return SharedPrefsHelper.getInstance().get(TUTORIAL_06_AVATAR,false)
    }

    fun markTutorial06AvatarClicked(){
        SharedPrefsHelper.getInstance().save(TUTORIAL_06_AVATAR,true)
    }



    /**
     * profile navigation
     */

    fun isTutorial07ChatNameClicked():Boolean{
        return SharedPrefsHelper.getInstance().get(TUTORIAL_07_CHATNAME,false)
    }

    fun markTutorial07ChatNameClicked(){
        SharedPrefsHelper.getInstance().save(TUTORIAL_07_CHATNAME,true)
    }


}