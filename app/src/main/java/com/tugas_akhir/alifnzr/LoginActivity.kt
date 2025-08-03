package com.tugas_akhir.alifnzr

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private var email = ""
    private var password = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // inisialisasi firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // teks tombol belum ada akun
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterUserActivity::class.java))
        }

        binding.lupaPassword.setOnClickListener {
            startActivity(Intent(this, LupaPassword::class.java))
        }

        // inisialisasi Dialog progress akan ditampilkan saat membuat akun
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon tunggu")
        progressDialog.setCanceledOnTouchOutside(false)

        // tombol Login
        binding.btnLogin.setOnClickListener{
            validateData()
        }
    }

    private fun validateData() {

        // 1.) Input data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        // 2.) Validasi Data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email tidak valid...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()) {
            Toast.makeText(this, "Masukkan Password...", Toast.LENGTH_SHORT).show()
        }
        else {
            // membuat user di firebase auth
            firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener {
                    // Login berhasil, buka MainActivity
                    login()
                }
                .addOnFailureListener {
                    // login gagal
                    progressDialog.dismiss()
                    Toast.makeText(this, "Gagal Login...", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun login() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.uid)
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("type_akun").getValue(String::class.java)
                    if (userType != null) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("userType", userType)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Tipe akun tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this@LoginActivity, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}