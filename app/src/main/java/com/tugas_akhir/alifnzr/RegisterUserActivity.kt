package com.tugas_akhir.alifnzr

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tugas_akhir.alifnzr.databinding.ActivityRegisterUserBinding
import org.json.JSONObject


class RegisterUserActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterUserBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon tunggu")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnKembali.setOnClickListener{
            onBackPressed()
        }

        binding.registerPenjual.setOnClickListener{
            startActivity(Intent(this, RegisterPenjualActivity::class.java))
        }

        binding.btnRegister.setOnClickListener{
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""
    private var telepon = ""
    private var alamat = ""

    private fun validateData() {

        // 1.) Input data
        name = binding.namaEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        telepon = binding.noTelepon.text.toString().trim()
        alamat = binding.alamat.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        // 2.) Validasi Data
        if (name.isEmpty()) {
            Toast.makeText(this, "Wajib Masukkan semua kolom...", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Wajib Masukkan semua kolom...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()) {
            Toast.makeText(this, "Wajib Masukkan semua kolom...", Toast.LENGTH_SHORT).show()
        }
        else if (cPassword.isEmpty()) {
            Toast.makeText(this, "Wajib Masukkan semua kolom...", Toast.LENGTH_SHORT).show()
        }
        else if (password != cPassword) {
            Toast.makeText(this, "Wajib Masukkan semua kolom...", Toast.LENGTH_SHORT).show()
        }
        else {
            membuatAkunPembeli()
        }
    }

    private fun membuatAkunPembeli() {
        // tampilkan progress
        progressDialog.setMessage("Membuat Akun...")
        progressDialog.show()
        // membuat user di firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // akun dibuat, sekarang tambahkan info pengguna di database
                progressDialog.setMessage("Menyimpan informasi user")
                // waktu tempel
                val timestamp = System.currentTimeMillis()
                // dapatkan uid pengguna saat ini, karena pengguna sudah terdaftar sehingga kita bisa mendapatkannya sekarang
                val registeredPenjualEmail = firebaseAuth.currentUser!!.email
                val registeredPenjualUid = firebaseAuth.uid
                val hashMap: HashMap<String, Any?> = HashMap()

                hashMap["uid"] = registeredPenjualUid
                hashMap["email"] = registeredPenjualEmail
                hashMap["nama"] = name
                hashMap["nomor_telepon"] = telepon
                hashMap["alamat"] = alamat
                hashMap["type_akun"] = "pembeli"  // Gunakan type_akun yang diambil dari Firebase
                hashMap["foto_profil"] = ""  // tambah kosong, akan dilakukan dalam pengeditan profil
                hashMap["timestamp"] = timestamp

                val userRef = FirebaseDatabase.getInstance().getReference("Users")
                userRef.child(registeredPenjualUid!!)
                    .setValue(hashMap)
                    .addOnCompleteListener { task2 ->
                        progressDialog.dismiss()
                        if (task2.isSuccessful) {
                            // Data user berhasil disimpan ke dalam database
                            Toast.makeText(this, "Akun berhasil dibuat...", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RegisterUserActivity, LoginActivity::class.java))
                            finishAffinity()
                        } else {
                            // Gagal menambahkan data ke database
                            Toast.makeText(this, "Gagal membuat akun...", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .addOnFailureListener {
                // Gagal membuat akun
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal membuat akun...", Toast.LENGTH_SHORT).show()
            }
    }
}