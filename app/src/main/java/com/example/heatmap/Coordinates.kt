package com.example.heatmap

import androidx.lifecycle.MutableLiveData
import com.google.maps.android.heatmaps.WeightedLatLng

object Coordinates{
    var coordinates = MutableLiveData<List<WeightedLatLng>>()
}