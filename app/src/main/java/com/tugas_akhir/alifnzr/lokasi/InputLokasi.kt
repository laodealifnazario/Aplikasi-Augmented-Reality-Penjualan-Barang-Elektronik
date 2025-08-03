package com.tugas_akhir.alifnzr.lokasi

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.ActivityInputLokasiBinding
import java.util.Locale

class InputLokasi : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityInputLokasiBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var selectedAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputLokasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mendapatkan fragment peta dan mempersiapkannya
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this) // Memanggil onMapReadyCallback ketika peta siap

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        // tombol pencarian lokasi
        binding.searchButton.setOnClickListener {
            val address = binding.addressInput.text.toString()
            if (address.isNotEmpty()) {
                searchAddress(address)
            } else {
                Toast.makeText(this, "Masukkan alamat terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol untuk mendapatkan lokasi pengguna
        binding.gpsButton.setOnClickListener {
            getLokasiSaya()
        }

        // Tombol untuk mengubah menjadi peta satelit atau default
        binding.satelliteButton.setOnClickListener {
            toggleMapType()
        }

        // tombol input alamat
        binding.iconAddress.setOnClickListener {
            val intent = Intent()
            intent.putExtra("latitude", selectedLatitude)
            intent.putExtra("longtitude", selectedLongitude)
            intent.putExtra("alamat", selectedAddress)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener { latLng ->
            // Ketika peta diklik, tambahkan marker dan tampilkan alamat di EditText
            addMarkerAtLocation(latLng)
        }
        // Menambahkan listener ketika marker diklik
        mMap.setOnMarkerClickListener { marker ->
            // Dapatkan alamat dari lokasi marker
            val latLng = marker.position
            getAddressFromLatLng(latLng)
            false // Return false to allow the default behavior of clicking a marker (showing the info window)
        }
    }

    private fun addMarkerAtLocation(latLng: LatLng) {
        mMap.clear() // Bersihkan marker sebelumnya
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Selected Location")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        // Simpan koordinat
        selectedLatitude = latLng.latitude
        selectedLongitude = latLng.longitude

        // Ambil alamat dari koordinat dan update EditText
        getAddressFromLatLng(latLng)
    }

    private fun getAddressFromLatLng(latLng: LatLng) {
        try {
            // Gunakan Geocoder untuk mendapatkan alamat dari latitude dan longitude
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0].getAddressLine(0)
                selectedAddress = address
                binding.addressInput.setText(address) // Menampilkan alamat di EditText
            } else {
                binding.addressInput.setText("Alamat Tidak Ditemukan")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Terjadi kesalahan dalam mendapatkan alamat", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLokasiSaya() {
        // Mengecek apakah izin sudah diberikan
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Mendapatkan latitude dan longitude lokasi pengguna
                        selectedLatitude = location.latitude
                        selectedLongitude = location.longitude
                        // Menambahkan marker pada lokasi pengguna
                        val userLocation = LatLng(selectedLatitude!!, selectedLongitude!!)

                        // Memindahkan kamera ke lokasi pengguna
                        mMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                        // Mendapatkan alamat dari lokasi
                        try {
                            val addresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1)
                            if (addresses != null && addresses.isNotEmpty()) {
                                selectedAddress = addresses[0].getAddressLine(0)
                                binding.addressInput.setText(selectedAddress) // Menampilkan alamat di EditText
                            } else {
                                binding.addressInput.setText("Alamat Tidak Ditemukan")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        } else {
            // Jika izin tidak ada, minta izin terlebih dahulu
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun searchAddress(address: String) {
        try {
            // Melakukan geocoding dengan Geocoder
            val addresses = geocoder.getFromLocationName(address, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                // Mendapatkan latitude dan longitude dari alamat yang ditemukan
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)

                // Menambahkan marker di lokasi yang ditemukan
                mMap.clear()  // Menghapus marker sebelumnya
                mMap.addMarker(MarkerOptions().position(latLng).title(address))

                // Memindahkan kamera ke lokasi yang ditemukan
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            } else {
                Toast.makeText(this, "Alamat tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Terjadi kesalahan dalam pencarian alamat", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk mengubah tipe peta menjadi satelit atau normal
    private fun toggleMapType() {
        // Mengecek apakah tipe peta saat ini adalah satelit
        if (mMap.mapType == GoogleMap.MAP_TYPE_SATELLITE) {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            binding.satelliteButton.text = "Switch to Satellite"
        } else {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            binding.satelliteButton.text = "Switch to Normal"
        }
    }
}
