package com.tugas_akhir.alifnzr.admin

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
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.FragmentTransaksiAdminBinding
import java.util.Locale

class TransaksiAdminFragment : Fragment() {

    private lateinit var binding: FragmentTransaksiAdminBinding
    private lateinit var mContext : Context
    private lateinit var transaksiRecyclerView : RecyclerView
    private lateinit var transaksiArrayList : ArrayList<ModelTransaksi>
    private lateinit var adapterRVtransaksi : AdapterTransaksi
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var pencarian : SearchView

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        binding = FragmentTransaksiAdminBinding.inflate(LayoutInflater.from(mContext),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        transaksiArrayList = ArrayList()
        pencarian = view.findViewById(R.id.cari)
        transaksiRecyclerView = view.findViewById(R.id.lihatTransaksi)

        transaksiRecyclerView.layoutManager = LinearLayoutManager(context)
        transaksiRecyclerView.setHasFixedSize(true)
        adapterRVtransaksi = AdapterTransaksi(requireContext(), transaksiArrayList)
        transaksiRecyclerView.adapter = adapterRVtransaksi

        pencarian.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        loadDataTransaksi()
    }

    private fun filterList(query: String?){
        if (query != null){
            val filteredList = ArrayList<ModelTransaksi>()
            for (i in transaksiArrayList){
                if (i.id_transaksi.lowercase(Locale.ROOT).contains(query)){
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()){
                adapterRVtransaksi.setFilteredList(ArrayList())
            } else {
                adapterRVtransaksi.setFilteredList(filteredList)
            }
        } else {
            adapterRVtransaksi.setFilteredList(transaksiArrayList)
        }
    }

    private fun loadDataTransaksi(){
        val modelTransaksiList = ArrayList<ModelTransaksi>()
        val db = FirebaseDatabase.getInstance().reference.child("Transaksi")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transaksiArrayList.clear()
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val produk = userSnapshot.getValue(ModelTransaksi::class.java)
                        produk?.let { modelTransaksiList.add(it) }
                    }
                }
                transaksiArrayList = modelTransaksiList
                adapterRVtransaksi.setFilteredList(transaksiArrayList)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseData", "Error: ${error.message}")
            }
        })
    }

}