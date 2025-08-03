package com.tugas_akhir.alifnzr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.DetailBarang.AdapterGambarSlider
import com.tugas_akhir.alifnzr.DetailBarang.ModelGambarSlider
import com.tugas_akhir.alifnzr.databinding.ActivityDetailBuktiPengirimanBinding
import com.tugas_akhir.alifnzr.lokasi.LihatLokasi
import java.util.ArrayList

class DetailBuktiPengirimanBarang : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBuktiPengirimanBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var id_pengiriman_barang = ""
    private var id_pesanan = ""
    private lateinit var imageSliderArrayList : ArrayList<ModelGambarSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBuktiPengirimanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        id_pengiriman_barang = intent.getStringExtra("id_pengiriman_barang").toString()
        id_pesanan = intent.getStringExtra("id_pesanan").toString()

        loadFotoBuktiPengiriman()
        loadDetailBuktiPengiriman()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnLokasi.setOnClickListener {
            val intent = Intent(this, LihatLokasi::class.java)
            intent.putExtra("id_pesanan", id_pesanan)
            startActivity(intent)
        }
    }

    private fun loadDetailBuktiPengiriman() {
        val ref1 = FirebaseDatabase.getInstance().getReference("Bukti_Pengiriman")
        ref1.child(id_pengiriman_barang)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val namaPengirim = snapshot.child("nama_pengirim").value.toString()
                    val jasaEkspedisi = snapshot.child("jasa_ekspedisi").value.toString()
                    val nomorResi = snapshot.child("nomor_resi").value.toString()

                    // Menampilkan data ke View melalui binding
                    binding.namaPengirim.text = namaPengirim
                    binding.jasaEkspedisi.text = jasaEkspedisi
                    binding.noResi.text = nomorResi
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        val ref2 = FirebaseDatabase.getInstance().getReference("Pesanan")
        ref2.child(id_pesanan)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val alamatPenjual = snapshot.child("alamat_penjual").value.toString()
                    val alamatPembeli = snapshot.child("alamat_pembeli").value.toString()
                    //
                    binding.alamatPengirim.text = alamatPenjual
                    binding.alamatPenerima.text = alamatPembeli
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadFotoBuktiPengiriman() {
        imageSliderArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Bukti_Pengiriman")
        ref.child(id_pengiriman_barang).child("Images")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    imageSliderArrayList.clear()
                    for (ds in snapshot.children){
                        try {
                            val modelImageSlider = ds.getValue(ModelGambarSlider::class.java)
                            imageSliderArrayList.add(modelImageSlider!!)
                        } catch (e : Exception){
                        }
                    }
                    binding.gambarBuktiPengiriman.adapter = AdapterGambarSlider(this@DetailBuktiPengirimanBarang, imageSliderArrayList)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}