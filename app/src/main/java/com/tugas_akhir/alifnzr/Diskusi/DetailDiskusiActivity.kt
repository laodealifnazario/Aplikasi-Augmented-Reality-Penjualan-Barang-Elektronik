package com.tugas_akhir.alifnzr.Diskusi

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.ActivityDetailDiskusiBinding
import java.util.Arrays

class DetailDiskusiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailDiskusiBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var myUid = ""
    private var receiptUid = ""
    private var chatPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailDiskusiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon tunggu")
        progressDialog.setCanceledOnTouchOutside(false)

        receiptUid = intent.getStringExtra("receiptUid")!!
        myUid = firebaseAuth.uid!!

        fun chatPath(receiptUid:String,yourUid:String):String {
            val arrayUids = arrayOf(receiptUid, yourUid)

            Arrays.sort(arrayUids)

            return "${arrayUids[0]}_${arrayUids[1]}"
        }

        chatPath = chatPath(receiptUid,myUid)

        loadReceiptsDetails()

        loadDiskusi()

        // tombol mengirim pesan teks
        binding.sendFab.setOnClickListener {
            validateData()
        }

        binding.toolbarBackBtn.setOnClickListener{
            finish()
        }
    }

    private fun loadReceiptsDetails(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(receiptUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val name = "${snapshot.child("nama").value}"
                        val profilImageUrl = "${snapshot.child("foto_profil").value}"
                        binding.toolbarTitleTv.text = name
                        try {
                            Glide.with(this@DetailDiskusiActivity)
                                .load(profilImageUrl)
                                .placeholder(R.drawable.akun)
                                .into(binding.toolbarProvileIv)
                        } catch (e:Exception) {
                        }
                    }
                    catch (e:Exception){
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun validateData() {
        val message = binding.messageEt.text.toString().trim()
        val timestamp = System.currentTimeMillis()

        if (message.isEmpty()) {
            Toast.makeText(this, "Enter untuk mengirim . . .", Toast.LENGTH_SHORT).show()
        }
        else {
            kirimDiskusi("TEXT", message, timestamp)
        }
    }

    private fun kirimDiskusi(messageType: String, message: String, timestamp:Long) {
        progressDialog.setMessage("Mengirim pesan...")
        progressDialog.show()

        val refChat = FirebaseDatabase.getInstance().getReference("Diskusi")
        val keyId = "${refChat.push().key}"

        val hashMap = HashMap<String, Any>()
        hashMap["messageId"] = "$keyId"
        hashMap["messageType"] = "$messageType"
        hashMap["message"] = "$message"
        hashMap["fromUid"] = "$myUid"
        hashMap["toUid"] = "$receiptUid"
        hashMap["timestamp"] = timestamp

        refChat.child(chatPath)
            .child(keyId)
            .setValue(hashMap)
            .addOnCompleteListener { task ->
                progressDialog.dismiss()
                binding.messageEt.setText("")
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal mengirim pesan ke ${e.message} . . .", Toast.LENGTH_SHORT)
            }
    }

    private fun loadDiskusi() {
        val messageArrayList = ArrayList<ModelDetailDiskusi>()
        val ref = FirebaseDatabase.getInstance().getReference("Diskusi")
        ref.child(chatPath)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        messageArrayList.clear()
                        for (ds: DataSnapshot in snapshot.children) {
                            val modelChat = ds.getValue(ModelDetailDiskusi::class.java)
                            if (modelChat != null) {
                                messageArrayList.add(modelChat)
                            }
                        }
                        // Pastikan RecyclerView di-update di thread UI
                        runOnUiThread {
                            val adapterDetailDiskusi = AdapterDetailDiskusi(messageArrayList, myUid)
                            binding.chatRv.adapter = adapterDetailDiskusi
                            val layoutManager = LinearLayoutManager(this@DetailDiskusiActivity)
                            binding.chatRv.layoutManager = layoutManager
                        }
                    } catch (e: Exception) {
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

}