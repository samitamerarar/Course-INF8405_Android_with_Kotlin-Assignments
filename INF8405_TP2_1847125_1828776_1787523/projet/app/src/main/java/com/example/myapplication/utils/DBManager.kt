package com.example.myapplication.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.myapplication.MainActivity
import com.example.myapplication.RecyclerViews.ModelDetails
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*


class DBManager(activity: MainActivity?) {
    private var mainActivity: MainActivity? = activity

    // Local Broadcast message handler (from AdapterScanResults)
    var mMessageReceiverAddFavorite: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val itemName = intent.getStringExtra("name")
            val itemMacAddr = intent.getStringExtra("macaddress")
            val itemDescription = intent.getStringExtra("description")

            addDeviceToFavorite(itemName!!, itemMacAddr!!, itemDescription!!)
        }
    }

    // Local Broadcast message handler (from AdapterScanFavorites)
    var mMessageReceiverRemoveFavorite: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val itemMacAddr = intent.getStringExtra("macaddress")

            removeDeviceFromFavorite(itemMacAddr!!)
        }
    }

    // Local Broadcast message handler (from AdapterScanHistory)
    var mMessageReceiverRemoveHistory: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val itemMacAddr = intent.getStringExtra("macaddress")

            removeDeviceFromHistory(itemMacAddr!!)
        }
    }

    /* Favorites DB */

    fun addDeviceToFavorite(itemName: String, itemMacAddr: String, itemDescription: String) {
        val sharedPreferences: SharedPreferences = mainActivity!!.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet(itemMacAddr, setOf(itemName, itemDescription))
        editor.apply()

        populateFavoritesRecyclerView()
    }

    fun removeDeviceFromFavorite(itemMacAddr: String) {
        val sharedPreferences: SharedPreferences = mainActivity!!.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(itemMacAddr)
        editor.apply()

        populateFavoritesRecyclerView()
    }

    fun populateFavoritesRecyclerView() {
        mainActivity!!.favoritesDevicesShown.clear()
        val sharedPreferences: SharedPreferences = mainActivity!!.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val keys: Map<String, *> = sharedPreferences.all
        for ((key, value) in keys) {
            if ((value as Set<String>).first()[0] == '•')
                mainActivity!!.favoritesDevicesShown.add( ModelDetails( value.last(), key, value.first(),null, getLatLong(value.first())))
            else
                mainActivity!!.favoritesDevicesShown.add(ModelDetails( value.first(), key, value.last(),null, getLatLong(value.last())))
        }
        mainActivity!!.recyclerView_favorites.adapter!!.notifyDataSetChanged()
    }

    fun clearFavoritesPrefs() {
        val sharedPreferences: SharedPreferences = mainActivity!!.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    /* History DB */

    fun addDeviceToHistory(itemName: String, itemMacAddr: String, itemDescription: String) {
        val sharedPreferences: SharedPreferences = mainActivity!!.getSharedPreferences("history", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet(itemMacAddr, setOf(itemName, itemDescription))
        editor.apply()

        populateScanHistoryRecyclerView()
    }

    fun removeDeviceFromHistory(itemMacAddr: String) {
        val sharedPreferences: SharedPreferences = mainActivity!!.getSharedPreferences("history", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(itemMacAddr)
        editor.apply()

        populateScanHistoryRecyclerView()
    }

    fun populateScanHistoryRecyclerView() {
        mainActivity!!.historyDevicesShown.clear()
        val sharedPreferences: SharedPreferences = mainActivity!!.getSharedPreferences("history", Context.MODE_PRIVATE)
        val keys: Map<String, *> = sharedPreferences.all
        for ((key, value) in keys) {
            if ((value as Set<String>).first()[0] == '•') {
                mainActivity!!.historyDevicesShown.add( ModelDetails( value.last(), key, value.first(),null, getLatLong(value.first())))
            }
            else
                mainActivity!!.historyDevicesShown.add(ModelDetails( value.first(), key, value.last(),null, getLatLong(value.last())))
        }
        mainActivity!!.recyclerView_history.adapter!!.notifyDataSetChanged()
    }

    fun clearScanHistoryPrefs() {
        val sharedPreferences: SharedPreferences = mainActivity!!.getSharedPreferences("history", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        populateScanHistoryRecyclerView()
    }

    private fun getLatLong(string: String): LatLng {
        val result = string
            .substringAfter("lat/lng: (")
            .substringBefore(')')
        val latlong =
            result.split(",").toTypedArray()
        val latitude = latlong[0].toDouble()
        val longitude = latlong[1].toDouble()
        return LatLng(latitude, longitude)
    }
}
