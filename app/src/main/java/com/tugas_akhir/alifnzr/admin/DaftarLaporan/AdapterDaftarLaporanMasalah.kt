package com.tugas_akhir.alifnzr.admin.DaftarLaporan

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.tugas_akhir.alifnzr.Diskusi.DetailDiskusiActivity
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.DeskripsiLaporanBinding
import java.util.Calendar
import java.util.Locale

class AdapterDaftarLaporanMasalah (private val context: Context, private var laporanMasalahList : ArrayList<ModelLaporanMasalah>):
    RecyclerView.Adapter<AdapterDaftarLaporanMasalah.ViewHolderLaporan>()  {

    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, ): ViewHolderLaporan {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_laporan, parent, false)
        return ViewHolderLaporan(view)
    }

    override fun onBindViewHolder(holder: ViewHolderLaporan, position: Int) {
        val modelMasalah = laporanMasalahList[position]

        firebaseAuth = FirebaseAuth.getInstance()

        // fungsi klik item
        holder.itemView.setOnClickListener {
            lihatDetailLaporanMasalah(context, modelMasalah, holder)
        }

        fun formatTimestampDate(timestamp: Long) : String {
            val calender = Calendar.getInstance(Locale.ENGLISH)
            calender.timeInMillis = timestamp
            return android.text.format.DateFormat.format("dd/MM/yyyy", calender).toString()
        }
        val formattedDate = formatTimestampDate(modelMasalah.timestamp)
        holder.tglLaporan.text = formattedDate
        holder.akun.text = modelMasalah.akun
        holder.jenisLaporan.text = modelMasalah.jenis_laporan
        holder.typeAkun.text = modelMasalah.type_akun
    }

    override fun getItemCount(): Int {
        return laporanMasalahList.size
    }

    class ViewHolderLaporan (v : View): RecyclerView.ViewHolder(v) {
        val akun : TextView = v.findViewById(R.id.akun)
        val tglLaporan : TextView = v.findViewById(R.id.tglLaporan)
        val typeAkun : TextView = v.findViewById(R.id.typeAkun)
        val jenisLaporan : TextView = v.findViewById(R.id.jenisLaporan)
    }

    fun updateData(newList: ArrayList<ModelLaporanMasalah>) {
        laporanMasalahList = newList
        notifyDataSetChanged()
    }

    private fun lihatDetailLaporanMasalah(a: Context, b: ModelLaporanMasalah, c : ViewHolderLaporan){
        val binding : DeskripsiLaporanBinding = DeskripsiLaporanBinding.inflate(LayoutInflater.from(context))
        firebaseAuth = FirebaseAuth.getInstance()
        // output deskripsi
        binding.deskripsi.text = b.deskripsi_masalah
        // tombol diskusi
        binding.diskusi.setOnClickListener {
            tombolDiskusi(b)
        }
        val inputDialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setView(binding.root)
        inputDialog.show()
    }
    private fun tombolDiskusi(b : ModelLaporanMasalah){
        val intent = Intent(context, DetailDiskusiActivity::class.java)
        intent.putExtra("receiptUid", b.uid)
        context.startActivity(intent)
    }
}