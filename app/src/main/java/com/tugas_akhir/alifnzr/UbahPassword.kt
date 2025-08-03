package com.tugas_akhir.alifnzr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.tugas_akhir.alifnzr.databinding.ActivityUbahPasswordBinding

class UbahPassword : AppCompatActivity() {

    private lateinit var binding : ActivityUbahPasswordBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUbahPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener{
            onBackPressed()
        }

        binding.changePasswordBtn.setOnClickListener {
            val currentPass = binding.currentPassword.text.toString()
            val newPass = binding.newPassword.text.toString()
            val confirmPass = binding.confirmPassword.text.toString()
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Kolom tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass != confirmPass) {
                Toast.makeText(this, "Passwords tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            changePassword(currentPass, newPass)
        }
    }

    private fun changePassword(currentPass: String, newPass: String) {
        val user = auth.currentUser
        val email = user?.email
        if (user != null && email != null) {
            val credential = EmailAuthProvider.getCredential(email, currentPass)
            user.reauthenticate(credential)
                .addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        user.updatePassword(newPass)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this, "Password gagal diubahi", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Re-authentication gagal", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}