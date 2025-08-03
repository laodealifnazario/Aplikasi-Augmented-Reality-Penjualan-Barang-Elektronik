package com.tugas_akhir.alifnzr.DetailBarang

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.tugas_akhir.alifnzr.R
import com.tugas_akhir.alifnzr.databinding.RowGambarSliderBinding

class AdapterGambarSlider : RecyclerView.Adapter<AdapterGambarSlider.HolderImageSlider> {

    private lateinit var binding : RowGambarSliderBinding
    private var imageArrayList : ArrayList<ModelGambarSlider>
    private var context : Context

    constructor(context: Context, imageArrayList: java.util.ArrayList<ModelGambarSlider>) {
        this.context = context
        this.imageArrayList = imageArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImageSlider {
        binding = RowGambarSliderBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImageSlider(binding.root)
    }

    override fun getItemCount(): Int {
        return imageArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImageSlider, position: Int) {
        val modelGambarSlider = imageArrayList[position]
        lihatGambarBarang(holder, modelGambarSlider, position)

    }

    inner class HolderImageSlider(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageTv : ShapeableImageView = binding.imageIv
        var imageCountTv : TextView = binding.imageCountTv
    }

    private fun lihatGambarBarang(a : HolderImageSlider, b : ModelGambarSlider, c : Int){
        val imageUrl = b.imageUrl
        val imageCount = "${c+1}/${imageArrayList.size}"
        a.imageCountTv.text = imageCount
        try {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(a.imageTv)
        } catch (e:Exception) {
        }
    }
}