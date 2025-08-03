package com.tugas_akhir.alifnzr.admin
//
class ModelBarang {
    var id_barang : String = ""
    var uid : String = ""
    var nama_barang: String = ""
    var jenis_barang: String = ""
    var harga: Int = 0
    var deskripsi: String = ""

    constructor()

    constructor(
        id_barang: String,
        uid : String,
        nama_barang: String,
        jenis_barang: String,
        harga: Int,
        deskripsi: String,
    ) {
        this.id_barang = id_barang
        this.uid = uid
        this.nama_barang = nama_barang
        this.jenis_barang = jenis_barang
        this.harga = harga
        this.deskripsi = deskripsi
    }
}