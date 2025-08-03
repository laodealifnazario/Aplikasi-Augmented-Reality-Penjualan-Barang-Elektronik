package com.tugas_akhir.alifnzr

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.tugas_akhir.alifnzr.databinding.ActivityHapusAkunBinding

class HapusAkunActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHapusAkunBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHapusAkunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon Tunggu")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser

        binding.btnBack.setOnClickListener{
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener{
            hapusAkun()
        }
    }

    private fun hapusAkun() {
        try {
            progressDialog.setMessage("Menghapus data dan gambar...")
            progressDialog.show()

            val myUid = firebaseAuth.uid ?: return
            val profileImagePath = "UserProfile/profile_$myUid"
            val storageRef = FirebaseStorage.getInstance().reference
            val databaseRef = FirebaseDatabase.getInstance().reference

            // 1. Hapus semua gambar postingan user
            val refUserAds = databaseRef.child("Barang")
            refUserAds.orderByChild("uid").equalTo(myUid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val deleteTasks = mutableListOf<Task<Void>>()

                        for (ds in snapshot.children) {
                            // Ambil dan hapus gambar di node Barang/{id}/Images
                            val idBarang = ds.key ?: continue
                            val imageList = ds.child("Images")
                            for (img in imageList.children) {
                                val imageId = img.child("id").value.toString()
                                val imageStorageRef = storageRef.child("Barang/$imageId")
                                deleteTasks.add(imageStorageRef.delete())
                            }

                            // Hapus data barang dari database
                            ds.ref.removeValue()
                        }

                        // 2. Hapus foto profil
                        val profileImageRef = storageRef.child(profileImagePath)
                        deleteTasks.add(profileImageRef.delete())

                        // 3. Tunggu semua penghapusan selesai
                        Tasks.whenAllComplete(deleteTasks).addOnCompleteListener {
                            // 4. Hapus data Users
                            databaseRef.child("Users").child(myUid).removeValue()
                                .addOnSuccessListener {
                                    // 5. Terakhir: hapus akun auth
                                    firebaseAuth.currentUser?.delete()
                                        ?.addOnSuccessListener {
                                            progressDialog.dismiss()
                                            Toast.makeText(this@HapusAkunActivity, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                                            startMainActivityLogin()
                                        }
                                        ?.addOnFailureListener {
                                            progressDialog.dismiss()
                                            Toast.makeText(this@HapusAkunActivity, "Gagal hapus akun: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(this@HapusAkunActivity, "Gagal hapus data user: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        progressDialog.dismiss()
                        Toast.makeText(this@HapusAkunActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })

        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Kesalahan saat hapus akun: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startMainActivityLogin(){
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

}