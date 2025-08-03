package com.tugas_akhir.alifnzr.JualBarang

import android.net.Uri

class ModelGambarPicked {
    var id = ""
    var imageUri: Uri? = null
    var imagerUrl: String? = null
    var fromInternet = false

    constructor()

    constructor(id: String, imageUri: Uri?, imagerUrl: String?, fromInternet: Boolean) {
        this.id = id
        this.imageUri = imageUri
        this.imagerUrl = imagerUrl
        this.fromInternet = fromInternet
    }
}