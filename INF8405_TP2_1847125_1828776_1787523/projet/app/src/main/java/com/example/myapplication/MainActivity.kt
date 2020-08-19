package com.example.myapplication

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.RecyclerViews.AdapterScanFavorites
import com.example.myapplication.RecyclerViews.AdapterScanHistory
import com.example.myapplication.RecyclerViews.AdapterScanResults
import com.example.myapplication.RecyclerViews.ModelDetails
import com.example.myapplication.utils.BTInfoAnalyser
import com.example.myapplication.utils.DBManager
import com.example.myapplication.utils.calculateDistance
import com.example.myapplication.utils.newLocationOnMap
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothClassicService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService.OnBluetoothScanCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.adapter_details.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    private lateinit var location: SimpleLocation
    private var currentLat: Double = 45.508888 // Montreal location
    private var currentLong: Double = -73.561668

    private val devicesList = HashMap<String, BluetoothDevice>()
    val devicesShown = ArrayList<ModelDetails>()
    val favoritesDevicesShown = ArrayList<ModelDetails>()
    val historyDevicesShown = ArrayList<ModelDetails>()

    private lateinit var service: BluetoothService
    private val bluetoothInfoAnalyzer = BTInfoAnalyser()

    private var dBManager: DBManager = DBManager(this)

    private fun buildAlertMessageNoGps(mainActivity: MainActivity) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(mainActivity)
        builder.setMessage("GPS is required to run the application, enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
                mainActivity.finishAffinity()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    override fun onResume() {
        super.onResume()
        setupEverything()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
        setupEverything()
    }

    private fun setupEverything() {
        // Enable Bluetooth
        val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter.isEnabled) {

            // Enable GPS
            val manager: LocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps(this);
            } else {
                // tabs en haut pour switch de listes
                tab_scan_results.setOnClickListener { toggleListView("results") }
                tab_scan_favorites.setOnClickListener { toggleListView("favorites") }
                tab_scan_history.setOnClickListener { toggleListView("history") }
                button_clear_history.setOnClickListener { dBManager.clearScanHistoryPrefs() }
                toggleListView("results")

                // boutons d'actions
                button_scan_again.setOnClickListener { scanAgain() }
                button_swap_theme.setOnClickListener {
                    darkMode = !darkMode
                    toggleDarkLightTheme()
                }

                // bluetooth scan
                val config = configurateBluetoothScanner()
                BluetoothService.init(config)
                service = BluetoothService.getDefaultInstance()
                initBluetoothScan()

                // recycler view
                val customAdapter = AdapterScanResults(devicesShown)
                recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                recyclerView.adapter = customAdapter

                val adapterScanFavorites = AdapterScanFavorites(favoritesDevicesShown)
                recyclerView_favorites.layoutManager =
                    LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                recyclerView_favorites.adapter = adapterScanFavorites

                val adapterScanHistory = AdapterScanHistory(historyDevicesShown)
                recyclerView_history.layoutManager =
                    LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                recyclerView_history.adapter = adapterScanHistory

                // localisation
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager;
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    currentLat = location.latitude;
                    currentLong = location.longitude;
                } else {
                    val firstLocation = SimpleLocation(this)
                    currentLat = firstLocation.latitude
                    currentLong = firstLocation.longitude
                }

                location = SimpleLocation(this, false, false, 1000, true)
                location.beginUpdates()

                // nouvelle localisation mise a jour
                location.setListener {
                    // Toast.makeText(applicationContext, "position changed", Toast.LENGTH_SHORT).show()
                    currentLat = location.latitude
                    currentLong = location.longitude
                }

                // google maps
                mapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync {
                    googleMap = it
                    googleMap.isMyLocationEnabled = true

                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                currentLat,
                                currentLong
                            ), 20f
                        )
                    )
                }

                // broadcast local du adapteur de la liste
                LocalBroadcastManager.getInstance(this).registerReceiver(
                    dBManager.mMessageReceiverAddFavorite,
                    IntentFilter("put-favorite")
                )
                LocalBroadcastManager.getInstance(this).registerReceiver(
                    dBManager.mMessageReceiverRemoveFavorite,
                    IntentFilter("remove-favorite")
                )
                LocalBroadcastManager.getInstance(this).registerReceiver(
                    dBManager.mMessageReceiverRemoveHistory,
                    IntentFilter("remove-history")
                )
                dBManager.populateFavoritesRecyclerView()
                dBManager.populateScanHistoryRecyclerView()
            }
        } else {
            mBluetoothAdapter.enable()
            Toast.makeText(applicationContext, "Bluetooth enabled!", Toast.LENGTH_SHORT).show()
            onCreate(Bundle())
        }
    }

    private fun configurateBluetoothScanner(): BluetoothConfiguration {
        val config = BluetoothConfiguration()
        config.context = applicationContext
        config.bluetoothServiceClass = BluetoothClassicService::class.java
        config.bufferSize = 1024
        config.characterDelimiter = '\n'
        config.deviceName = "BluetoothScanner-APP"
        config.callListenersInMainThread = true
        config.uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb") // Required
        return config
    }

    private fun displayDeviceInformations(
        device: BluetoothDevice,
        name: String,
        distance: Double,
        position: LatLng
    ) {
        var deviceDescription =
            "• MAC address: " + "\n" + device.address +
                    "\n--------------------------------\n" +
                    "• Device Class: " + "\n" + bluetoothInfoAnalyzer.bluetoothDeviceClass(
                device.bluetoothClass.toString().toInt(16)
            ) +
                    "\n--------------------------------\n" +
                    "• Bond State: " + "\n" + bluetoothInfoAnalyzer.deviceBondState(device.bondState) +
                    "\n--------------------------------\n" +
                    "• Bluetooth Type: " + "\n" + bluetoothInfoAnalyzer.deviceType(device.type) +
                    "\n--------------------------------\n" +
                    "• UUID: " + "\n"
        if (device.uuids == null) deviceDescription += "Unknown UUID" else deviceDescription += device.uuids
        deviceDescription += "\n--------------------------------\n ~~ was at approx. $distance meters away ~~"
        deviceDescription += "\n${position}"

        devicesShown.add(ModelDetails(name, device.address, deviceDescription, distance, position))
        recyclerView.adapter!!.notifyDataSetChanged()
        dBManager.addDeviceToHistory(name, device.address, deviceDescription)
        recyclerView_history.adapter!!.notifyDataSetChanged()
    }

    // ccan bluetooth device and show them on map and in results recyclerview
    private fun initBluetoothScan() {
        service.setOnScanCallback(object : OnBluetoothScanCallback {
            override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
                if (!devicesList.containsKey(device.address)) {
                    var name = "\"no name device\""
                    if (device.name != null) name = device.name
                    devicesList.put(device.address, device)
                    val distance = calculateDistance(rssi.toDouble())
                    val newLatLng = newLocationOnMap(
                        currentLong,
                        currentLat,
                        distance,
                        ((0..359).shuffled().first()).toDouble()
                    )
                    displayDeviceInformations(device, name, distance, newLatLng)
                    mapFragment.getMapAsync {
                        googleMap = it
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(newLatLng).title(device.address)
                                .snippet("$name at approx. $distance meters away")
                        )
                        googleMap.addCircle(
                            CircleOptions()
                                .center(LatLng(location.latitude, location.longitude))
                                .radius(distance)
                                .strokeColor(Color.BLACK)
                                .strokePattern(listOf(Dash(10f), Gap(10f)))
                                .strokeWidth(2f)
                        )

                        googleMap.setOnInfoWindowClickListener { m ->
                            for (i in 0 until devicesShown.size) {
                                if (m.title == devicesShown[i].macaddress) {
                                    recyclerView.findViewHolderForAdapterPosition(i)!!.itemView.performClick()
                                }
                            }
                        }
                    }
                }
            }

            override fun onStartScan() {
                Toast.makeText(applicationContext, "scanning started", Toast.LENGTH_SHORT).show()
                button_scan_again.isEnabled = false
                button_scan_again.text = "Scanning..."
            }

            override fun onStopScan() {
                Toast.makeText(applicationContext, "scanning completed", Toast.LENGTH_SHORT).show()
                button_scan_again.isEnabled = true
                button_scan_again.text = "Scan Again"
            }
        })
        service.startScan()
    }

    // perform bluetooth scan and repopulate the recyclerview
    private fun scanAgain() {
        mapFragment.getMapAsync {
            googleMap = it
            googleMap.clear()
        }
        devicesList.clear()
        devicesShown.clear()
        recyclerView.adapter!!.notifyDataSetChanged()
        service.startScan()
    }

    // toggle between recyclerviews (tabs on top)
    private fun toggleListView(list: String) {
        when (list) {
            "results" -> {
                recyclerView.visibility = View.VISIBLE
                recyclerView_favorites.visibility = View.GONE
                recyclerView_history.visibility = View.GONE
                button_clear_history.visibility = View.GONE
                tab_scan_results.setBackgroundColor(Color.parseColor("#5C6773"))
                tab_scan_favorites.setBackgroundColor(Color.parseColor("#A1AAB3"))
                tab_scan_history.setBackgroundColor(Color.parseColor("#A1AAB3"))
            }
            "favorites" -> {
                recyclerView.visibility = View.GONE
                recyclerView_favorites.visibility = View.VISIBLE
                recyclerView_history.visibility = View.GONE
                button_clear_history.visibility = View.GONE
                tab_scan_results.setBackgroundColor(Color.parseColor("#A1AAB3"))
                tab_scan_favorites.setBackgroundColor(Color.parseColor("#5C6773"))
                tab_scan_history.setBackgroundColor(Color.parseColor("#A1AAB3"))
            }
            "history" -> {
                recyclerView.visibility = View.GONE
                recyclerView_favorites.visibility = View.GONE
                recyclerView_history.visibility = View.VISIBLE
                button_clear_history.visibility = View.VISIBLE
                tab_scan_results.setBackgroundColor(Color.parseColor("#A1AAB3"))
                tab_scan_favorites.setBackgroundColor(Color.parseColor("#A1AAB3"))
                tab_scan_history.setBackgroundColor(Color.parseColor("#5C6773"))
            }
        }
    }

    /* Dark mode stuffs */

    private fun toggleDarkLightTheme() {
        if (darkMode) {
            recyclerView_layout.setBackgroundColor(Color.parseColor("#121212"))
            divider.setBackgroundColor(Color.parseColor("#03dac6"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window: Window = window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.BLACK
                getWindow().navigationBarColor = Color.BLACK
            }
        }
        else {
            recyclerView_layout.setBackgroundColor(Color.parseColor("#FFFFFF"))
            divider.setBackgroundColor(Color.parseColor("#008577"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window: Window = window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.parseColor("#008577")
                getWindow().navigationBarColor = Color.parseColor("#ffffff")
            }
        }
        recyclerView.adapter!!.notifyDataSetChanged()
        recyclerView_history.adapter!!.notifyDataSetChanged()
        recyclerView_favorites.adapter!!.notifyDataSetChanged()
    }

    companion object {
        var darkMode = false
    }

}
