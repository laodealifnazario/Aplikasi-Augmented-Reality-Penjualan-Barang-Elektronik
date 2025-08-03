package com.tugas_akhir.alifnzr

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.tugas_akhir.alifnzr.databinding.ActivityLupaPasswordBinding

class LupaPassword : AppCompatActivity() {

    private lateinit var binding : ActivityLupaPasswordBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLupaPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnResetPassword.setOnClickListener{
            val email = binding.emailEt.text.toString()
            val edtEmail = binding.emailEt

            if (email.isEmpty()){
                edtEmail.error = "Email Wajib Diisi"
                edtEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                edtEmail.error = "Email Tidak Valid"
                edtEmail.requestFocus()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this,"Email Reset Password Telah Dikirim",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this,"Gagal Reset Password Telah Dikirim",Toast.LENGTH_SHORT).show()
                }
            }

        }

        binding.tvBackToLogin.setOnClickListener {
            finish() // atau bisa intent ke LoginActivity
        }
     }
}