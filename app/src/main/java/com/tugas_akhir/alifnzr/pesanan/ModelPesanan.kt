package com.tugas_akhir.alifnzr
//
class ModelPesanan {
    var id_pesanan : String = ""
    var id_barang : String = ""
    var uid_penjual : String = ""
    var uid_pembeli : String = ""
    var nama_barang: String = ""
    var total_harga: Int = 0
    var status : String = ""
    var timestamp : Long = 0
    var id_pengiriman_barang : String = ""
    var id_transaksi : String = ""
    var alamat_pengiriman : String = ""

    constructor()

    constructor(
        id_pesanan : String,
        id_barang: String,
        uid_penjual : String,
        uid_pembeli : String,
        nama_barang: String,
        total_harga: Int,
        status : String,
        timestamp : Long,
        id_pengiriman_barang : String,
        id_transaksi : String,
        alamat_pengiriman : String
    ) {
        this.id_pesanan = id_pesanan
        this.id_barang = id_barang
        this.uid_penjual = uid_penjual
        this.uid_pembeli = uid_pembeli
        this.nama_barang = nama_barang
        this.total_harga = total_harga
        this.status = status
        this.timestamp = timestamp
        this.id_pengiriman_barang = id_pengiriman_barang
        this.id_transaksi = id_transaksi
        this.alamat_pengiriman = alamat_pengiriman
    }
}

