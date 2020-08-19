package com.example.myapplication.RecyclerViews

import com.google.android.gms.maps.model.LatLng

data class ModelDetails(val name: String, val macaddress: String, val description: String, val distance: Double?, val position: LatLng?)
