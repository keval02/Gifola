package com.gifola.constans

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.gifola.BuildConfig
import java.io.Serializable


/**
 * Created by shagun on 02-01-2018.
 */
class SharedPreferenceHelper(val context: Context) {

    private val settings = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    private val editor = settings.edit()

    companion object {
        var isInvitedUser: Boolean = false
        val PREF_FILE_NAME = BuildConfig.APPLICATION_ID + ".pref"
    }

    /**
     * Set a string shared preference
     * @param key - Key to set shared preference
     * @param value - Value for the key
     */
    fun setString(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * Set a integer shared preference
     * @param key - Key to set shared preference
     * @param value - Value for the key
     */
    fun setInt(key: String, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * Set a integer shared preference
     * @param key - Key to set shared preference
     * @param value - Value for the key
     */
    @SuppressLint("ApplySharedPref")
    fun setObject(key: String, value: Serializable?) {
        val data = Gson().toJson(value)
        editor.putString(key, data)
        editor.commit()
    }

    /**
     * Set a Boolean shared preference
     * @param key - Key to set shared preference
     * @param value - Value for the key
     */
    fun setBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**
     * Get a string shared preference
     * @param key - Key to look up in shared preferences.
     * @param defValue - Default value to be returned if shared preference isn't found.
     * @return value - String containing value of the shared preference if found.
     */
    fun getString(key: String, defValue: String): String? {
        return settings.getString(key, defValue)
    }

    /**
     * Get a integer shared preference
     * @param key - Key to look up in shared preferences.
     * @param defValue - Default value to be returned if shared preference isn't found.
     * @return value - String containing value of the shared preference if found.
     */
    fun getInt(key: String, defValue: Int): Int {
        return settings.getInt(key, defValue)
    }

    /**
     * Get a boolean shared preference
     * @param key - Key to look up in shared preferences.
     * @param defValue - Default value to be returned if shared preference isn't found.
     * @return value - String containing value of the shared preference if found.
     */
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return settings.getBoolean(key, defValue)
    }

    fun <Y : Serializable> getObject(key: String, type: Class<Y>): Y? {
        val json = settings.getString(key, "")
        return if (json!!.isEmpty()) {
            type.newInstance()
        } else {
            try {
                Gson().fromJson(json, type)
            } catch (e: Exception) {
                type.newInstance()
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    fun clearAll() {
        editor.clear().apply()
    }
}