package com.tugas_akhir.alifnzr

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.DetailBarang.DetailBarang
import com.tugas_akhir.alifnzr.bayar.Pembayaran
import com.tugas_akhir.alifnzr.bayar.DetailBuktiPembayaranManual
import com.tugas_akhir.alifnzr.databinding.DetailPesananBinding
import com.tugas_akhir.alifnzr.databinding.LaporkanMasalahBinding
import java.util.Calendar
import java.util.Locale

class AdapterPesanan (private val context: Context, private var pesananList : ArrayList<ModelPesanan>):
    RecyclerView.Adapter<AdapterPesanan.ViewHolderPesanan>() {

    private var type_akun = ""
    private var deskripsi = ""
    private var jenis_laporan = ""
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, ): ViewHolderPesanan {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_pesanan_pembeli, parent, false)
        return ViewHolderPesanan(view)
    }

    override fun onBindViewHolder(holder: ViewHolderPesanan, position: Int) {
        val modelPesanan = pesananList[position]
        firebaseAuth = FirebaseAuth.getInstance()
        holder.idPesanan.text = modelPesanan.id_pesanan

        // fungsi klik item
        holder.itemView.setOnClickListener {
            lihatDetailPesanan(context, modelPesanan.id_pesanan, holder)
        }
        val timestamp = modelPesanan.timestamp
        fun formatTimestampDate(timestamp: Long) : String {
            val calender = Calendar.getInstance(Locale.ENGLISH)
            calender.timeInMillis = timestamp
            return android.text.format.DateFormat.format("dd/MM/yyyy", calender).toString()
        }
        val formattedDate = formatTimestampDate(timestamp)
        holder.tanggal.text = formattedDate
        holder.namaBarang.text = modelPesanan.nama_barang
        holder.harga.text = modelPesanan.total_harga.toString()
        holder.status.text = modelPesanan.status
        cekTypeAkun(modelPesanan, holder)

    }

    override fun getItemCount(): Int {
        return pesananList.size
    }

    class ViewHolderPesanan (v : View):RecyclerView.ViewHolder(v) {
        val idPesanan : TextView = v.findViewById(R.id.id_pesanan)
        val tanggal : TextView = v.findViewById(R.id.tglPesanan)
        val namaBarang : TextView = v.findViewById(R.id.nama_barang)
        val harga : TextView = v.findViewById(R.id.harga)
        val status : TextView = v.findViewById(R.id.statusTV)
        // pembeli
        val bayar : Button = v.findViewById(R.id.bayar)
        val lihatBuktiPengiriman : Button = v.findViewById(R.id.lihatBuktiPengiriman)
        val konfirmasiPembeli : Button = v.findViewById(R.id.konfirmasiPembeli)
        // penjual
        val konfirmasiPenjual : Button = v.findViewById(R.id.konfrimasiPenjual)
        val kirimBarang : Button = v.findViewById(R.id.kirimBarang)
        // admin
        val batalkan : Button = v.findViewById(R.id.batalkan)
        val verifikasiPembayaran : Button = v.findViewById(R.id.verifikasi_pembayaran)
        val verifikasiPengirimanBarang : Button = v.findViewById(R.id.verifikasiPengirimanBarang)
        val verifikasiPengembalianUang : Button = v.findViewById(R.id.verifikasi_pengembalian_uang)
    }

    fun setFilteredList(pesananList: ArrayList<ModelPesanan>){
        this.pesananList = pesananList
        notifyDataSetChanged()
    }

    private fun cekTypeAkun(modelPesanan : ModelPesanan, holder : ViewHolderPesanan) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("type_akun").value.toString()
                    if (userType == "pembeli") {
                        holder.status.text = modelPesanan.status
                        // Mengubah warna tombol berdasarkan status dengan if-else
                        if (modelPesanan.status == "bayar") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.belumlunas))
                            holder.status.text = "Lakukan pembayaran"
                            holder.bayar.visibility = View.VISIBLE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL BAYAR
                            holder.bayar.setOnClickListener {
//                                bayar(context, modelPesanan.id_pesanan)
                                bayar(context, modelPesanan.id_pesanan, modelPesanan.total_harga, modelPesanan.uid_pembeli)
                            }
                        }
                        else if (modelPesanan.status == "verifikasi_pembayaran") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.pengiriman)) // Warna hijau yang telah didefinisikan
                            holder.status.text = "Pembayaran sedang diverifikasi"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "kirim_barang") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.konfirmasi)) // Warna hijau yang telah didefinisikan
                            holder.status.text = "Pengiriman sedang diproses"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "verifikasi_bukti_pengiriman"){
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.konfirmasi)) // Warna hijau yang telah didefinisikan
                            holder.status.text = "Pengiriman sedang diproses"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "konfirmasi_penjual") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.konfirmasi))
                            holder.status.text = "Pesanan anda sedang dalam perjalanan"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.VISIBLE
                            holder.lihatBuktiPengiriman.setOnClickListener{
                                lihatDetailBuktiPengiriman(context, modelPesanan.id_pesanan, modelPesanan.id_pengiriman_barang)
                            }
                        }
                        else if (modelPesanan.status == "konfirmasi_pembeli") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.barang_sudah_tiba))
                            holder.status.text = "Barang telah sampai"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.VISIBLE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL KONFIRMASI BARANG SUDAH TIBA
                            holder.konfirmasiPembeli.setOnClickListener {
                                konfirmasiPembeli(context, modelPesanan.id_pesanan)
                            }
                        }
                        else if (modelPesanan.status == "selesai") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.selesai))
                            holder.status.text = "Selesai"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "verifikasi_pengembalian_uang") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.verifikasiKomplain))
                            holder.status.text = "Komplain sedang diproses"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "refund") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.refund))
                            holder.status.text = "Uang dikembalikan"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "dibatalkan") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.dibatalkan))
                            holder.status.text = "Pesanan dibatalkan"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.dibatalkan))
                            holder.status.text = "Pesanan dibatalkan"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                    }
                    else if (userType == "penjual") {
                        // Mengubah warna tombol berdasarkan status dengan if-else
                        if (modelPesanan.status == "bayar") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.belumlunas))
                            holder.status.text = "Menunggu pembayaran dari pembeli"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "verifikasi_pembayaran") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.pengiriman)) // Warna hijau yang telah didefinisikan
                            holder.status.text = "Pembayaran sedang diverifikasi"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "kirim_barang") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.tombolKirimBarang)) // Warna hijau yang telah didefinisikan
                            holder.status.text = "Kirim bukti pengiriman barang"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.VISIBLE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL BAYAR
                            holder.kirimBarang.setOnClickListener {
                                kirimBuktiPengiriman(context, modelPesanan.id_pesanan)
                            }
                        }
                        else if (modelPesanan.status == "verifikasi_bukti_pengiriman"){
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.konfirmasi)) // Warna hijau yang telah didefinisikan
                            holder.status.text = "Bukti pengiriman barang sedang diverifikasi"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.VISIBLE
                            holder.lihatBuktiPengiriman.setOnClickListener{
                                lihatDetailBuktiPengiriman(context, modelPesanan.id_pesanan, modelPesanan.id_pengiriman_barang)
                            }
                        }
                        else if (modelPesanan.status == "konfirmasi_penjual") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.konfirmasi))
                            holder.status.text = "Konfirmasi barang telah sampai"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.VISIBLE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL KONFIRMASI BARANG SUDAH DIKIRIM
                            holder.konfirmasiPenjual.setOnClickListener {
                                konfirmasiPenjual(context, modelPesanan.id_pesanan, modelPesanan.id_pengiriman_barang)
                            }
                        }
                        else if (modelPesanan.status == "konfirmasi_pembeli") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.menungguKonfirmasiPembeli))
                            holder.status.text = "Menunggu konfirmasi pembeli"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "selesai") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.selesai))
                            holder.status.text = "Selesai"
                            holder.kirimBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.bayar.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "verifikasi_pengembalian_uang") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.verifikasiKomplain))
                            holder.status.text = "Menunggu verifikasi komplain dari pembeli"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "refund") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.refund))
                            holder.status.text = "Barang anda dikembalikan"
                            holder.kirimBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.bayar.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "dibatalkan") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.dibatalkan))
                            holder.status.text = "Pesanan dibatalkan"
                            holder.kirimBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.bayar.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.dibatalkan))
                            holder.status.text = "Pesanan dibatalkan"
                            holder.kirimBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.bayar.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                    }
                    else if (userType == "admin"){
                        // Mengubah warna tombol berdasarkan status dengan if-else
                        if (modelPesanan.status == "bayar") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.belumlunas))
                            holder.status.text = "Menunggu pembayaran pembeli"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.VISIBLE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL BATALKAN
                            holder.batalkan.setOnClickListener {
                                batalkan(context, modelPesanan.id_pesanan)
                            }
                        }
                        else if (modelPesanan.status == "verifikasi_pembayaran") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.pengiriman)) // Warna hijau yang telah didefinisikan
                            holder.status.text = "Lakukan verifikasi pembayaran"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.VISIBLE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL VERIFIKASI PEMBAYARAN
                            holder.verifikasiPembayaran.setOnClickListener {
                                verifikasiPembayaran(context, modelPesanan.id_pesanan, modelPesanan.id_transaksi)
                            }
                        }
                        else if (modelPesanan.status == "kirim_barang") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.pengiriman)) // Warna hijau yang telah didefinisikan
                            holder.status.text = "Menunggu bukti pengiriman"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.VISIBLE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL BATALKAN
                            holder.batalkan.setOnClickListener {
                                batalkan(context, modelPesanan.id_pesanan)
                            }
                        }
                        else if (modelPesanan.status == "verifikasi_bukti_pengiriman"){
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.konfirmasi)) // Warna hijau yang telah didefinisikan
                            holder.status.text = "Lakukan verifikasi bukti pengiriman barang"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.VISIBLE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL VERIFIKASI BUKTI PENGIRIMAN
                            holder.verifikasiPengirimanBarang.setOnClickListener{
                                verifikasiBuktiPengiriman(context, modelPesanan.id_pesanan, modelPesanan.id_pengiriman_barang)
                            }
                        }
                        else if (modelPesanan.status == "konfirmasi_penjual") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.konfirmasi))
                            holder.status.text = "Menunggu konfirmasi penjual"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.VISIBLE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL BATALKAN
                            holder.batalkan.setOnClickListener {
                                batalkan(context, modelPesanan.id_pesanan)
                            }
                        }
                        else if (modelPesanan.status == "konfirmasi_pembeli") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.konfirmasi))
                            holder.status.text = "Menunggu konfirmasi pembeli"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.VISIBLE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL BATALKAN
                            holder.batalkan.setOnClickListener {
                                batalkan(context, modelPesanan.id_pesanan)
                            }
                        }
                        else if (modelPesanan.status == "selesai") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.selesai))
                            holder.status.text = "Selesai"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "verifikasi_pengembalian_uang") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.menungguKonfirmasiPembeli))
                            holder.status.text = "Lakukan verifkasi komplain dari pembeli"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.VISIBLE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                            // TOMBOL BATALKAN
                            holder.verifikasiPengembalianUang.setOnClickListener {
                                verifikasiPengembalianUang(context, modelPesanan.id_pesanan)
                            }
                        }
                        else if (modelPesanan.status == "refund") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.refund))
                            holder.status.text = "Uang dikembalikan"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else if (modelPesanan.status == "dibatalkan") {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.dibatalkan))
                            holder.status.text = "Pesanan dibatalkan"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                        else {
                            holder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.dibatalkan))
                            holder.status.text = "Pesanan dibatalkan"
                            holder.bayar.visibility = View.GONE
                            holder.konfirmasiPembeli.visibility = View.GONE
                            holder.kirimBarang.visibility = View.GONE
                            holder.verifikasiPengirimanBarang.visibility = View.GONE
                            holder.konfirmasiPenjual.visibility = View.GONE
                            holder.batalkan.visibility = View.GONE
                            holder.verifikasiPengembalianUang.visibility = View.GONE
                            holder.verifikasiPembayaran.visibility = View.GONE
                            holder.lihatBuktiPengiriman.visibility = View.GONE
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun lihatDetailPesanan(context: Context, idPesanan: String, holder: ViewHolderPesanan) {
        val binding: DetailPesananBinding = DetailPesananBinding.inflate(LayoutInflater.from(context))
        val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
        ref.child(idPesanan)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // menampilkan detail barang
                    val modelPesanan = snapshot.getValue(ModelPesanan::class.java)
                    if (modelPesanan != null) {
                        val harga = modelPesanan.total_harga
                        val timestamp = modelPesanan.timestamp
                        val biayaPengiriman = 7000
                        val biayaLayananSistem = 1000

                        val totalHarga = harga + biayaPengiriman + biayaLayananSistem
                        binding.totalHarga.text = totalHarga.toString()
                        binding.idpembayaran.text = modelPesanan.id_pesanan
                        binding.detailIdBarang.text = modelPesanan.id_barang
                        binding.detailNamaBarang.text = modelPesanan.nama_barang

                        // load nama akun pembeli dan penjual
                        // Referensi ke Firebase Authentication untuk mendapatkan pengguna yang sedang login
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val userRef = FirebaseDatabase.getInstance().getReference("Users")
                        currentUser?.let { user ->
                            userRef.child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    // Periksa jenis akun dari pengguna yang sedang login
                                    val jenisAkun = snapshot.child("type_akun").value.toString() // contoh: "pembeli" atau "penjual"

                                    if (jenisAkun == "pembeli") {
                                        // Tampilkan informasi akun penjual
                                        val penjualUid = modelPesanan.uid_penjual // Uid penjual dari data pesanan
                                        val penjualRef = FirebaseDatabase.getInstance().getReference("Users").child(penjualUid)

                                        penjualRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val akunPenjual = snapshot.child("email").value.toString()
                                                binding.akunPenjual.text = akunPenjual
                                                binding.akunPenjual.visibility = View.VISIBLE
                                                binding.labelPembeli.visibility = View.GONE
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
                                    }
                                    else if (jenisAkun == "penjual") {
                                        // Tampilkan informasi akun pembeli
                                        val pembeliUid = modelPesanan.uid_pembeli // Uid pembeli dari data pesanan
                                        val pembeliRef = FirebaseDatabase.getInstance().getReference("Users").child(pembeliUid)
                                        pembeliRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val akunPembeli = snapshot.child("email").value.toString()
                                                binding.akunPembeli.text = akunPembeli
                                                binding.akunPembeli.visibility = View.VISIBLE
                                                binding.labelPenjual.visibility = View.GONE
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
                                    }
                                    else if (jenisAkun == "admin") {
                                        // Tampilkan informasi akun pembeli
                                        val pembeliUid = modelPesanan.uid_pembeli // Uid pembeli dari data pesanan
                                        val penjualUid = modelPesanan.uid_penjual // Uid penjual dari data pesanan

                                        // Referensi ke data pembeli
                                        val pembeliRef = FirebaseDatabase.getInstance().getReference("Users").child(pembeliUid)
                                        pembeliRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val akunPembeli = snapshot.child("email").value.toString()
                                                binding.akunPembeli.text = akunPembeli
                                                binding.akunPembeli.visibility = View.VISIBLE // Tampilkan akun pembeli
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })

                                        // Referensi ke data penjual
                                        val penjualRef = FirebaseDatabase.getInstance().getReference("Users").child(penjualUid)
                                        penjualRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val akunPenjual = snapshot.child("email").value.toString()
                                                binding.akunPenjual.text = akunPenjual
                                                binding.akunPenjual.visibility = View.VISIBLE // Tampilkan akun penjual
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
                                        // Pastikan label juga terlihat
                                        binding.labelPenjual.visibility = View.VISIBLE
                                        binding.labelPembeli.visibility = View.VISIBLE
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                        }


                        fun formatTimestampDate(timestamp: Long) : String {
                            val calender = Calendar.getInstance(Locale.ENGLISH)
                            calender.timeInMillis = timestamp
                            return android.text.format.DateFormat.format("dd/MM/yyyy", calender).toString()
                        }
                        val formattedDate = formatTimestampDate(timestamp)
                        holder.tanggal.text = formattedDate
                        binding.tanggalDetil.text = formattedDate

                        val konfirmasiBeli = androidx.appcompat.app.AlertDialog.Builder(context)
                            .setView(binding.root)
                        konfirmasiBeli.show()

                        // tombol Lihat Detail Barang
                        binding.buttonViewOrder.setOnClickListener {
                            Log.d("AdapterBeranda", "Item diklik")
                            val intent = Intent(context, DetailBarang::class.java)
                            intent.putExtra("id_barang", modelPesanan.id_barang)
                            context.startActivity(intent)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    // Mode Pembeli
    private fun bayar(context: Context, idPesanan: String, harga: Int, uidPembeli: String) {
        val intent = Intent(context, Pembayaran::class.java)
        intent.putExtra("id_pesanan", idPesanan)
        intent.putExtra("harga", harga)
        intent.putExtra("uid_pembeli", uidPembeli)
        context.startActivity(intent)
    }
    private fun konfirmasiPembeli(context: Context, idPesanan : String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Konfirmasi Barang Telah Sampai")
        builder.setMessage("Apakah barang telah sampai dengan baik?")
        builder.setPositiveButton("Ya") { dialog, which ->
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["status"] = "selesai"
            val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
            ref.child(idPesanan).updateChildren(hashMap)
            dialog.dismiss()
        }
        builder.setNegativeButton("Ajukan Komplain") { dialog, which ->
            val binding : LaporkanMasalahBinding = LaporkanMasalahBinding.inflate(LayoutInflater.from(context))
            firebaseAuth = FirebaseAuth.getInstance()
            val dataAkun = FirebaseDatabase.getInstance().getReference("Users")
            dataAkun.ref.child(firebaseAuth.uid!!)
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val namaAkun = snapshot.child("email").value.toString()
                        val typeAkun = snapshot.child("typeAkun").value.toString()
                        type_akun = typeAkun
                    }
                    override fun onCancelled(error: DatabaseError) { TODO("Not yet implemented") } }
                )
            val jenisLaporan = arrayOf(
                "Pesanan Tidak Diterima",
                "Pesanan Tidak Sesuai",
                "Barang Rusak atau Cacat",
                "lainnya"
            )
            val adapterJenisLaporan = ArrayAdapter(context, R.layout.row_jenis_barang, jenisLaporan)
            binding.jenisLaporan.setAdapter(adapterJenisLaporan)
            val inputDialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setView(binding.root)
                .setPositiveButton("Kirim"){_,_->
                    jenis_laporan = binding.jenisLaporan.text.toString().trim()
                    deskripsi = binding.inputLaporan.text.toString().trim()
                    val data = FirebaseDatabase.getInstance().getReference("Laporan")
                    val keyId = data.push().key
                    val hashMap: HashMap<String, Any?> = HashMap()
                    hashMap["id_laporan_masalah"] = keyId
                    hashMap["type_akun"] = type_akun
                    hashMap["uid"] = firebaseAuth.uid
                    hashMap["jenis_laporan"] = jenis_laporan
                    hashMap["deskripsi_masalah"] = deskripsi
                    hashMap["timestamp"] = System.currentTimeMillis()
                    data.child(keyId!!).setValue(hashMap)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Laporan Berhasil Dikirim", Toast.LENGTH_SHORT).show()
                            val hashMap: HashMap<String, Any?> = HashMap()
                            hashMap["status"] = "verifikasi_pengembalian_uang"
                            val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
                            ref.child(idPesanan)
                                .updateChildren(hashMap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Menunggu verifikasi pengembalian uang", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                }
                            dialog.dismiss()
                        }
                }
                .setNegativeButton("Batalkan", null)
                .create()
            inputDialog.show()
        }
        val dialog = builder.create()
        dialog.show()
    }

    // Mode Penjual
    private fun kirimBuktiPengiriman(context: Context, idPesanan : String) {
        val intent = Intent(context, KirimBuktiPengirimanBarang::class.java)
        intent.putExtra("id_pesanan", idPesanan)
        context.startActivity(intent)
    }
    private fun konfirmasiPenjual(context: Context, idPesanan : String, id_pengiriman_barang:String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Konfirmasi Barang Telah Tiba Ke Pembeli")
        builder.setMessage("Apakah barang yang Anda kirim sudah tiba di lokasi alamat pembeli yang dituju?")
        builder.setNeutralButton("Detail") { dialog, _ ->
            val intent = Intent(context, DetailBuktiPengirimanBarang::class.java)
            intent.putExtra("id_pengiriman_barang", id_pengiriman_barang)
            intent.putExtra("id_pesanan", idPesanan)
            context.startActivity(intent)
            dialog.dismiss()
        }
        builder.setPositiveButton("Ya") { dialog, which ->
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["status"] = "konfirmasi_pembeli"
            val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
            ref.child(idPesanan).updateChildren(hashMap)
            dialog.dismiss()
        }
        builder.setNegativeButton("Belum") { dialog, which ->
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["status"] = "dibatalkan"
            val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
            ref.child(idPesanan).updateChildren(hashMap)
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    // Mode Admin
    private fun verifikasiPembayaran(context: Context, idPesanan : String, id_transaksi:String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Verifikasi Pembayaran")
        builder.setMessage("Apakah Pembayaran Sudah Lunas?")
        // Tombol Menuju Activity DetailBuktiPengiriman
        builder.setNeutralButton("Detail") { dialog, _ ->
            // Buka Activity DetailBuktiPengiriman
            val intent = Intent(context, DetailBuktiPembayaranManual::class.java)
            intent.putExtra("id_transaksi", id_transaksi) // Kirim ID Pesanan
            context.startActivity(intent)
            dialog.dismiss()
        }
        builder.setPositiveButton("Terima") { dialog, which ->
            // Lakukan sesuatu jika barang sudah tiba dengan baik
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["status"] = "kirim_barang"
            val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
            ref.child(idPesanan).updateChildren(hashMap)
            dialog.dismiss()
        }
        builder.setNegativeButton("Tolak") { dialog, which ->
            Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
            // Lakukan sesuatu jika barang sudah tiba dengan baik
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["status"] = "dibatalkan"
            val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
            ref.child(idPesanan).updateChildren(hashMap)
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
    private fun verifikasiBuktiPengiriman(context: Context, idPesanan : String, id_pengiriman_barang:String){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Verifikasi Bukti Pengiriman")
        builder.setMessage("Pilih tindakan untuk bukti pengiriman ini.")
        // Tombol Menuju Activity DetailBuktiPengiriman
        builder.setNeutralButton("Detail") { dialog, _ ->
            // Buka Activity DetailBuktiPengiriman
            val intent = Intent(context, DetailBuktiPengirimanBarang::class.java)
            intent.putExtra("id_pengiriman_barang", id_pengiriman_barang)
            intent.putExtra("id_pesanan", idPesanan)// Kirim ID Pesanan
            context.startActivity(intent)
            dialog.dismiss()
        }
        // Tombol Verifikasi
        builder.setPositiveButton("Terima") { dialog, _ ->
            // Lakukan proses verifikasi
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["status"] = "konfirmasi_penjual"
            val ref = FirebaseDatabase.getInstance().getReference("Pesanan").child(idPesanan)
            ref.updateChildren(hashMap)
            dialog.dismiss()
        }
        // Tombol Tolak
        builder.setNegativeButton("Tolak") { dialog, _ ->
            // Lakukan proses penolakan
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["status"] = "dibatalkan"
            val ref = FirebaseDatabase.getInstance().getReference("Pesanan").child(idPesanan)
            ref.updateChildren(hashMap)
            dialog.dismiss()
        }
        // Tampilkan Dialog
        builder.create().show()
    }
    private fun batalkan(context: Context, idPesanan : String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Batalkan Proses")
        builder.setMessage("Konfirmasi Pembatalan Transaksi")
        builder.setPositiveButton("Ya") { dialog, which ->
            // Lakukan sesuatu jika barang sudah tiba dengan baik
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["status"] = "dibatalkan"
            val ref = FirebaseDatabase.getInstance().getReference("Pesanan").child(idPesanan)
            ref.updateChildren(hashMap)
            dialog.dismiss()
        }
        builder.setNegativeButton("Tidak") { dialog, which ->
            Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
    private fun verifikasiPengembalianUang(context: Context, idPesanan : String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Verifikasi Pengembalian Uang")
        builder.setMessage("Konfirmasi Pengembalian Uang Kepada Pembeli")
        builder.setPositiveButton("Ya") { dialog, which ->
            // Lakukan sesuatu jika barang sudah tiba dengan baik
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["status"] = "refund"
            val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
            ref.child(idPesanan).updateChildren(hashMap)
            dialog.dismiss()
        }
        builder.setNegativeButton("Tidak") { dialog, which ->
            Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
            // Lakukan sesuatu jika barang sudah tiba dengan baik
            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["status"] = "dibatalkan"
            val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
            ref.child(idPesanan).updateChildren(hashMap)
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    // bukti pengiriman barang dapat dilihat oleh pembeli, penjual, dan admin
    private fun lihatDetailBuktiPengiriman(context: Context, idPesanan : String, id_pengiriman_barang:String){
        // Buka Activity DetailBuktiPengiriman
        val intent = Intent(context, DetailBuktiPengirimanBarang::class.java)
        intent.putExtra("id_pengiriman_barang", id_pengiriman_barang)
        intent.putExtra("id_pesanan", idPesanan)// Kirim ID Pesanan
        context.startActivity(intent)
    }
}