package com.ping.android.exeption


/*
 * Created by Huu Hoang
 *
 * The use-case can throw an instant of this exception in case there are something wrong
  * while process the business logic.
  *
  * @code the error code defined by the use-case. The presenters that call such use-case
  * must handle error code and show appropriate message to user
 */
class BzzzExeption(val code: Int, override val message: String? = "") : Throwable(message) {
    companion object {
        val AppInitialize_NoInternet = 1001
        val Unknown = 1
        val Server_Error = 2
        val No_Configuration = 3
        val firebaseNetWorkExeption = 4
        val firebaseQueryExeption = 5;
    }
}