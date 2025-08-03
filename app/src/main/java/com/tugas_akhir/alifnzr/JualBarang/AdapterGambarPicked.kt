package com.tugas_akhir.alifnzr.JualBarang


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.RowGambarPickedBinding

class AdapterGambarPicked (

    private val context: Context,
    private val imagePickedArrayList : ArrayList<ModelGambarPicked>,
    private val adId: String

    ) : Adapter<AdapterGambarPicked.HolderImagePicked>() {

    private lateinit var binding : RowGambarPickedBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagePicked {
        // untuk memanggil binding dari row_gambar_picked.xml
        binding = RowGambarPickedBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagePicked(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {
        // mengambil data dari posisi Daftar tertentu dan atur ke Tampilan UI row_gambar_picked.xml dan menangani klik
        val model = imagePickedArrayList[position]

        if (model.fromInternet){
            try {
                val imageUrl = model.imagerUrl
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.akun)
                    .into(holder.imageIv)
            }
            catch (e:Exception) {
            }
        }
        else {
            try {
                val imageUri = model.imageUri
                Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.imageIv)
            }
            catch (e:Exception) {
            }
        }
        holder.closeBtn.setOnClickListener{
            if(model.fromInternet){
                deleteImageFirebase(model,holder,position)
            }
            imagePickedArrayList.remove(model)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return imagePickedArrayList.size
    }

    private fun sanitizeFirebasePath(path: String): String {
        // Mengganti karakter yang tidak diizinkan dengan karakter lain, misalnya '_' atau ' '
        return path.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
    }

    private fun deleteImageFirebase(a: ModelGambarPicked, b: HolderImagePicked, c: Int) {
        val imageId = sanitizeFirebasePath(a.id.toString()) // Membersihkan karakter tidak diizinkan
        val adIdSanitized = sanitizeFirebasePath(adId) // Membersihkan karakter pada adId jika diperlukan

        val ref = FirebaseDatabase.getInstance().getReference("Barang")
        if (adIdSanitized.isNotEmpty() && imageId.isNotEmpty()) {
            ref.child(adIdSanitized).child("Images").child(imageId)
                .removeValue()
                .addOnSuccessListener {
                    imagePickedArrayList.remove(a)
                    notifyItemRemoved(c)
                }
                .addOnFailureListener { exception ->
                }
        } else {
        }
    }

    inner class HolderImagePicked(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageIv = binding.imageIv
        var closeBtn = binding.closeBtn
    }
}