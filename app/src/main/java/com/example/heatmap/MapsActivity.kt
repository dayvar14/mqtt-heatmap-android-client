package com.example.heatmap

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    val colors = intArrayOf(
        Color.rgb(102, 225, 0),  // green
        Color.rgb(255, 0, 0) // red
    )

    val startPoints = floatArrayOf(
        1f, 20f
    )

    val gradient = Gradient(colors, startPoints)
    var hProvider: HeatmapTileProvider? = null
    var hOverlay: TileOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        this.googleMap = googleMap

        //Map preferences
        //The box mad by NE coordinates and SW coordinates. Bounds prevents user from moving out of view
        val bounds = LatLngBounds(
            LatLng(41.975612, -87.720767),LatLng(41.982986, -87.716177)
        )

        //Blocks out a area with a grey polygon
        val greyedArea = mutableListOf(LatLng(41.990053, -87.711413), LatLng(41.990053, -87.723294),
            LatLng(41.968519, -87.723294), LatLng(41.968519, -87.711413))

        //This puts a hole in the polygon so we can see the school
        val hole = mutableListOf(LatLng(41.982986,-87.716177), LatLng(41.982933,-87.720882),
            LatLng(41.977495, -87.720882), LatLng(41.977495, -87.718332),
            LatLng(41.975695, -87.718332), LatLng(41.975697, -87.716177)
        )



        //Moves Camera to a point
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(bounds.center))

        //Puts a bounds in the map
        googleMap.setLatLngBoundsForCameraTarget(bounds)

        googleMap.setPadding(200,200,200,200)

        //The minimum zoom distance
        googleMap.setMinZoomPreference(17f)
        //The maximum zoom distance
        googleMap.setMaxZoomPreference(19f)

        //Adds polygon to map
        val polygon: Polygon = googleMap.addPolygon(
            PolygonOptions()
                .addAll(greyedArea)
                .strokeColor(Color.LTGRAY)
                .fillColor(Color.LTGRAY)
                .addHole(hole)
                .geodesic(true)
                .strokeJointType(JointType.ROUND)
        )

        val mqttConnector = MqttConnector(this)

        Coordinates.coordinates.observe( this, Observer {
            addHeatmap()
        })
        //MqttClient.connect(this)
        //receiveMessages()
    }

    fun addHeatmap() {

        print("added coordinates to heatmap")
        //Refreshes the Overlay

        //Removes coordinates from map
        if(hOverlay != null)
            hOverlay!!.remove()

        val coordinates = Coordinates.coordinates.value
        //If no coordinates exist then do nothing
        if(coordinates == null || coordinates.size  == 0)
            return

        //Make heatmap from coordinates
        hProvider = HeatmapTileProvider.Builder().gradient(gradient).weightedData(Coordinates.coordinates.value).build()
        hOverlay = googleMap.addTileOverlay(TileOverlayOptions().tileProvider(hProvider))
    }




}
