package com.tugas_akhir.alifnzr

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.tugas_akhir.alifnzr.JualBarang.AdapterGambarPicked
import com.tugas_akhir.alifnzr.JualBarang.ModelGambarPicked
import com.tugas_akhir.alifnzr.databinding.ActivityKirimBuktiPengirimanBarangBinding
import com.tugas_akhir.alifnzr.lokasi.InputLokasi

class KirimBuktiPengirimanBarang : AppCompatActivity() {

    private lateinit var binding : ActivityKirimBuktiPengirimanBarangBinding
    private lateinit var progressDialog : ProgressDialog
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var imagePickedArrayList : ArrayList<ModelGambarPicked>
    private lateinit var adapterGambarPicked: AdapterGambarPicked
    private var imageUri: Uri? = null
    private var barangIdForEditing = ""
    private var nama_pengirim = ""
    private var jasa_ekspedisi = ""
    private var no_resi = ""
    private var alamatPenjual = ""
    private var id_pesanan = ""
    private var latitude = 0.0
    private var longtitude = 0.0
    private var alamat = ""
    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        var areAllGranted = true
        for (isGranted in result.values) {
            areAllGranted = areAllGranted && isGranted
        }
        if (areAllGranted){
            pickImageCamera()
        }
        else {
            Toast.makeText(this, "kamera gagal...", Toast.LENGTH_SHORT).show()
        }
    }
    private val cameraActivityResultLauncher =  registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val timetamp = System.currentTimeMillis()
            val modelImagePicked = ModelGambarPicked(timetamp.toString(),imageUri,null, false)
            imagePickedArrayList.add(modelImagePicked)
            loadGambar()
        }
        else {
            // dibatalkan
            Toast.makeText(this, "Batal...", Toast.LENGTH_SHORT).show()
        }
    }
    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            pickImageGalery()
        }
        else {
            Toast.makeText(this, "Device gagal...", Toast.LENGTH_SHORT).show()
        }
    }
    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Check if image is picked or not
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            imageUri = data!!.data // Uri adalah identifikasi alamat referensi tempat file berada di dalam penyimpanan Komputer/Mobile
            val timestamp = System.currentTimeMillis()
            // Menyimpan data gambar sementara dari pick galeri ke dalam kelas ModelGambarPicked.Kt
            imagePickedArrayList.add(ModelGambarPicked(timestamp.toString(), imageUri, null, false))
            loadGambar()
        }
        else {
            // dibatalkan
            Toast.makeText(this, "...", Toast.LENGTH_SHORT).show()
        }
    }
    private val locationPickerActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode==Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                latitude = data.getDoubleExtra("latitude",0.0)
                longtitude = data.getDoubleExtra("longtitude",0.0)
                alamat = data.getStringExtra("alamat") ?: ""
                binding.alamatPenjual.setText(alamat)
            }
            else {
                Toast.makeText(this, "Batal...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKirimBuktiPengirimanBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // tombol kembali ke Fragment
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        // inisialisasi firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        id_pesanan = intent.getStringExtra("id_pesanan").toString()

        loadAlamat()

        // progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon tunggu")
        progressDialog.setCanceledOnTouchOutside(false)

        val jasa_ekspedisi = arrayOf(
            "JNE Express",
            "TIKI (Titipan Kilat)",
            "POS Indonesia",
            "J&T Express",
            "SiCepat Ekspres",
            "Ninja Xpress",
            "Lion Parcel",
            "GoSend (GoJek)",
            "GrabExpress",
        )
        val adapterJasaEkspedisi = ArrayAdapter(this, R.layout.row_jenis_barang, jasa_ekspedisi)
        binding.inputJasaEkspedisi.setAdapter(adapterJasaEkspedisi)

        imagePickedArrayList = ArrayList()
        loadGambar()

        // tombol opsi input kamera
        binding.btnGambar.setOnClickListener{
            showImagePickOptions()
        }

        binding.btnKirim.setOnClickListener{
            validateData()
        }

        binding.alamatPenjual.setOnClickListener {
            val intent = Intent(this, InputLokasi::class.java)
            locationPickerActivityResultLauncher.launch(intent)
        }

    }

    private fun loadAlamat(){
        val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
        ref.child(id_pesanan)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val alamatPengirim = snapshot.child("alamat_pembeli").value.toString()
                    binding.alamatPengiriman.text = alamatPengirim
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun validateData() {
        // input data dari xml
        nama_pengirim = binding.inputNamaPengirim.text.toString().trim()
        jasa_ekspedisi = binding.inputJasaEkspedisi.text.toString().trim()
        no_resi = binding.inputNoResi.text.toString().trim()
        alamatPenjual = binding.alamatPenjual.text.toString().trim()

        // validasi data
        if (nama_pengirim.isEmpty()) {
            binding.inputNamaPengirim.error = "Masukkan Nama Pengirim"
            binding.inputNamaPengirim.requestFocus()
        }
        else if (jasa_ekspedisi.isEmpty()) {
            binding.inputJasaEkspedisi.error = "Pilih Jasa Ekspedisi"
            binding.inputJasaEkspedisi.requestFocus()
        }
        else if (no_resi.isEmpty()) { // Validasi harga tidak boleh negatif atau nol
            binding.inputNoResi.error = "Masukkan Harga yang valid"
            binding.inputNoResi.requestFocus()
        }
        else if (alamatPenjual.isEmpty()) { // Validasi harga tidak boleh negatif atau nol
            binding.alamatPenjual.error = "Masukkan Harga yang valid"
            binding.alamatPenjual.requestFocus()
        }
        else {
            binding.inputNamaPengirim.setText("")
            binding.inputNoResi.setText("")
            binding.inputJasaEkspedisi.setText("")
            binding.alamatPenjual.setText("")
            kirimBukti()
        }
    }

    private fun kirimBukti() {
        progressDialog.setMessage("Mohon Tunggu...")
        progressDialog.show()
        val timestamp = System.currentTimeMillis()
        val refAds = FirebaseDatabase.getInstance().getReference("Bukti_Pengiriman")
        val keyId = refAds.push().key
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["id_pengiriman_barang"] = "$keyId"
        hashMap["id_pesanan"] = id_pesanan
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["nama_pengirim"] = "$nama_pengirim"
        hashMap["jasa_ekspedisi"] = "$jasa_ekspedisi"
        hashMap["nomor_resi"] = no_resi
        hashMap["timestamp"] = timestamp
        refAds.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                val hashMap: HashMap<String, Any?> = HashMap()
                hashMap["status"] = "verifikasi_bukti_pengiriman"
                hashMap["alamat_penjual"] = alamatPenjual
                hashMap["latitude_penjual"] = latitude
                hashMap["longtitude_penjual"] = longtitude
                hashMap["id_pengiriman_barang"] = "$keyId"
                val ref = FirebaseDatabase.getInstance().getReference("Pesanan")
                ref.child(id_pesanan)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        uploadGambar(keyId)
                    }
            }
    }

    private fun uploadGambar(keyId: String) {
        var uploadSuccessCount = 0

        try {
            for (i in imagePickedArrayList.indices) {
                val modelImagePicked = imagePickedArrayList[i]
                if (!modelImagePicked.fromInternet) {
                    val imageName = modelImagePicked.id
                    val imageIndexForProgress = i + 1
                    val storageReference = FirebaseStorage.getInstance().getReference("Bukti_Pengiriman/$imageName")

                    storageReference.putFile(modelImagePicked.imageUri!!)
                        .addOnProgressListener { snapshot ->
                            val progress = 100 * snapshot.bytesTransferred / snapshot.totalByteCount
                            val message = "Sedang Memproses... ${progress.toInt()}%"
                            progressDialog.setMessage(message)
                            progressDialog.show()
                        }
                        .addOnSuccessListener { taskSnapshot ->
                            val uriTask = taskSnapshot.storage.downloadUrl
                            while (!uriTask.isSuccessful);
                            val uploadedImageUrl = uriTask.result
                            if (uriTask.isSuccessful) {
                                val hashMap = HashMap<String, Any>()
                                hashMap["id"] = modelImagePicked.id
                                hashMap["imageUrl"] = "$uploadedImageUrl"

                                val ref = FirebaseDatabase.getInstance().getReference("Bukti_Pengiriman")
                                ref.child(keyId).child("Images")
                                    .child(imageName)
                                    .updateChildren(hashMap)
                                    .addOnCompleteListener {
                                        uploadSuccessCount++
                                        // Cek apakah semua gambar sudah di-upload
                                        if (uploadSuccessCount == imagePickedArrayList.size) {
                                            progressDialog.dismiss()
                                            Toast.makeText(this, "Semua gambar berhasil diupload", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                    }
                            } else {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Gagal mendapatkan URL gambar", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(this, "Upload gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Jika gambar dari internet, tetap hitung sebagai "selesai"
                    uploadSuccessCount++
                    if (uploadSuccessCount == imagePickedArrayList.size) {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Semua gambar berhasil diproses", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showImagePickOptions() {

        // init untuk popup menu memilih kamera atau galeri
        val popupMenu = PopupMenu(this, binding.btnGambar)

        // lebel tulisan memilih kamera atau galeri
        popupMenu.menu.add(Menu.NONE,1,1,"Kamera")
        popupMenu.menu.add(Menu.NONE,2,2,"Galeri")

        // menampilkan popup Menu
        popupMenu.show()

        // menghandle pop menu ketika di klik
        popupMenu.setOnMenuItemClickListener { item ->
            //
            val itemId = item.itemId

            if (itemId == 1){
                // kamera diklik kita perlu memeriksa apakah kita memiliki izin Kamera, Penyimpanan sebelum meluncurkan Kamera untuk Mengambil gambar
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Versi perangkat adalah TIRAMISU atau lebih tinggi. Kami hanya memerlukan izin Kamera
                    val cameraPermission = arrayOf(android.Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermission)
                }
                else {
                    // Versi perangkat di bawah Tiramisu. kami memerlukan izin Kamera dan Penyimpanan
                    val cameraPermission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermission.launch(cameraPermission)
                }
            }

            else if (itemId == 2){
                // Gallery is clicked we need to check to check if we have permission of Storage before launching Gallery to Pick image
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Versi perangkat adalah TIRAMISU atau lebih tinggi. Kami tidak memerlukan izin Penyimpanan untuk meluncurkan Galeri
                    pickImageGalery()
                }
                else {
                    // Versi perangkat di bawah Tiramisu. Kami memerlukan izin Penyimpanan untuk meluncurkan Galeri
                    val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermission)
                }
            }
            true
        }
    }

    private fun loadGambar() {
        adapterGambarPicked  = AdapterGambarPicked(this,imagePickedArrayList, barangIdForEditing)
        binding.imagesRv.adapter = adapterGambarPicked
    }

    private fun pickImageCamera() {
        // Atur nilai Konten, MediaStore untuk mengambil gambar berkualitas tinggi menggunakan maksud kamera
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")

        // Uri gambar yang akan diambil dari kamera
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)

        // Intent ke kamera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGalery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        // ketika gambar dipilih, maka filenya akan di kirim menuju variabel dibawah ini
        galleryActivityResultLauncher.launch(intent)
    }
}