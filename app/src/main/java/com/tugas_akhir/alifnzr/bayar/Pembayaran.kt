package com.tugas_akhir.alifnzr.bayar

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.tugas_akhir.alifnzr.JualBarang.AdapterGambarPicked
import com.tugas_akhir.alifnzr.JualBarang.ModelGambarPicked
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.ActivityBayarManualBinding
import com.tugas_akhir.alifnzr.lokasi.InputLokasi


class Pembayaran : AppCompatActivity() {
    private lateinit var binding : ActivityBayarManualBinding
    private lateinit var adapterGambarPicked: AdapterGambarPicked
    private lateinit var imagePickedArrayList : ArrayList<ModelGambarPicked>
    private lateinit var progressDialog : ProgressDialog
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
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
    private var imageUri: Uri? = null
    private var nama_pemilik_rekening = ""
    private var nomor_rekening = ""
    private var bank_anda = ""
    private var bank_tujuan_transfer = ""
    private var rekening_tujuan_transfer = ""
    private var alamatPembeli = ""
    private var idPesanan = ""
    private var uidPembeli = ""
    private var harga = 0
    private var latitude = 0.0
    private var longtitude = 0.0
    private var alamat = ""
    private var barangIdForEditing = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBayarManualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon tunggu")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance() // Ini menginisialisasi FirebaseDatabase

        idPesanan = intent.getStringExtra("id_pesanan").toString()
        uidPembeli = intent.getStringExtra("uid_pembeli").toString()
        harga = intent.getIntExtra("harga",0)
        binding.inputHarga.setText(harga.toString())

        // Pilih bank anda
        val pilih_bank_anda = arrayOf("BRI", "BNI", "BCA", "Mandiri", "Lainnya")
        val adapter_bank_anda = ArrayAdapter(this, R.layout.row_jenis_bank, pilih_bank_anda)
        binding.inputBankAnda.setAdapter(adapter_bank_anda)

        // Pilih bank tujuan transfer
        myRef = database.getReference("config/biaya") // Inisialisasi reference yang sesuai
        val pilih_bank_tujuan_transfer = arrayOf("BRI", "BNI", "BCA", "Mandiri")
        val adapter_bank_tujuan_transfer = ArrayAdapter(this, R.layout.row_jenis_bank, pilih_bank_tujuan_transfer)
        binding.inputBanktujuanTransfer.setAdapter(adapter_bank_tujuan_transfer)
        binding.inputBanktujuanTransfer.setOnItemClickListener { parent, view, position, id ->
            val selectedBank = parent.getItemAtPosition(position).toString()
            updateRekeningNumber(selectedBank)
        }

        imagePickedArrayList = ArrayList()
        loadGambar()

        // tombol lokasi
        binding.alamatPembeli.setOnClickListener {
            val intent = Intent(this, InputLokasi::class.java)
            locationPickerActivityResultLauncher.launch(intent)
        }

        // tombol input gambar
        binding.btnGambar.setOnClickListener{
            showImagePickOptions()
        }

