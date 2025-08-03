package com.tugas_akhir.alifnzr.pesanan

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.AdapterPesanan
import com.tugas_akhir.alifnzr.ModelPesanan
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.FragmentPesananBinding
import java.util.Locale

class Pesanan : Fragment() {

    private lateinit var binding: FragmentPesananBinding
    private lateinit var mContext: Context
    private lateinit var pesananRecyclerView: RecyclerView
    private lateinit var pesananArrayList: ArrayList<ModelPesanan>
    private lateinit var adapterRvPesanan: AdapterPesanan
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var pencarian : SearchView

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        binding = FragmentPesananBinding.inflate(LayoutInflater.from(mContext), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        pesananArrayList = ArrayList()
        pencarian = view.findViewById(R.id.cari)
        pesananRecyclerView = view.findViewById(R.id.adsRv)

        pesananRecyclerView.setHasFixedSize(true)
        pesananRecyclerView.layoutManager = LinearLayoutManager(context)
        adapterRvPesanan = AdapterPesanan(requireContext(), pesananArrayList)
        pesananRecyclerView.adapter = adapterRvPesanan

        pencarian.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val currentUserUid = currentUser.uid
            loadDataPesanan(currentUserUid)
        }
    }

    private fun filterList(query: String?){
        if (query != null){
            val filteredList = ArrayList<ModelPesanan>()
            for (i in pesananArrayList){
                if (i.nama_barang.lowercase(Locale.ROOT).contains(query)){
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()){
                adapterRvPesanan.setFilteredList(ArrayList())
            } else {
                adapterRvPesanan.setFilteredList(filteredList)
            }
        } else {
            adapterRvPesanan.setFilteredList(pesananArrayList)
        }
    }

    // Load Data berdasarkan Jenis Akun
    private fun loadDataPesanan(currentUserUid : String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("type_akun").value.toString()
                    if (userType == "pembeli") {
                        val db = FirebaseDatabase.getInstance().reference.child("Pesanan")
                        db.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                pesananArrayList.clear()
                                if (snapshot.exists()) {
                                    for (userSnapshot in snapshot.children) {
                                        val produk = userSnapshot.getValue(ModelPesanan::class.java)
                                        if (produk != null && produk.uid_pembeli == currentUserUid) {
                                            pesananArrayList.add(produk)
                                        }
                                    }
                                }
                                adapterRvPesanan.setFilteredList(pesananArrayList)
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseData", "Error: ${error.message}")
                            }
                        })
                    }
                    else if (userType == "penjual") {
                        val db = FirebaseDatabase.getInstance().reference.child("Pesanan")
                        db.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                pesananArrayList.clear()
                                if (snapshot.exists()) {
                                    for (userSnapshot in snapshot.children) {
                                        val produk = userSnapshot.getValue(
                                            ModelPesanan::class.java)
                                        if (produk != null && produk.uid_penjual == currentUserUid) {
                                            pesananArrayList.add(produk)
                                        }
                                    }
                                }
                                adapterRvPesanan.setFilteredList(pesananArrayList)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseData", "Error: ${error.message}")
                            }
                        })
                    }
                    else if (userType == "admin"){
                        val db = FirebaseDatabase.getInstance().reference.child("Pesanan")
                        db.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                pesananArrayList.clear()
                                if (snapshot.exists()) {
                                    for (userSnapshot in snapshot.children) {
                                        val produk = userSnapshot.getValue(ModelPesanan::class.java)
                                        produk?.let { pesananArrayList.add(it) }
                                    }
                                }
                                adapterRvPesanan.setFilteredList(pesananArrayList)
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseData", "Error: ${error.message}")
                            }
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}
