package com.tugas_akhir.alifnzr.admin.DaftarLaporan

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.databinding.ActivityDaftarLaporanMasalahBinding

class DaftarLaporanMasalahActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDaftarLaporanMasalahBinding
    private lateinit var mContext : Context
    private lateinit var daftarLaporanMasalahRecyclerView : RecyclerView
    private lateinit var daftarLaporanMasalahArrayList : ArrayList<ModelLaporanMasalah>
    private lateinit var adapterRVdaftarLaporanMasalah : AdapterDaftarLaporanMasalah
    private lateinit var firebaseAuth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarLaporanMasalahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadDataLaporanMasalah()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        daftarLaporanMasalahArrayList = ArrayList()
        daftarLaporanMasalahRecyclerView = binding.adsRv
        daftarLaporanMasalahRecyclerView.layoutManager = LinearLayoutManager(this)
        daftarLaporanMasalahRecyclerView.setHasFixedSize(true)
        adapterRVdaftarLaporanMasalah = AdapterDaftarLaporanMasalah(this, daftarLaporanMasalahArrayList)
        daftarLaporanMasalahRecyclerView.adapter = adapterRVdaftarLaporanMasalah
    }

    private fun loadDataLaporanMasalah(){
        val modelLaporanList = ArrayList<ModelLaporanMasalah>()
        val db = FirebaseDatabase.getInstance().reference.child("Laporan")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                daftarLaporanMasalahArrayList.clear() // Bersihkan daftar sebelum memperbarui data
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val produk = userSnapshot.getValue(ModelLaporanMasalah::class.java)
                        produk?.let { modelLaporanList.add(it) }
                    }
                }
                daftarLaporanMasalahArrayList = modelLaporanList
                adapterRVdaftarLaporanMasalah.updateData(daftarLaporanMasalahArrayList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseData", "Error: ${error.message}")
            }
        })
    }
}