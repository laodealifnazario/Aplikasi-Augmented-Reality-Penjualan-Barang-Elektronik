package com.tugas_akhir.alifnzr.JualBarang

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
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.ActivityJualBarangBinding

class JualBarang : AppCompatActivity() {

    private lateinit var binding : ActivityJualBarangBinding
    private lateinit var progressDialog : ProgressDialog
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var imagePickedArrayList : ArrayList<ModelGambarPicked>
    private lateinit var adapterGambarPicked: AdapterGambarPicked
    private var isEditMode = false
    private var barangIdForEditing = ""
    private var imageUri: Uri? = null
    private var nama_barang = ""
    private var jenis_barang = ""
    private var harga = 0
    private var deskripsi = ""
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJualBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // tombol kembali ke Fragment
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // inisialisasi firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon tunggu")
        progressDialog.setCanceledOnTouchOutside(false)

        val jenis_barang = arrayOf(
            "Computer/laptop",
            "Monitor",
            "Kamera",
            "Televisi",
            "Smartphone",
            "Konsol Permainan",
        )
        val adapterJenisBarang = ArrayAdapter(this, R.layout.row_jenis_barang, jenis_barang)
        binding.inputJenisBarang.setAdapter(adapterJenisBarang)

        isEditMode = intent.getBooleanExtra("isEditMode",false)
        if (isEditMode){
            barangIdForEditing = intent.getStringExtra("id_barang") ?: ""
            loadBarangDetails()
            binding.toolbarTitleTv.text = "Edit Barang"
            binding.postingAdBtn.text = "Simpan Perubahan"
        } else{
            binding.toolbarTitleTv.text = "Jual Barang"
            binding.postingAdBtn.text = "Posting Barang"
        }

        imagePickedArrayList = ArrayList()
//        objekPickedArrayList = ArrayList()
        loadGambar()

        // tombol opsi input kamera
        binding.btnGambar.setOnClickListener{
            showImagePickOptions()
        }

        // tombol posting
        binding.postingAdBtn.setOnClickListener{
            validateData()
        }
    }

    private fun validateData() {
        // input data dari xml
        nama_barang = binding.inputNamaBarang.text.toString().trim()
        jenis_barang = binding.inputJenisBarang.text.toString().trim()
        harga = binding.inputHarga.text.toString().trim().toIntOrNull() ?: 0 // Konversi ke Integer
        deskripsi = binding.inputDeskripsi.text.toString().trim()

        // validasi data
        if (nama_barang.isEmpty()) {
            binding.inputNamaBarang.error = "Masukkan Nama Barang"
            binding.inputNamaBarang.requestFocus()
        }
        else if (jenis_barang.isEmpty()) {
            binding.inputJenisBarang.error = "Pilih Jenis Barang"
            binding.inputJenisBarang.requestFocus()
        }
        else if (harga <= 0) { // Validasi harga tidak boleh negatif atau nol
            binding.inputHarga.error = "Masukkan Harga yang valid"
            binding.inputHarga.requestFocus()
        }
        else if (deskripsi.isEmpty()) {
            binding.inputDeskripsi.error = "Masukkan Deskripsi"
            binding.inputDeskripsi.requestFocus()
        }
        else {
            if (isEditMode){
                updateBarang()
            }
            else {
                binding.inputNamaBarang.setText("")
                binding.inputJenisBarang.setText("")
                binding.inputHarga.setText("")
                binding.inputDeskripsi.setText("")
                jualBarang()
            }
        }
    }

    private fun loadGambar() {
        adapterGambarPicked  = AdapterGambarPicked(this,imagePickedArrayList, barangIdForEditing)
        binding.imagesRv.adapter = adapterGambarPicked
    }

    private fun updateBarang() {
        progressDialog.setMessage("Perbarui Produk")
        progressDialog.show()
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["nama_barang"] = "$nama_barang"
        hashMap["jenis_barang"] = "$jenis_barang"
        hashMap["harga"] = harga
        hashMap["deskripsi"] = "$deskripsi"
        val ref = FirebaseDatabase.getInstance().getReference("Barang")
        ref.child(barangIdForEditing)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                uploadGambar(barangIdForEditing)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal Memperbarui Barang...", Toast.LENGTH_SHORT).show()
            }
    }

    private fun jualBarang() {
        try {
            progressDialog.setMessage("Mohon Tunggu...")
            progressDialog.show()

            val timestamp = System.currentTimeMillis()
            val refAds = FirebaseDatabase.getInstance().getReference("Barang")
            val keyId = refAds.push().key

            val hashMap: HashMap<String, Any?> = HashMap()
            hashMap["id_barang"] = "$keyId"
            hashMap["uid"] = "${firebaseAuth.uid}"
            hashMap["nama_barang"] = "$nama_barang"
            hashMap["jenis_barang"] = "$jenis_barang"
            hashMap["harga"] = harga
            hashMap["deskripsi"] = "$deskripsi"
            hashMap["status"] = "tersedia"
            hashMap["timestamp"] = timestamp
            refAds.child(keyId!!)
                .setValue(hashMap)
                .addOnSuccessListener {
                    try {
                        progressDialog.dismiss()
                        uploadGambar(keyId)
                    } catch (e: Exception) {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Terjadi kesalahan saat memproses gambar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { exception ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Gagal menyimpan data: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
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
                        // Untuk mengupload gambar ke Firebase Storage
                        val storageReference = FirebaseStorage.getInstance().getReference("Barang/$imageName")
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
                                    while (!uriTask.isSuccessful) ; // Tunggu hingga berhasil mendapatkan URL
                                    val uploadedImageUrl = uriTask.result
                                    if (uriTask.isSuccessful) {
                                        val hashMap = HashMap<String, Any>()
                                        hashMap["id"] = modelImagePicked.id
                                        hashMap["imageUrl"] = uploadedImageUrl.toString()

                                        val ref = FirebaseDatabase.getInstance().getReference("Barang")
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
                Toast.makeText(this, "Barang berhasil dijual!", Toast.LENGTH_LONG).show()
                // Tutup activity dan kembali ke halaman sebelumnya
                finish()
            } else {
                // Jika tidak ada gambar yang dipilih
                progressDialog.dismiss()
                Toast.makeText(this, "Barang berhasil dijual tanpa gambar!", Toast.LENGTH_LONG).show()
                // Tutup activity dan kembali ke halaman sebelumnya
                finish()
            }
        } catch (e: Exception) {
            // Tangani error secara umum dalam proses upload gambar
            progressDialog.dismiss()
            Toast.makeText(this, "Terjadi kesalahan saat mengupload gambar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadBarangDetails(){
        val ref = FirebaseDatabase.getInstance().getReference("Barang")
        ref.child(barangIdForEditing)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val inputNamaBarang = "${snapshot.child("nama_barang").value}"
                    val inputJenisBarang = "${snapshot.child("jenis_barang").value}"
                    val inputHarga = "${snapshot.child("harga").value}"
                    val inputDeskripsi = "${snapshot.child("deskripsi").value}"

                    binding.inputNamaBarang.setText(inputNamaBarang)
                    binding.inputJenisBarang.setText(inputJenisBarang)
                    binding.inputHarga.setText(inputHarga)
                    binding.inputDeskripsi.setText(inputDeskripsi)

                    val refImages = snapshot.child("Images").ref
                    refImages.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children){
                                val id = "${ds.child("id").value}"
                                val imageUrl = "${ds.child("imageUrl").value}"
                                val modelImagePicked = ModelGambarPicked(id, null, imageUrl, true)
                                imagePickedArrayList.add(modelImagePicked)
                            }
                            loadGambar()
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
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