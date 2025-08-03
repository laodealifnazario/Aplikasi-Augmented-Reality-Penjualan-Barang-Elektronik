package com.tugas_akhir.alifnzr.admin


class ModelTransaksi {
    var id_transaksi : String = ""
    var id_pesanan : String = ""
    var id_barang : String = ""
    var nama_pemilik_rekening : String = ""
    var bank_asal: String = ""
    var rekening_asal : String = ""
    var bank_tujuan_transfer:String = ""
    var rekening_tujuan_transfer : String = ""
    var akun:String = ""
    var uid_pembeli: String = ""
    var alamat_pengiriman: String = ""
    var harga : Int = 0
    var timestamp : Long = 0

    constructor()

    constructor(
        id_transaksi: String,
        id_pesanan : String,
        id_barang : String,
        nama_pemilik_rekening: String,
        bank_asal: String,
        rekening_asal: String,
        bank_tujuan_transfer: String,
        rekening_tujuan_transfer:String,
        akun: String,
        uid_pembeli: String,
        alamat_pengiriman: String,
        harga : Int,
        timestamp : Long
    ) {
        this.id_transaksi = id_transaksi
        this.id_pesanan = id_pesanan
        this.id_barang = id_barang
        this.nama_pemilik_rekening = nama_pemilik_rekening
        this.bank_asal = bank_asal
        this.rekening_asal = rekening_asal
        this.bank_tujuan_transfer = bank_tujuan_transfer
        this.rekening_tujuan_transfer = rekening_tujuan_transfer
        this.akun = akun
        this.uid_pembeli = uid_pembeli
        this.alamat_pengiriman = alamat_pengiriman
        this.harga = harga
        this.timestamp = timestamp
    }

}
