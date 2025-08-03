package com.tugas_akhir.alifnzr

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.tugas_akhir.alifnzr.databinding.ActivityEditProfilBinding

class EditProfilActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEditProfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null
    private var typeAkun = ""
    private var name = ""
    private var email = ""
    private var phoneNumber = ""
    private var alamat = ""
    private var bank = ""
    private var rekening = ""

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
    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            pickImageGalery()
        }
        else {
            Toast.makeText(this, "Device gagal...", Toast.LENGTH_SHORT).show()
        }
    }
    private val cameraActivityResultLauncher =  registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.akun)
                    .into(binding.profileIv)
            } catch (e:Exception){
            }
        }
        else {
            // dibatalkan
            Toast.makeText(this, "Batal...", Toast.LENGTH_SHORT).show()
        }
    }
    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            imageUri = data!!.data
            try {
                Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.akun)
                    .into(binding.profileIv)
            } catch (e:Exception){
            }
        }
        else {
            // dibatalkan
            Toast.makeText(this, "...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon tunggu...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadProfil()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.profileImagePickFab.setOnClickListener {
            imagePickDialog()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private fun loadProfil() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object :ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("nama").value}"
                    val phoneNumber = "${snapshot.child("nomor_telepon").value}"
                    val alamat = "${snapshot.child("alamat").value}"
                    val profileImageUrl= "${snapshot.child("foto_profil").value}"
                    val bank = "${snapshot.child("bank").value}"
                    val noRekening = "${snapshot.child("no_rekening").value}"
                    val userType = snapshot.child("type_akun").getValue(String::class.java) ?: ""

                    typeAkun = userType

                    binding.emailEt.setText(email)
                    binding.nameEt.setText(name)
                    binding.phoneNumberEt.setText(phoneNumber)
                    binding.inputAlamat.setText(alamat)

                    try{
                        Glide.with(this@EditProfilActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.akun)
                            .into(binding.profileIv)
                    } catch (e:Exception){
                    }

                    if (userType == "penjual") {
                        binding.bank.setText(bank)
                        binding.rekening.setText(noRekening)
                        binding.labelRekening.visibility = View.VISIBLE
                        binding.labelbank.visibility = View.VISIBLE
                    } else {
                        binding.labelRekening.visibility = View.GONE
                        binding.labelbank.visibility = View.GONE
                    }

                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun validateData() {
        // input data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        phoneNumber = binding.phoneNumberEt.text.toString().trim()
        alamat = binding.inputAlamat.text.toString().trim()
        bank = binding.bank.text.toString().trim()
        rekening = binding.rekening.text.toString().trim()

        // jika foto kosong
        if (imageUri == null){
            updateProfil(null)
        }
        // jika foto diunggah
        else{
            uploadProfileImageStorage()
        }
    }
    
    private fun uploadProfileImageStorage() {
        val filePathAndName = "UserProfile/profile_${firebaseAuth.uid}"
        progressDialog.setMessage("Upload foto profil pengguna")
        progressDialog.show()
        val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnProgressListener { snapshot ->
                val progress = 100 * snapshot.bytesTransferred / snapshot.totalByteCount
                progressDialog.setMessage("Menunggu Proses Pembaruan Profil $progress")
            }
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = uriTask.result.toString()
                if (uriTask.isSuccessful){
                    updateProfil(uploadedImageUrl)
                }
            }
            .addOnCanceledListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal memperbarui...", Toast.LENGTH_SHORT).show()
            }
    }

//    private fun updateProfil(uploadedImageUrl: String?) {
//        progressDialog.setMessage("Memperbarui informasi user")
//        progressDialog.show()
//        val hashMap = HashMap<String,Any>()
//        hashMap["nama"] = "$name"
//        hashMap["email"] = "$email"
//        hashMap["nomor_telepon"] = "$phoneNumber"
//        hashMap["alamat"] = "$alamat"
//        if (uploadedImageUrl != null){
//            hashMap["foto_profil"] = "$uploadedImageUrl"
//        }
//        val reference = FirebaseDatabase.getInstance().getReference("Users")
//        reference.child("${firebaseAuth.uid}")
//            .updateChildren(hashMap)
//            .addOnSuccessListener {
//                progressDialog.dismiss()
//                Toast.makeText(this, "Profil Berhasil Diperbarui...", Toast.LENGTH_SHORT).show()
//                imageUri = null
//            }
//            .addOnFailureListener {
//                progressDialog.dismiss()
//                Toast.makeText(this, "Gagl Perbarui...", Toast.LENGTH_SHORT).show()
//            }
//    }

//    private fun updateProfil(uploadedImageUrl: String?) {
//        progressDialog.setMessage("Memperbarui informasi user")
//        progressDialog.show()
//        val hashMap = HashMap<String, Any>()
//        hashMap["nama"] = name
//        hashMap["email"] = email
//        hashMap["nomor_telepon"] = phoneNumber
//        hashMap["alamat"] = alamat
//        if (uploadedImageUrl != null) {
//            hashMap["foto_profil"] = uploadedImageUrl
//        }
//        // Tambahkan data khusus penjual
//        if (typeAkun == "penjual") {
//            bank = binding.bank.text.toString().trim()
//            rekening = binding.rekening.text.toString().trim()
//
//            hashMap["bank"] = bank
//            hashMap["no_rekening"] = rekening
//        }
//        val reference = FirebaseDatabase.getInstance().getReference("Users")
//        reference.child(firebaseAuth.uid ?: "")
//            .updateChildren(hashMap)
//            .addOnSuccessListener {
//                progressDialog.dismiss()
//                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
//                imageUri = null
//            }
//            .addOnFailureListener {
//                progressDialog.dismiss()
//                Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun updateProfil(uploadedImageUrl: String?) {
        progressDialog.setMessage("Memperbarui informasi user")
        progressDialog.show()
        try {
            val hashMap = HashMap<String, Any>()
            hashMap["nama"] = name
            hashMap["email"] = email
            hashMap["nomor_telepon"] = phoneNumber
            hashMap["alamat"] = alamat
            if (uploadedImageUrl != null) {
                hashMap["foto_profil"] = uploadedImageUrl
            }
            // Tambahkan data khusus penjual
            if (typeAkun == "penjual") {
                bank = binding.bank.text.toString().trim()
                rekening = binding.rekening.text.toString().trim()
                hashMap["bank"] = bank
                hashMap["no_rekening"] = rekening
            }
            val reference = FirebaseDatabase.getInstance().getReference("Users")
            reference.child(firebaseAuth.uid ?: "")
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    imageUri = null
                    finish()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun imagePickDialog() {

        // init untuk popup menu memilih kamera atau galeri
        val popupMenu = PopupMenu(this, binding.profileImagePickFab)

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
            return@setOnMenuItemClickListener true
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
        galleryActivityResultLauncher.launch(intent)
    }

}