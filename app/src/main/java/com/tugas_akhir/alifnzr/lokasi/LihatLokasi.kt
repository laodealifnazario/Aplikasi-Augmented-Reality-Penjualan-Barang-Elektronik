package com.tugas_akhir.alifnzr.lokasi

import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.ActivityLihatLokasiBinding
import java.util.Locale

class LihatLokasi : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding : ActivityLihatLokasiBinding
    private var id_pesanan = ""
    private var pembeli: LatLng = LatLng(0.0, 0.0)
    private var penjual: LatLng = LatLng(0.0, 0.0)
    private lateinit var geoApiContext: GeoApiContext
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLihatLokasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id_pesanan = intent.getStringExtra("id_pesanan").toString()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Menambahkan event listener pada tombol untuk mengubah tampilan peta menjadi satelit
        binding.satelliteButton.setOnClickListener {
            toggleMapType()
        }

        geoApiContext = GeoApiContext.Builder()
            .apiKey("AIzaSyDqO-ih9UnmL6QgVfvB2CppKCPONb_3_Bw") // Ganti dengan API Key Anda
            .build()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        geocoder = Geocoder(this, Locale.getDefault())

        loadLokasi()

    }

    private fun updateMapMarkers() {
        mMap.clear()  // Membersihkan peta terlebih dahulu jika perlu

        // Menambahkan marker untuk Pembeli dan Penjual
        val pembeliMarker = mMap.addMarker(MarkerOptions().position(pembeli).title("Pembeli"))
        val penjualMarker = mMap.addMarker(MarkerOptions().position(penjual).title("Penjual"))

        // Memastikan InfoWindow muncul secara otomatis
        pembeliMarker?.showInfoWindow()
        penjualMarker?.showInfoWindow()

        // Memindahkan kamera ke pembeli
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pembeli, 7f))
    }

    private fun loadLokasi() {
        val ref1 = FirebaseDatabase.getInstance().getReference("Pesanan")
        ref1.child(id_pesanan)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val latitudePenjual = snapshot.child("latitude_penjual").value.toString()
                    val longtitudePenjual = snapshot.child("longtitude_penjual").value.toString()
                    val latitudePembeli = snapshot.child("latitude_pembeli").value.toString()
                    val longtitudePembeli = snapshot.child("longtitude_pembeli").value.toString()

                    // Pastikan data valid sebelum digunakan
                    if (latitudePenjual.isNotEmpty() && longtitudePenjual.isNotEmpty() && latitudePembeli.isNotEmpty() && longtitudePembeli.isNotEmpty()) {
                        val latitudePenjualDouble = latitudePenjual.toDoubleOrNull() ?: 0.0
                        val longtitudePenjualDouble = longtitudePenjual.toDoubleOrNull() ?: 0.0
                        val latitudePembeliDouble = latitudePembeli.toDoubleOrNull() ?: 0.0
                        val longtitudePembeliDouble = longtitudePembeli.toDoubleOrNull() ?: 0.0

                        pembeli = LatLng(latitudePembeliDouble, longtitudePembeliDouble)
                        penjual = LatLng(latitudePenjualDouble, longtitudePenjualDouble)

                        // Update marker di peta setelah data dimuat
                        updateMapMarkers()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Tangani error jika ada
                }
            })
    }

    private fun gambarRute(origin: LatLng, destination: LatLng) {
        val request = DirectionsApi.newRequest(geoApiContext)
            .mode(TravelMode.DRIVING)  // Gunakan DRIVING, bukan DRIVE
            .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
            .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))

        request.setCallback(object : com.google.maps.PendingResult.Callback<com.google.maps.model.DirectionsResult> {
            override fun onResult(result: com.google.maps.model.DirectionsResult?) {
                if (result != null) {
                    for (route in result.routes) {
                        val polylineOptions = PolylineOptions().apply {
                            addAll(convertToLatLng(route.overviewPolyline.decodePath()))
                            width(8f)
                            color(android.graphics.Color.BLUE)
                        }
                        mMap.addPolyline(polylineOptions)

                        val bounds = LatLngBounds.Builder()
                        route.legs.forEach { leg ->
                            bounds.include(LatLng(leg.startLocation.lat, leg.startLocation.lng))
                            bounds.include(LatLng(leg.endLocation.lat, leg.endLocation.lng))
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                    }
                }
            }

            override fun onFailure(e: Throwable?) {
                e?.printStackTrace()
            }
        })
    }

    private fun convertToLatLng(path: List<com.google.maps.model.LatLng>): List<LatLng> {
        val latLngList = mutableListOf<LatLng>()
        for (point in path) {
            latLngList.add(LatLng(point.lat, point.lng))
        }
        return latLngList
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Atur kamera awal
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pembeli, 7f))

        // Menambahkan marker untuk Pembeli dan Penjual
        mMap.addMarker(MarkerOptions().position(pembeli).title("Pembeli"))
        mMap.addMarker(MarkerOptions().position(penjual).title("Penjual"))

        // Tampilkan rute dari Pembeli ke Penjual
        gambarRute(pembeli, penjual)
    }

    private fun toggleMapType() {
        // Mengecek apakah tipe peta saat ini adalah satelit
        if (mMap.mapType == GoogleMap.MAP_TYPE_SATELLITE) {
            // Jika iya, ubah ke tipe normal dan ganti teks tombol
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            binding.satelliteButton.text = "Switch to Satellite"
        } else {
            // Jika tidak, ubah ke tipe satelit dan ganti teks tombol
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            binding.satelliteButton.text = "Switch to Normal"
        }
    }

}