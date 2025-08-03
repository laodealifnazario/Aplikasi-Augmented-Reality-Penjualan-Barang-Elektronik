package com.tugas_akhir.alifnzr.bayar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.DetailBarang.AdapterGambarSlider
import com.tugas_akhir.alifnzr.DetailBarang.ModelGambarSlider
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.DetailBuktiPembayaranManualBinding
import java.util.ArrayList

class DetailBuktiPembayaranManual : AppCompatActivity() {

    private lateinit var binding: DetailBuktiPembayaranManualBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var id_transaksi = ""
    private lateinit var imageSliderArrayList : ArrayList<ModelGambarSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_bukti_pembayaran_manual)
        binding = DetailBuktiPembayaranManualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        id_transaksi = intent.getStringExtra("id_transaksi").toString()

        loadDetailBuktiPengiriman()
        loadFotoBuktiMutasi()
    }

    private fun loadDetailBuktiPengiriman() {
        val ref = FirebaseDatabase.getInstance().getReference("Transaksi")
        ref.child(id_transaksi)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        // Mendapatkan data dari snapshot
                        val harga = snapshot.child("harga").value.toString()
                        val nama_pemilik_rekening = snapshot.child("nama_pemilik_rekening").value.toString()
                        val no_rekening = snapshot.child("rekening_tujuan_transfer").value.toString()
                        val nama_bank = snapshot.child("bank_asal").value.toString()

                        // Menampilkan data ke View melalui binding
                        binding.uang.text = harga
                        binding.namaPemilikRekening.text = nama_pemilik_rekening
                        binding.noRekening.text = no_rekening
                        binding.namaBank.text = nama_bank

                    } catch (e: Exception) {
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadFotoBuktiMutasi() {
        imageSliderArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Transaksi")
        ref.child(id_transaksi).child("Images")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    imageSliderArrayList.clear()
                    for (ds in snapshot.children){
                        try {
                            val modelImageSlider = ds.getValue(ModelGambarSlider::class.java)
                            imageSliderArrayList.add(modelImageSlider!!)
                        } catch (e : Exception){
                        }
                    }
                    binding.gambarSliderVp.adapter = AdapterGambarSlider(this@DetailBuktiPembayaranManual, imageSliderArrayList)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}