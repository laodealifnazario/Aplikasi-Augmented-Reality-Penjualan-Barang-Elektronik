package com.tugas_akhir.alifnzr.DetailBarang

import android.net.Uri

class ModelGambarSlider {

    var id : String = ""
    var imageUrl: String = ""

    constructor()

    constructor(id:String,imageUri: Uri) {
        this.id = id
        this.imageUrl = imageUrl
    }
}

