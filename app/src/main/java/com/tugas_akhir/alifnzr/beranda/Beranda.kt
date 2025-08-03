package com.tugas_akhir.alifnzr.beranda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.JualBarang.JualBarang
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.admin.ModelBarang
import com.tugas_akhir.alifnzr.databinding.FragmentBerandaBinding
import java.util.Locale

class Beranda : Fragment() {

    private lateinit var binding: FragmentBerandaBinding
    private lateinit var mContext : Context
    private lateinit var barangRecyclerView: RecyclerView
    private lateinit var barangArrayList : ArrayList<ModelBarang>
    private lateinit var adapterRvBarang : AdapterBeranda
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var pencarian : SearchView

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        binding = FragmentBerandaBinding.inflate(LayoutInflater.from(mContext),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        barangArrayList = ArrayList()
        pencarian = view.findViewById(R.id.cari)
        barangRecyclerView = view.findViewById(R.id.lihatRVbarang)
        barangRecyclerView.setHasFixedSize(true)
        barangRecyclerView.layoutManager = LinearLayoutManager(context)
        adapterRvBarang = AdapterBeranda(requireContext(), barangArrayList)
        barangRecyclerView.adapter = adapterRvBarang

        pencarian.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
        loadDataBarang()
        binding.jualBarang.setOnClickListener {
            jualBarang()
        }
    }

    private fun filterList(query: String?){
        if (query != null){
            val filteredList = ArrayList<ModelBarang>()
            for (i in barangArrayList){
                if (i.nama_barang.lowercase(Locale.ROOT).contains(query)){
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()){
                adapterRvBarang.setFilteredList(ArrayList())
            } else {
                adapterRvBarang.setFilteredList(filteredList)
            }
        } else {
            adapterRvBarang.setFilteredList(barangArrayList)
        }
    }

    private fun loadDataBarang() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("type_akun").value.toString()
                    if (userType == "pembeli") {
                        val modelBarangList = ArrayList<ModelBarang>()
                        val db = FirebaseDatabase.getInstance().reference.child("Barang")
                        db.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                modelBarangList.clear() // Bersihkan daftar sebelum memperbarui data
                                if (snapshot.exists()) {
                                    for (barangSnapshot in snapshot.children) {
                                        val barang = barangSnapshot.getValue(ModelBarang::class.java)
                                        // Periksa apakah atribut "Objek3D" ada dan tidak kosong
                                        val objek3D = barangSnapshot.child("Objek3D").value?.toString()
                                        if (!objek3D.isNullOrEmpty() && barang != null) {
                                            modelBarangList.add(barang) // Tambahkan barang ke daftar
                                        }
                                    }
                                }
                                // Perbarui RecyclerView
                                barangArrayList = modelBarangList
                                adapterRvBarang.setFilteredList(barangArrayList)

                                // Tampilkan pesan jika tidak ada barang dengan file objek 3D
                                if (barangArrayList.isEmpty()) {
                                    Toast.makeText(context, "Tidak ada barang dengan objek 3D", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseData", "Error: ${error.message}")
                            }
                        })
                        binding.jualBarang.visibility = View.GONE
                    }
                    else if (userType == "admin"){
                        val modelBarangList = ArrayList<ModelBarang>()
                        val db = FirebaseDatabase.getInstance().reference.child("Barang")
                        db.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                barangArrayList.clear() // Bersihkan daftar sebelum memperbarui data
                                if (snapshot.exists()) {
                                    for (userSnapshot in snapshot.children) {
                                        val produk = userSnapshot.getValue(ModelBarang::class.java)
                                        produk?.let { modelBarangList.add(it) }
                                    }
                                }
                                barangArrayList = modelBarangList
                                adapterRvBarang.setFilteredList(barangArrayList)
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseData", "Error: ${error.message}")
                            }
                        })
                        binding.jualBarang.visibility = View.GONE
                    }
                    else if (userType == "penjual") {
                        val db = FirebaseDatabase.getInstance().reference.child("Barang")
                        val currentUserUID = FirebaseAuth.getInstance().uid
                        db.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                barangArrayList.clear()
                                if (snapshot.exists()) {
                                    for (barangSnapshot in snapshot.children) {
                                        val barang = barangSnapshot.getValue(ModelBarang::class.java)
                                        if (barang != null && barang.uid == currentUserUID) {
                                            barangArrayList.add(barang)
                                        }
                                    }
                                    adapterRvBarang.notifyDataSetChanged()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseData", "Error: ${error.message}")
                            }
                        })
                        binding.jualBarang.visibility = View.VISIBLE
                    } else{}
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun jualBarang(){
        val intent = Intent(context, JualBarang::class.java)
        startActivity(intent)
    }
}