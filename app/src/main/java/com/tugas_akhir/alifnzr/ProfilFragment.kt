package com.tugas_akhir.alifnzr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.admin.DaftarLaporan.DaftarLaporanMasalahActivity
import com.tugas_akhir.alifnzr.databinding.FragmentProfilBinding
import com.tugas_akhir.alifnzr.databinding.LaporkanMasalahBinding

class ProfilFragment : Fragment() {

    private lateinit var binding : FragmentProfilBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private val auth = FirebaseAuth.getInstance()
    private var akun = ""
    private var type_akun = ""
    private var deskripsi = ""
    private var jenis_laporan = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        binding = FragmentProfilBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        loadDataAkun()

        // admin
        binding.daftarLaporan.setOnClickListener {
            daftarLaporanMasalah()
        }

        binding.konfigurasiPembayaran.setOnClickListener {
            konfigurasiPembayaran()
        }

        // user
        binding.laporkan.setOnClickListener {
            laporkan(requireContext())
        }

        // user
        binding.editProfil.setOnClickListener{
            editProfil()
        }

        binding.ubahPassword.setOnClickListener {
            startActivity(Intent(context, UbahPassword::class.java))
        }

        // user
        binding.hapusAkun.setOnClickListener{
            hapusAkun()
        }

        binding.logoutCv.setOnClickListener {
            logOut()
        }

        cekTypeAkun()
    }



    private fun loadDataAkun() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("nama").value}"
                    val phoneNumber = "${snapshot.child("nomor_telepon").value}"
                    val alamat = "${snapshot.child("alamat").value}"
                    val profileImageUrl = "${snapshot.child("foto_profil").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    var bank = "${snapshot.child("bank").value}"
                    var noRekening = "${snapshot.child("no_rekening").value}"
                    var jenis_akun = "${snapshot.child("type_akun").value}"
                    val phone = phoneNumber

                    if (timestamp == "null"){
                        timestamp = "0"
                    }

                    binding.emailTv.text = email
                    binding.namaTv.text = name
                    binding.phoneTv.text = phone
                    binding.alamatTV.text = alamat
                    binding.typeAkun.text = jenis_akun
                    binding.bank.text = bank
                    binding.rekening.text = noRekening

                    // upload foto profil
                    try {
                        Glide.with(mContext).load(profileImageUrl)
                            .placeholder(R.drawable.akun)
                            .into(binding.profileIv)

                        // Tambahkan klik listener untuk membuka tampilan penuh
                        binding.profileIv.setOnClickListener {
                            val intent = Intent(mContext, FullGambarActivity::class.java)
                            intent.putExtra("IMAGE_URL", profileImageUrl)
                            startActivity(intent)
                        }
                    } catch (e:Exception){
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun cekTypeAkun() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.uid)
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("type_akun").getValue(String::class.java)

                    when (userType) {
                        "admin" -> {
                            // Sembunyikan elemen lainnya
                            binding.hapusAkun.visibility = GONE
                            binding.laporkan.visibility = GONE
                            binding.labelBank.visibility = GONE
                            binding.bank.visibility = GONE
                            binding.labelRekening.visibility = GONE
                            binding.rekening.visibility = GONE
                            binding.labelAlamat.visibility = GONE
                            binding.alamatTV.visibility = GONE
                        }

                        "penjual" -> {
                            // Sembunyikan
                            binding.daftarLaporan.visibility = GONE
                            binding.konfigurasiPembayaran.visibility = GONE
                        }

                        "pembeli" -> {
                            // Sembunyikan yang tidak diperlukan
                            binding.daftarLaporan.visibility = GONE
                            binding.konfigurasiPembayaran.visibility = GONE
                            binding.labelBank.visibility = GONE
                            binding.bank.visibility = GONE
                            binding.labelRekening.visibility = GONE
                            binding.rekening.visibility = GONE
                        }

                        else -> {
                            // Jika tipe akun tidak diketahui, semua disembunyikan
                            binding.root.visibility = GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Tangani kesalahan jika perlu
                }
            })
        }
    }

    private fun daftarLaporanMasalah(){
        val intent = Intent(mContext, DaftarLaporanMasalahActivity::class.java)
        startActivity(intent)
    }

    private fun konfigurasiPembayaran(){
        val intent = Intent(mContext, KonfigurasiPembayaran::class.java)
        startActivity(intent)
    }

    private fun laporkan(context: Context) {
        val binding : LaporkanMasalahBinding = LaporkanMasalahBinding.inflate(LayoutInflater.from(context))

        firebaseAuth = FirebaseAuth.getInstance()

        val dataAkun = FirebaseDatabase.getInstance().getReference("Users")
        dataAkun.ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val namaAkun = snapshot.child("email").value.toString()
                    val typeAkun = snapshot.child("type_akun").value.toString()
                    akun = namaAkun
                    type_akun = typeAkun
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        val jenisLaporan = arrayOf(
            "Akun Diretas",
            "Masalah Pembayaran",
            "Penipuan",
            "Melaporkan Bug Error",
            "Pengembalian Uang",
            "lainnya"
        )
        val adapterJenisLaporan = ArrayAdapter(context, R.layout.row_jenis_barang, jenisLaporan)
        binding.jenisLaporan.setAdapter(adapterJenisLaporan)

        val inputDialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setView(binding.root)
            .setPositiveButton("Kirim"){_,_->

                // input teks
                jenis_laporan = binding.jenisLaporan.text.toString().trim()
                deskripsi = binding.inputLaporan.text.toString().trim()

                val data = FirebaseDatabase.getInstance().getReference("Laporan")
                val keyId = data.push().key
                val hashMap: HashMap<String, Any?> = HashMap()
                hashMap["id_laporan_masalah"] = keyId
                hashMap["akun"] = akun
                hashMap["type_akun"] = type_akun
                hashMap["uid"] = firebaseAuth.uid
                hashMap["jenis_laporan"] = jenis_laporan
                hashMap["deskripsi_masalah"] = deskripsi
                hashMap["timestamp"] = System.currentTimeMillis()
                data.child(keyId!!).setValue(hashMap)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Laporan Berhasil Dikirim", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batalkan", null)
            .create()
        inputDialog.show()
    }

    private fun editProfil() {
        val konteks = mContext
        val kelas = EditProfilActivity::class.java
        val a = Intent(konteks, kelas)
        startActivity(a)
    }

    private fun hapusAkun(){
        val konteks = mContext
        val kelas = HapusAkunActivity::class.java
        val inten =  Intent(konteks, kelas)
        startActivity(inten)
    }

    private fun logOut() {
        firebaseAuth.signOut()
        startActivity(Intent(mContext, LoginActivity::class.java))
        activity?.finishAffinity()
    }
}