        // tombol bayar
        binding.btnBayar.setOnClickListener {
            validateData()
        }
    }

    fun updateRekeningNumber(bank: String) {
        // Mengambil data dari Firebase berdasarkan bank yang dipilih
        val bankKey = when (bank) {
            "BRI" -> "noRekeningBRI"
            "BNI" -> "noRekeningBNI"
            "BCA" -> "noRekeningBCA"
            "Mandiri" -> "noRekeningMandiri"
            else -> ""
        }
        if (bankKey.isNotEmpty()) {
            // Mengambil nomor rekening dari Firebase berdasarkan key
            myRef.child(bankKey).get().addOnSuccessListener { dataSnapshot ->
                val noRekening = dataSnapshot.value.toString()
                // Set nomor rekening ke EditText sesuai bank yang dipilih
                binding.pilihRek.setText(noRekening)
            }.addOnFailureListener {
                // Tangani error jika gagal mengambil data
                Log.e("Firebase", "Error fetching data: ${it.message}")
            }
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
                binding.alamatPembeli.setText(alamat)
            }
            else {
            }
        }
    }

    private fun validateData() {
        // input data dari xml
        bank_tujuan_transfer = binding.inputBanktujuanTransfer.text.toString().trim()
        rekening_tujuan_transfer = binding.pilihRek.text.toString().trim()
        nama_pemilik_rekening = binding.inputNamaPemilikRekening.text.toString().trim()
        nomor_rekening = binding.inputNoRekening.text.toString().trim()
        bank_anda = binding.inputBankAnda.text.toString().trim()
        alamatPembeli = binding.alamatPembeli.text.toString().trim()

        // validasi data
        if (nama_pemilik_rekening.isEmpty()) {
            binding.inputNamaPemilikRekening.error = "Masukkan Nama Pemilik Rekening"
            binding.inputNamaPemilikRekening.requestFocus()
        }
        else if (nomor_rekening.isEmpty()) {
            binding.inputNoRekening.error = "Masukkan Nomor Rekening"
            binding.inputNoRekening.requestFocus()
        }
        else if (bank_anda.isEmpty()) { // Validasi harga tidak boleh negatif atau nol
            binding.inputBankAnda.error = "Pilih Bank"
            binding.inputBankAnda.requestFocus()
        }
        else if (bank_tujuan_transfer.isEmpty()) {
            binding.inputBanktujuanTransfer.error = "Pilih Bank Tujuan Transfer"
            binding.inputBanktujuanTransfer.requestFocus()
        }
        else if (alamatPembeli.isEmpty()) {
            binding.alamatPembeli.error = "Masukkan Alamat"
            binding.alamatPembeli.requestFocus()
        }
        else {
            binding.inputBanktujuanTransfer.setText("")
            binding.pilihRek.setText("")
            binding.inputNamaPemilikRekening.setText("")
            binding.inputNoRekening.setText("")
            binding.inputBankAnda.setText("")
            binding.alamatPembeli.setText("")
            bayar()
        }
    }

    private fun bayar() {
        try {
            progressDialog.setMessage("Mohon Tunggu...")
            progressDialog.show()
            val timestamp = System.currentTimeMillis()
            val uidPembeli = firebaseAuth.uid
            if (uidPembeli == null) {
                progressDialog.dismiss()
                Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
                return
            }
            val refUsers = FirebaseDatabase.getInstance().getReference("Users").child(uidPembeli).child("email")
            refUsers.get()
                .addOnSuccessListener { dataSnapshot ->
                    val emailPembeli = dataSnapshot.getValue(String::class.java) ?: ""
                    val refTransaksi = FirebaseDatabase.getInstance().getReference("Transaksi")
                    val keyId = refTransaksi.push().key
                    val hashMap: HashMap<String, Any?> = HashMap()
                    hashMap["id_transaksi"] = keyId ?: ""
                    hashMap["id_pesanan"] = idPesanan
                    hashMap["uid"] = uidPembeli
                    hashMap["akun"] = emailPembeli
                    hashMap["nama_pemilik_rekening"] = nama_pemilik_rekening
                    hashMap["bank_asal"] = bank_anda
                    hashMap["rekening_asal"] = nomor_rekening
                    hashMap["bank_tujuan_transfer"] = bank_tujuan_transfer
                    hashMap["rekening_tujuan_transfer"] = rekening_tujuan_transfer
                    hashMap["harga"] = harga
                    hashMap["timestamp"] = timestamp
                    refTransaksi.child(keyId ?: "")
                        .setValue(hashMap)
                        .addOnSuccessListener {
                            try {
                                val updatePesanan: HashMap<String, Any?> = HashMap()
                                updatePesanan["status"] = "verifikasi_pembayaran"
                                updatePesanan["alamat_pembeli"] = alamatPembeli
                                updatePesanan["latitude_pembeli"] = latitude
                                updatePesanan["longtitude_pembeli"] = longtitude
                                updatePesanan["id_transaksi"] = keyId ?: ""
                                val refPesanan = FirebaseDatabase.getInstance().getReference("Pesanan")
                                refPesanan.child(idPesanan)
                                    .updateChildren(updatePesanan)
                                    .addOnSuccessListener {
                                        uploadGambar(keyId ?: "")
                                    }
                                    .addOnFailureListener { exception ->
                                        progressDialog.dismiss()
                                        Toast.makeText(this, "Gagal memperbarui pesanan: ${exception.message}", Toast.LENGTH_LONG).show()
                                    }
                            } catch (e: Exception) {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            progressDialog.dismiss()
                            Toast.makeText(this, "Gagal menambah transaksi: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Gagal mengambil email pembeli", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Terjadi kesalahan saat memproses pembayaran: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadGambar(keyId: String) {
        try {
            // Upload gambar hanya jika ada gambar yang dipilih
            if (imagePickedArrayList.isNotEmpty()) {
                for (i in imagePickedArrayList.indices) {
                    val modelImagePicked = imagePickedArrayList[i]
                    if (!modelImagePicked.fromInternet) {
                        val imageName = modelImagePicked.id
                        val imageIndexForProgress = i + 1
                        // untuk mengupload gambar ke Firebase Storage
                        val storageReference = FirebaseStorage.getInstance().getReference("Transaksi/$imageName")
                        storageReference.putFile(modelImagePicked.imageUri!!)
                            .addOnProgressListener { snapshot ->
                                val progress = 100 * snapshot.bytesTransferred / snapshot.totalByteCount
                                val message = "Uploading $imageIndexForProgress of ${imagePickedArrayList.size} gambar....diproses ${progress.toInt()}"
                                progressDialog.setMessage(message)
                                progressDialog.show()
                            }
                            .addOnSuccessListener { taskSnapshot ->
                                try {
                                    val uriTask = taskSnapshot.storage.downloadUrl
                                    while (!uriTask.isSuccessful);
                                    val uploadedImageUrl = uriTask.result
                                    if (uriTask.isSuccessful) {
                                        val hashMap = HashMap<String, Any>()
                                        hashMap["id"] = modelImagePicked.id
                                        hashMap["imageUrl"] = uploadedImageUrl.toString()

                                        val ref = FirebaseDatabase.getInstance().getReference("Transaksi")
                                        ref.child(keyId).child("Images")
                                            .child(imageName)
                                            .updateChildren(hashMap) // Menambah node di dalam Images
                                    }
                                } catch (e: Exception) {
                                    // Tangani error saat mengupdate URL gambar ke database
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Gagal mengupdate URL gambar: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener { exception ->
                                // Tangani error saat gagal mengupload gambar
                                progressDialog.dismiss()
                                Toast.makeText(this, "Gagal mengupload gambar: ${exception.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                // Setelah semua gambar di-upload, tutup progressDialog dan kembali ke halaman sebelumnya
                progressDialog.dismiss()
                // Beri tahu pengguna jika upload gambar selesai
                Toast.makeText(this, "Pembayaran berhasil!", Toast.LENGTH_LONG).show()
                // Tutup activity dan kembali ke halaman sebelumnya
                finish()
            } else {
                // Jika tidak ada gambar yang dipilih
                progressDialog.dismiss()
                Toast.makeText(this, "Pembayaran berhasil tanpa gambar!", Toast.LENGTH_LONG).show()
                // Tutup activity dan kembali ke halaman sebelumnya
                finish()
            }
        } catch (e: Exception) {
            // Tangani error secara umum dalam proses upload gambar
            progressDialog.dismiss()
            Toast.makeText(this, "Terjadi kesalahan saat mengupload gambar: ${e.message}", Toast.LENGTH_LONG).show()
        }
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

    private fun loadGambar() {
        adapterGambarPicked  = AdapterGambarPicked(this,imagePickedArrayList, barangIdForEditing)
        binding.imagesRv.adapter = adapterGambarPicked
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
}