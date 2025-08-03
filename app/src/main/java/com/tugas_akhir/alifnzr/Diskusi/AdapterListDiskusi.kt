package com.tugas_akhir.alifnzr.Diskusi

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.RowListDiskusiBinding
import java.util.Calendar
import java.util.Locale

class AdapterListDiskusi  : RecyclerView.Adapter<AdapterListDiskusi.HolderObrolan> {

    private var context: Context
    private var diskusiArrayList: ArrayList<ModelListDiskusi>
    private lateinit var binding: RowListDiskusiBinding
    private var firebaseAuth: FirebaseAuth

    constructor(context: Context, diskusiArrayList: ArrayList<ModelListDiskusi>) {
        this.context = context
        this.diskusiArrayList = diskusiArrayList
        firebaseAuth = FirebaseAuth.getInstance()
        myUid = "${firebaseAuth.uid}"
    }

    private var myUid = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderObrolan {
        binding = RowListDiskusiBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderObrolan(binding.root)
    }

    override fun onBindViewHolder(holder: HolderObrolan, position: Int) {
        val modelChats = diskusiArrayList[position]

        loadLastMessage(modelChats,holder)

        holder.itemView.setOnClickListener {
            klikDetailDiskusi(context, modelChats)
        }
    }

    override fun getItemCount(): Int {
        return diskusiArrayList.size
    }

    private fun klikDetailDiskusi(a : Context, b : ModelListDiskusi){
        val receiptUid = b.receiptUid
        if (receiptUid != null){
            val intent = Intent(context, DetailDiskusiActivity::class.java)
            intent.putExtra("receiptUid", receiptUid)
            context.startActivity(intent)
        }
    }

    fun setFilteredList(diskusiArrayList: ArrayList<ModelListDiskusi>){
        this.diskusiArrayList = diskusiArrayList
        notifyDataSetChanged()
    }

    private fun loadLastMessage(a: ModelListDiskusi, b: HolderObrolan) {
        //
        val chatKey = a.chatKey

        val ref = FirebaseDatabase.getInstance().getReference("Diskusi")
        ref.child(chatKey).limitToLast(1)
            .addValueEventListener(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val fromUid = "${ds.child("fromUid").value}"
                        val message = "${ds.child("message").value}"
                        val messageId = "${ds.child("messageId").value}"
                        val messageType = "${ds.child("messageType").value}"
                        val timestamp = ds.child("timestamp").value as Long ?: 0
                        val toUid = "${ds.child("toUid").value}"

                        fun formatTimestampDate(timestamp: Long) : String {
                            val calender = Calendar.getInstance(Locale.ENGLISH)
                            calender.timeInMillis = timestamp
                            return android.text.format.DateFormat.format("dd/MM/yyyy", calender).toString()
                        }

                        val formattedDate = formatTimestampDate(timestamp)

                        a.message = message
                        a.messageId = messageId
                        a.messageType = messageType
                        a.fromUid = fromUid
                        a.timestamp = timestamp
                        a.toUid = toUid

                        b.dateTimeTv.text = formattedDate

                        if (messageType == "TEXT"){
                            b.lastMessageTv.text = message
                        } else {
                            b.lastMessageTv.text = "Sends Attahment"
                        }
                    }
                    loadProfil(a,b)
                }

                override fun onCancelled(error: DatabaseError) {
                    //
                }

            })
    }

    @SuppressLint("SuspiciousIndentation")
    private fun loadProfil(a: ModelListDiskusi, b: HolderObrolan) {
        val fromUid = a.fromUid
        val toUid = a.toUid
        var receiptUid = ""
        receiptUid = if(fromUid==myUid){
            toUid
        } else{
            fromUid
        }
        a.receiptUid=receiptUid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(receiptUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("nama").value}"
                    val profileImageUrl = "${snapshot.child("foto_profil").value}"

                    a.nama = name
                    a.foto_profil = profileImageUrl

                    b.nameTv.text = name

                    try {
                        Glide.with(b.profileIv.context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.person)
                            .into(b.profileIv)
                    }catch (e:Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //
                }

            })
    }

    inner class HolderObrolan(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileIv = binding.profileIv
        var nameTv = binding.nameTv
        var lastMessageTv = binding.lastMessageTv
        var dateTimeTv = binding.dateTimeTv
    }
}

