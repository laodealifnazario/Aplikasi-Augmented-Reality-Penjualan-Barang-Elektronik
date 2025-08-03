@file:Suppress("DEPRECATION")

package com.tugas_akhir.alifnzr.DetailBarang

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.tugas_akhir.alifnzr.Diskusi.DetailDiskusiActivity
import com.tugas_akhir.alifnzr.JualBarang.JualBarang
import com.tugas_akhir.alifnzr.LihatARbarang
import com.tugas_akhir.alifnzr.MainActivity
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.admin.ModelBarang
import com.tugas_akhir.alifnzr.databinding.ActivityBarangDetailBinding
import com.tugas_akhir.alifnzr.databinding.DialogOrderBinding
import java.util.ArrayList

class DetailBarang : AppCompatActivity() {
    private lateinit var binding: ActivityBarangDetailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var id_barang = ""
    private var nama_barang = ""
    private var uid_penjual = ""
    private var telepon_penjual = ""
    private var harga_barang= ""
    private lateinit var imageSliderArrayList : ArrayList<ModelGambarSlider>
    private lateinit var objek3DPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarangDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        // menerima data dari AdapterBerandaPenjual.kt
        id_barang = intent.getStringExtra("id_barang").toString()

        loadGambarBarang()
        loadDetailBarang()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // tombol hapus
        binding.toolbarHapusBtn.setOnClickListener {
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            materialAlertDialogBuilder.setTitle("Hapus Produk")
                .setMessage("Apakah anda ingin menghapus Produk ini?")
                .setPositiveButton("Hapus") { dialog, which->
                    hapusBarang()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish() // Menutup aktivitas saat ini agar kembali ke MainActivity
                }
                .setNegativeButton("Batalkan") {dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

        // tombol chat
        binding.tombolDiskusi.setOnClickListener{
            diskusi()
        }

        // tombol edit
        binding.toolbarEditBtn.setOnClickListener {
            editOptionsDialog()
        }

        // Lihat AR
        binding.lihatAR.setOnClickListener {
            lihatAR()
        }

        // Pesan Barang
        binding.beli.setOnClickListener {
            beliBarang(this)
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Progress Dialog untuk unggah objek 3D
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mengunggah...")
        progressDialog.setMessage("Mohon tunggu, sedang mengunggah objek 3D.")
        progressDialog.setCanceledOnTouchOutside(false)
        objek3DPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val fileUri = data?.data // URI file yang dipilih
                if (fileUri != null) {
                    // Tampilkan dialog konfirmasi sebelum unggah
                    dialogUnggahFile(fileUri)
                } else {
                    Toast.makeText(this, "Tidak ada file yang dipilih.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Tombol Unggah 3D AR
        binding.unggah3DAR.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/octet-stream" // Tipe file (misalnya .glb atau .obj)
            objek3DPickerLauncher.launch(intent)
        }

        cekTypeAkun()
    }

    private fun loadDetailBarang() {
        val ref = FirebaseDatabase.getInstance().getReference("Barang")
        ref.child(id_barang)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val modelBarang = snapshot.getValue(ModelBarang::class.java)
                        uid_penjual = "${modelBarang!!.uid}"
                        val namaBarang = modelBarang.nama_barang
                        val deskripsi = modelBarang.deskripsi
                        val jenisBarang = modelBarang.jenis_barang
                        val harga = modelBarang.harga
                        nama_barang = namaBarang
                        harga_barang = harga.toString()
                        binding.namaBarangTv.text = namaBarang
                        binding.jenisBarangTv.text = jenisBarang
                        binding.deskripsiTv.text = deskripsi
                        binding.hargaTv.text = harga.toString()
                        val ref = FirebaseDatabase.getInstance().getReference("Users")
                        ref.child(uid_penjual)
                            .addValueEventListener(object:ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val name = "${snapshot.child("nama").value}"
                                    val email = "${snapshot.child("email").value}"
                                    val telepon = "${snapshot.child("nomor_telepon").value}"
                                    val profileImageUrl = "${snapshot.child("foto_profil").value}"
                                    telepon_penjual = "$telepon"
                                    binding.namaPenjualTv.text = name
                                    binding.emailTV.text = email
                                    binding.nomorTelepon.text = telepon
                                    try {
                                        Glide.with(this@DetailBarang)
                                            .load(profileImageUrl)
                                            .placeholder(R.drawable.akun)
                                            .into(binding.fotoProfilPenjual)
                                    } catch (e:Exception) {
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                    } catch (e : Exception){ }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadGambarBarang() {
        imageSliderArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Barang")
        ref.child(id_barang).child("Images")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    imageSliderArrayList.clear()
                    for (ds in snapshot.children){
                        try {
                            val modelImageSlider = ds.getValue(ModelGambarSlider::class.java)
                            imageSliderArrayList.add(modelImageSlider!!)
                        } catch (e : Exception){
                        }
                    }
//                    val adapterImageSlider = AdapterGambarSlider(this@ProdukDetailActivity, imageSliderArrayList)
                    binding.gambarSliderVp.adapter = AdapterGambarSlider(this@DetailBarang, imageSliderArrayList)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun beliBarang(context: Context) {
        val binding: DialogOrderBinding = DialogOrderBinding.inflate(LayoutInflater.from(context))

        // Referensi ke Firebase untuk data barang
        val refBarang = FirebaseDatabase.getInstance().getReference("Barang")

        // Referensi ke Firebase untuk data biaya (PPN dan Layanan)
        val refBiaya = FirebaseDatabase.getInstance().getReference("config/biaya")

        // Ambil data biaya (PPN dan Layanan) dari Firebase terlebih dahulu
        refBiaya.get().addOnSuccessListener { biayaSnapshot ->
            if (biayaSnapshot.exists()) {
                // Ambil nilai biayaPPN dan biayaLayananSistem secara manual
                val biayaPPN = biayaSnapshot.child("biayaPPN").getValue(Int::class.java) ?: 0
                val biayaLayananSistem = biayaSnapshot.child("biayaLayananSistem").getValue(Int::class.java) ?: 0

                // Log data untuk memastikan nilai yang diterima
                Log.d("Firebase", "biayaPPN: $biayaPPN, biayaLayananSistem: $biayaLayananSistem")

                // Ambil data barang
                refBarang.child(id_barang).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val modelBarang = snapshot.getValue(ModelBarang::class.java)
                            if (modelBarang != null) {
                                val harga = modelBarang.harga
                                // Update harga barang
                                binding.hargaBarang.text = harga.toString()
                                // Hitung total harga
                                val totalHarga = harga + biayaPPN + biayaLayananSistem
                                binding.totalBayar.text = totalHarga.toString()

                                // Menyimpan total harga ke dalam dialog konfirmasi pesanan
                                dialogKonfirmasiBeli(context, totalHarga, biayaPPN, biayaLayananSistem)
                            }
                        } catch (e: Exception) {
                            Log.e("BeliBarang", "Error: ${e.message}")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("BeliBarang", "onCancelled: ${error.message}")
                    }
                })
            } else {
                Log.e("Firebase", "Path config/biaya tidak ditemukan")
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Gagal mengambil data biaya: ${exception.message}")
        }
    }

    private fun dialogKonfirmasiBeli(a:Context, totalHarga:Int, biayaPPN:Int, biayaLayananSistem:Int) {
        val binding: DialogOrderBinding = DialogOrderBinding.inflate(LayoutInflater.from(a))

        // Set total harga di dalam dialog konfirmasi pesanan
        binding.hargaBarang.text = harga_barang
        binding.biayaPPN.text = biayaPPN.toString()
        binding.biayaLayananSistem.text = biayaLayananSistem.toString()
        binding.totalBayar.text = totalHarga.toString()

        val konfirmasiBeli = AlertDialog.Builder(a)
            .setView(binding.root)
            .setPositiveButton("Order") { _, _ ->
                val timestamp = System.currentTimeMillis()
                val refAds = FirebaseDatabase.getInstance().getReference("Pesanan")
                val keyId = refAds.push().key

                // mengatur data untuk ditambahkan ke database
                val hashMap: HashMap<String, Any?> = HashMap()
                hashMap["id_pesanan"] = keyId
                hashMap["id_barang"] = id_barang
                hashMap["uid_pembeli"] = firebaseAuth.uid
                hashMap["uid_penjual"] = uid_penjual
//                hashMap["nama_barang"] = nama_barang
                hashMap["total_harga"] = totalHarga
                hashMap["status"] = "bayar"
                hashMap["timestamp"] = timestamp
                hashMap["alamat_pembeli"] = ""
                hashMap["latitude_pembeli"] = ""
                hashMap["longtitude_pembeli"] = ""
                hashMap["latitude_penjual"] = ""
                hashMap["longtitude_penjual"] = ""
                refAds.child(keyId!!).setValue(hashMap)
            }
            .setNegativeButton("Batal", null)
            .create()
        konfirmasiBeli.show()
    }

    private fun dialogUnggahFile(fileUri: Uri) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Konfirmasi Unggah")
        dialogBuilder.setMessage("Apakah Anda ingin mengunggah file ini?")
        dialogBuilder.setPositiveButton("Unggah") { _, _ ->
            // Jika pengguna mengonfirmasi, unggah file
            if (id_barang.isNotEmpty()) {
                unggahFileObject3D(fileUri, id_barang)
            } else {
                Toast.makeText(this, "ID barang tidak valid.", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun unggahFileObject3D(fileUri: Uri, objekId: String) {
        progressDialog.show()
        val storageRef = FirebaseStorage.getInstance().getReference("Objek3D/$objekId")
        storageRef.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                try {
                    // Ambil URL unduhan setelah berhasil mengunggah
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                        try {
                            // Persiapkan data untuk disimpan ke Realtime Database
                            val dataMap = HashMap<String, Any>()
                            dataMap["id"] = objekId
                            dataMap["objekUrl"] = downloadUrl.toString()
                            // Simpan data ke Realtime Database di node sesuai id_barang
                            val databaseRef = FirebaseDatabase.getInstance().getReference("Barang")
                            databaseRef.child(objekId).child("Objek3D").setValue(dataMap)
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    Log.d("DetailBarang", "3D object uploaded and saved: $downloadUrl")
                                    Toast.makeText(this, "3D object berhasil diunggah!", Toast.LENGTH_SHORT).show()
                                    periksaObjek3D()
                                }
                                .addOnFailureListener { e ->
                                    progressDialog.dismiss()
                                    Log.e("DetailBarang", "Gagal menyimpan URL ke database: ${e.message}")
                                    Toast.makeText(this, "Gagal menyimpan ke database: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } catch (e: Exception) {
                            progressDialog.dismiss()
                            Log.e("DetailBarang", "Error saat menyusun dataMap atau menyimpan ke database: ${e.message}")
                            Toast.makeText(this, "Terjadi kesalahan saat menyimpan data.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    Log.e("DetailBarang", "Error saat mengambil download URL: ${e.message}")
                    Toast.makeText(this, "Terjadi kesalahan saat mengambil URL unduhan.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.e("DetailBarang", "Gagal mengunggah file: ${e.message}")
                Toast.makeText(this, "Gagal mengunggah file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun lihatAR() {
        val intent = Intent(this, LihatARbarang::class.java)
        intent.putExtra("id_barang", id_barang)
        startActivity(intent)
    }

    private fun diskusi(){
        val intent = Intent(this, DetailDiskusiActivity::class.java)
        intent.putExtra("receiptUid", uid_penjual)
        startActivity(intent)
    }

    private fun cekTypeAkun() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("type_akun").value.toString()
                    if (userType == "admin") {
                        // Hilangkan tombol Edit dan Beli
                        binding.toolbarEditBtn.visibility = View.GONE
                        binding.beli.visibility = View.GONE
                        periksaObjek3D()
                    }
                    else if (userType == "penjual"){
                        // Hilangkan tombol Beli, Diskusi, profil, dan Unggah 3D AR
                        binding.beli.visibility = View.GONE
                        binding.tombolDiskusi.visibility = View.GONE
                        binding.sellerProfileCv.visibility = View.GONE
                        binding.profilPenjualLabel.visibility = View.GONE
                        binding.unggah3DAR.visibility = View.GONE
                        // Cek apakah objek 3D ada pada barang
                        val ref = FirebaseDatabase.getInstance().getReference("Barang")
                        ref.child(id_barang).child("Objek3D").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Cek apakah ada data Objek3D
                                if (snapshot.exists()) {
                                    // Jika objek 3D ada, tampilkan tombol "Lihat AR"
                                    binding.lihatAR.visibility = View.VISIBLE
                                } else {
                                    // Jika objek 3D tidak ada, tampilkan tombol "Unggah 3D AR"
                                    binding.lihatAR.visibility = View.GONE
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                    else if (userType == "pembeli"){
                        // Hilangkan tombol Edit, Hapus, dan Unggah 3D AR
                        binding.toolbarEditBtn.visibility = View.GONE
                        binding.toolbarHapusBtn.visibility = View.GONE
                        binding.toolbarTitleTv.text = "Detail Barang"
                        binding.unggah3DAR.visibility = View.GONE
                    }
                    else{}
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun hapusBarang() {
        try {
            val databaseRef = FirebaseDatabase.getInstance().getReference("Barang")
            val storageRef = FirebaseStorage.getInstance().reference
            val barangRef = databaseRef.child(id_barang)

            // Ambil data gambar dari node Images
            barangRef.child("Images").get().addOnSuccessListener { dataSnapshot ->
                try {
                    val deleteTasks = mutableListOf<Task<Void>>()
                    // Hapus semua file gambar dari Storage
                    for (imageSnapshot in dataSnapshot.children) {
                        val imageId = imageSnapshot.child("id").value.toString()
                        val imageStorageRef = storageRef.child("Barang/$imageId")
                        val deleteTask = imageStorageRef.delete()
                        deleteTasks.add(deleteTask)
                    }
                    // Tunggu semua gambar dihapus sebelum menghapus data barang
                    Tasks.whenAllComplete(deleteTasks).addOnCompleteListener {
                        try {
                            // Hapus data barang dari database
                            barangRef.removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Barang dan gambar berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Gagal menghapus data dari database", Toast.LENGTH_SHORT).show()
                                }
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error saat menghapus data barang: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error saat menghapus gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data gambar", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editOptionsDialog() {
        val popupMenu = PopupMenu(this, binding.toolbarEditBtn)
        popupMenu.menu.add(Menu.NONE,0,0,"Edit")
        popupMenu.menu.add(Menu.NONE,1,1,"Tandai Sudah Laku")
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val itemId = menuItem.itemId
            if (itemId == 0) {
                val intent = Intent(this, JualBarang::class.java)
                intent.putExtra("isEditMode", true)
                intent.putExtra("id_barang", id_barang)
                startActivity(intent)
            }
            else if (itemId == 1) {
                val alertDialogBuilder = MaterialAlertDialogBuilder(this)
                alertDialogBuilder.setTitle("Tandai yang sudah Laku")
                    .setMessage("Apakah anda ingin menandai Produk ini yang sudah laku?")
                    .setPositiveButton("Laku"){ dialog, which ->
                        val hashMap = HashMap<String,Any>()
                        hashMap["status"] = "Laku Terjual"

                        val ref = FirebaseDatabase.getInstance().getReference("Barang")
                        ref.child(id_barang).updateChildren(hashMap)
                    }
                    .setNegativeButton("Batalkan"){dialog, which->
                        dialog.dismiss()
                    }
                    .show()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun periksaObjek3D() {
        // Cek apakah objek 3D ada pada barang
        val ref = FirebaseDatabase.getInstance().getReference("Barang")
        ref.child(id_barang).child("Objek3D").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Cek apakah ada data Objek3D
                if (snapshot.exists()) {
                    // Jika objek 3D ada, tampilkan tombol "Lihat AR"
                    binding.lihatAR.visibility = View.VISIBLE
                    binding.unggah3DAR.visibility = View.GONE
                } else {
                    // Jika objek 3D tidak ada, tampilkan tombol "Unggah 3D AR"
                    binding.lihatAR.visibility = View.GONE
                    binding.unggah3DAR.visibility = View.VISIBLE
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}