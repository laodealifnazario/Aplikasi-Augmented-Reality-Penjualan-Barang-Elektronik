package com.tugas_akhir.alifnzr.beranda

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.DetailBarang.DetailBarang
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.admin.ModelBarang

class AdapterBeranda(private val context: Context, private var barangList: ArrayList<ModelBarang>):
    RecyclerView.Adapter<AdapterBeranda.ViewHolderBarang>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBarang {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_produk, parent, false)
        return ViewHolderBarang(view)
    }

    override fun getItemCount(): Int {
        return barangList.size
    }

    override fun onBindViewHolder(holder: ViewHolderBarang, position: Int) {
        val modelBarang = barangList[position]
        holder.namaBarang.text = modelBarang.nama_barang
        holder.deskripsi.text = modelBarang.deskripsi
        holder.harga.text = modelBarang.harga.toString()
        loadGambarPertama(modelBarang, holder) // menampilkan gambar pada item

        // fungsi klik item
        holder.itemView.setOnClickListener {
            klikDetailBarang(modelBarang)
        }

        cekTypeAkun(modelBarang, holder)
    }

    class ViewHolderBarang (v : View):RecyclerView.ViewHolder(v){
        val namaBarang : TextView = v.findViewById(R.id.nama_barang)
        val deskripsi : TextView = v.findViewById(R.id.deskripsiTv)
        val harga : TextView = v.findViewById(R.id.harga)
        val gambar : ImageView = v.findViewById(R.id.imageTv)
        val status : TextView = v.findViewById(R.id.status)
    }

    fun setFilteredList(barangList: ArrayList<ModelBarang>){
        this.barangList = barangList
        notifyDataSetChanged()
    }

    private fun loadGambarPertama(a : ModelBarang, b : ViewHolderBarang) {
        val id_barang = a.id_barang
        val referensi = FirebaseDatabase.getInstance().getReference("Barang")
        referensi.child(id_barang!!).child("Images")
            .limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val  imageUrl = "${ds.child("imageUrl").value}"
                        try {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.all)
                                .into(b.gambar)
                        }
                        catch (e : Exception) {
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun klikDetailBarang(modelBarang: ModelBarang){
        Log.d("AdapterBeranda", "Item diklik")
        val intent = Intent(context, DetailBarang::class.java)
        intent.putExtra("id_barang", modelBarang.id_barang)
        context.startActivity(intent)
    }

    private fun cekTypeAkun(a: ModelBarang, holder : ViewHolderBarang) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("type_akun").value.toString()
                    if (userType == "admin") {
                        val id_barang = a.id_barang
                        val refBarang = FirebaseDatabase.getInstance().getReference("Barang")
                        refBarang.child(id_barang!!).child("Objek3D")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    // Cek apakah ada data Objek3D
                                    if (snapshot.exists()) {
                                        // Jika objek 3D sudah ada, sembunyikan status unggah objek 3D AR
                                        holder.status.visibility = View.GONE
                                    } else {
                                        // Jika objek 3D belum ada, tampilkan status unggah objek 3D AR
                                        holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.unggahObjek3D))
                                        holder.status.text = "  Unggah Objek 3D AR  "
                                        holder.status.visibility = View.VISIBLE
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Tangani error jika diperlukan
                                }
                            })
                    }
                    else if (userType == "penjual") {
                        val id_barang = a.id_barang
                        val refBarang = FirebaseDatabase.getInstance().getReference("Barang")
                        refBarang.child(id_barang!!).child("Objek3D")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    // Cek apakah ada data Objek3D
                                    if (snapshot.exists()) {
                                        // Jika objek 3D sudah ada, sembunyikan status unggah objek 3D AR
                                        holder.status.visibility = View.GONE
                                    } else {
                                        // Jika objek 3D belum ada, tampilkan status unggah objek 3D AR
                                        holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.unggahObjek3D))
                                        holder.status.text = "  Objek 3D AR Sedang diproses oleh Admin  "
                                        holder.status.visibility = View.VISIBLE
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Tangani error jika diperlukan
                                }
                            })
                    }
                    else{holder.status.visibility = View.GONE}
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

}