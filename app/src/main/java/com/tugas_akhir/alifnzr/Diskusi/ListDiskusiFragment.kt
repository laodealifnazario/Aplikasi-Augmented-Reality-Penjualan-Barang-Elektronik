package com.tugas_akhir.alifnzr.Diskusi


import android.content.Context
import android.os.Bundle
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
import com.tugas_akhir.alifnzr.databinding.FragmentListDiskusiBinding
import java.util.Locale

class ListDiskusiFragment : Fragment() {

    private lateinit var binding: FragmentListDiskusiBinding
    private lateinit var adapterListDiskusiPembeli: AdapterListDiskusi
    private lateinit var listDiskusiPembeliArrayList: ArrayList<ModelListDiskusi>
    private lateinit var firebaseAuth: FirebaseAuth
    private var myUid = ""
    private lateinit var mContext : Context
    private lateinit var pencarian : SearchView
    private lateinit var diskusiRecyclerView: RecyclerView

    override fun onAttach(context : Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        binding = FragmentListDiskusiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        myUid = "${firebaseAuth.uid}"
        myUid = firebaseAuth.uid ?: ""

        listDiskusiPembeliArrayList = ArrayList()

        pencarian = view.findViewById(R.id.cari)
        diskusiRecyclerView = view.findViewById(R.id.chatsRv)

        diskusiRecyclerView.setHasFixedSize(true)
        diskusiRecyclerView.layoutManager = LinearLayoutManager(context)
        adapterListDiskusiPembeli = AdapterListDiskusi(requireContext(), listDiskusiPembeliArrayList)
        diskusiRecyclerView.adapter = adapterListDiskusiPembeli

        pencarian.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        loadDataListDiskusi()
    }

    private fun filterList(query: String?){
        if (query != null){
            val filteredList = ArrayList<ModelListDiskusi>()
            for (i in listDiskusiPembeliArrayList){
                if (i.nama.lowercase(Locale.ROOT).contains(query)){
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()){
                adapterListDiskusiPembeli.setFilteredList(ArrayList())
            } else {
                adapterListDiskusiPembeli.setFilteredList(filteredList)
            }
        } else {
            adapterListDiskusiPembeli.setFilteredList(listDiskusiPembeliArrayList)
        }
    }

    private fun loadDataListDiskusi() {
        listDiskusiPembeliArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Diskusi")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    listDiskusiPembeliArrayList.clear()
                    for (ds in snapshot.children) {
                        val chatKey = "${ds.key}"
                        // Cek apakah chatKey berisi myUid dan myUid tidak null
                        if (myUid != null && chatKey.contains(myUid)) {
                            val modelObrolan = ModelListDiskusi()
                            modelObrolan.chatKey = chatKey
                            listDiskusiPembeliArrayList.add(modelObrolan)
                        }
                    }
                    // Pastikan RecyclerView di-update di thread UI
                    activity?.runOnUiThread {
                        adapterListDiskusiPembeli = AdapterListDiskusi(context!!, listDiskusiPembeliArrayList)
                        binding.chatsRv.adapter = adapterListDiskusiPembeli
                        adapterListDiskusiPembeli.notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}