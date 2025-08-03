package com.tugas_akhir.alifnzr

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tugas_akhir.alifnzr.databinding.ActivityKonfigurasiPembayaranBinding
class KonfigurasiPembayaran : AppCompatActivity() {

    private lateinit var binding: ActivityKonfigurasiPembayaranBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKonfigurasiPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("config/biaya")

        // Menonaktifkan EditText di awal (default mode view-only)
        setEditTextsEnabled(false)

        // Menyembunyikan tombol Simpan di awal, hanya tombol Edit yang terlihat
        binding.btnSimpan.visibility = Button.GONE

        // Mengambil data dari Firebase
        getDataFromFirebase()

        // Tombol Edit: Mengaktifkan EditText untuk bisa diubah dan menampilkan tombol Simpan
        binding.btnEdit.setOnClickListener {
            // Aktifkan EditText untuk bisa diedit
            setEditTextsEnabled(true)

            // Ubah warna border saat sedang diedit
            binding.labelBiayaLayananSistem.setBoxBackgroundColor(ContextCompat.getColor(this, R.color.color_editing))
            binding.labelBiayaPPN.setBoxBackgroundColor(ContextCompat.getColor(this, R.color.color_editing))
            binding.labelNoRekeningBCA.setBoxBackgroundColor(ContextCompat.getColor(this, R.color.color_editing))
            binding.labelNoRekeningBRI.setBoxBackgroundColor(ContextCompat.getColor(this, R.color.color_editing))
            binding.labelNoRekeningBNI.setBoxBackgroundColor(ContextCompat.getColor(this, R.color.color_editing))
            binding.labelNoRekeningMandiri.setBoxBackgroundColor(ContextCompat.getColor(this, R.color.color_editing))

            // Sembunyikan tombol Edit dan tampilkan tombol Simpan
            binding.btnEdit.visibility = Button.GONE
            binding.btnSimpan.visibility = Button.VISIBLE
        }

        binding.btnSimpan.setOnClickListener {
            try {
                // Mengambil nilai dari EditText dan mengonversi ke Int
                val biayaLayanan = binding.etBiayaLayanan.text.toString().toIntOrNull() ?: 0
                val biayaPPN = binding.etBiayaPpn.text.toString().toIntOrNull() ?: 0
                val noRekeningBCA = binding.etNoRekeningBca.text.toString()
                val noRekeningMandiri = binding.etNoRekeningMandiri.text.toString()
                val noRekeningBRI = binding.etNoRekeningBri.text.toString()
                val noRekeningBNI = binding.etNoRekeningBni.text.toString()

                // Membuat objek data yang akan disimpan
                val configData = KonfigurasiData(biayaLayanan, biayaPPN, noRekeningBCA, noRekeningMandiri, noRekeningBRI, noRekeningBNI)

                // Menyimpan data ke Firebase
                saveDataToFirebase(configData)

                // Setelah simpan, matikan EditText lagi (kembali ke mode view-only)
                setEditTextsEnabled(false)

                // Kembalikan warna border ke putih setelah tombol Simpan
                binding.labelBiayaLayananSistem.setBoxBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                binding.labelBiayaPPN.setBoxBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                binding.labelNoRekeningBCA.setBoxBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                binding.labelNoRekeningBRI.setBoxBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                binding.labelNoRekeningBNI.setBoxBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                binding.labelNoRekeningMandiri.setBoxBackgroundColor(ContextCompat.getColor(this, android.R.color.white))

                // Sembunyikan tombol Simpan dan tampilkan tombol Edit kembali
                binding.btnSimpan.visibility = Button.GONE
                binding.btnEdit.visibility = Button.VISIBLE
            } catch (e: Exception) {
                // Tangani error jika terjadi kesalahan saat mengambil input
                Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

    }

    // Fungsi untuk mengaktifkan atau menonaktifkan EditText
    private fun setEditTextsEnabled(enabled: Boolean) {
        binding.etBiayaLayanan.isEnabled = enabled
        binding.etBiayaPpn.isEnabled = enabled
        binding.etNoRekeningBca.isEnabled = enabled
        binding.etNoRekeningMandiri.isEnabled = enabled
        binding.etNoRekeningBri.isEnabled = enabled
        binding.etNoRekeningBni.isEnabled = enabled
    }

    // Fungsi untuk mengambil data dari Firebase
    private fun getDataFromFirebase() {
        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val configData = snapshot.getValue(KonfigurasiData::class.java)
                configData?.let {
                    // Mengisi EditText dengan data dari Firebase
                    binding.etBiayaLayanan.setText(it.biayaLayananSistem.toString())  // Konversi Int ke String
                    binding.etBiayaPpn.setText(it.biayaPPN.toString())  // Konversi Int ke String
                    binding.etNoRekeningBca.setText(it.noRekeningBCA ?: "") // Menggunakan "" jika null
                    binding.etNoRekeningMandiri.setText(it.noRekeningMandiri ?: "") // Menggunakan "" jika null
                    binding.etNoRekeningBri.setText(it.noRekeningBRI ?: "") // Menggunakan "" jika null
                    binding.etNoRekeningBni.setText(it.noRekeningBNI ?: "") // Menggunakan "" jika null
                }
            }
        }
    }

    // Fungsi untuk menyimpan data ke Firebase
    private fun saveDataToFirebase(configData: KonfigurasiData) {
        databaseReference.setValue(configData).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Data class untuk representasi data konfigurasi pembayaran
    data class KonfigurasiData(
        val biayaLayananSistem: Int = 0,
        val biayaPPN: Int = 0,
        val noRekeningBCA: String = "",
        val noRekeningMandiri: String = "",
        val noRekeningBRI: String = "",
        val noRekeningBNI: String = ""
    )
}

