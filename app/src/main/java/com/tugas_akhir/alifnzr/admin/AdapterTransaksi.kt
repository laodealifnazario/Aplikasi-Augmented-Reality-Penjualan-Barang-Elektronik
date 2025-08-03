package com.tugas_akhir.alifnzr.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.DetailTransaksiBinding
import java.util.Calendar
import java.util.Locale

class AdapterTransaksi(private val context: Context, private var transaksiList : ArrayList<ModelTransaksi>):
    RecyclerView.Adapter<AdapterTransaksi.ViewHolderTransaksi>() {

    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, ): ViewHolderTransaksi {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_transaksi, parent, false)
        return ViewHolderTransaksi(view)
    }

    override fun onBindViewHolder(holder: AdapterTransaksi.ViewHolderTransaksi, position: Int) {
        val modelTransaksi = transaksiList[position]

        firebaseAuth = FirebaseAuth.getInstance()

        // fungsi klik item
        holder.itemView.setOnClickListener {
            lihatDetailTransaksi(context, modelTransaksi, holder)
        }

        holder.idPembayaran.text = modelTransaksi.id_transaksi
        holder.totalHarga.text = modelTransaksi.harga.toString()
        holder.akunPembeli.text = modelTransaksi.akun

        fun formatTimestampDate(timestamp: Long) : String {
            val calender = Calendar.getInstance(Locale.ENGLISH)
            calender.timeInMillis = timestamp
            return android.text.format.DateFormat.format("dd/MM/yyyy", calender).toString()
        }
        val formattedDate = formatTimestampDate(modelTransaksi.timestamp)
        holder.tglPembayaran.text = formattedDate
    }

    override fun getItemCount(): Int {
        return transaksiList.size
    }

    class ViewHolderTransaksi (v : View):RecyclerView.ViewHolder(v) {
        val idPembayaran : TextView = v.findViewById(R.id.id_pembayaran_transaksi)
        val tglPembayaran : TextView = v.findViewById(R.id.tglPembayaran)
        val totalHarga : TextView = v.findViewById(R.id.hargaPembayaran)
        val akunPembeli : TextView = v.findViewById(R.id.akunDetail)
    }

    fun setFilteredList(a: ArrayList<ModelTransaksi>){
        this.transaksiList = a
        notifyDataSetChanged()
    }

    private fun lihatDetailTransaksi(a: Context, b: ModelTransaksi, c : ViewHolderTransaksi){
        val binding: DetailTransaksiBinding = DetailTransaksiBinding.inflate(LayoutInflater.from(context))
        binding.idpembayaran.text = b.id_transaksi
        // tanggal
        fun formatTimestampDate(timestamp: Long) : String {
            val calender = Calendar.getInstance(Locale.ENGLISH)
            calender.timeInMillis = timestamp
            return android.text.format.DateFormat.format("dd/MM/yyyy", calender).toString()
        }
        val formattedDate = formatTimestampDate(b.timestamp)
        binding.tanggalPembayaran.text = formattedDate
        binding.idPesananPembayaran.text = b.id_pesanan
        binding.akunPembeli.text = b.akun
        binding.nama.text = b.nama_pemilik_rekening
        binding.bankAsal.text = b.bank_asal
        binding.rekeningAsal.text = b.rekening_asal
        binding.bankTujuan.text = b.bank_tujuan_transfer
        binding.rekeningTujuan.text = b.rekening_tujuan_transfer
        binding.totalHarga.text = b.harga.toString()
        val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setView(binding.root)
            .create()
        dialog.show()
    }

}