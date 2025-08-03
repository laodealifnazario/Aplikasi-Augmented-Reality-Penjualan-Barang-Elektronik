package com.tugas_akhir.alifnzr.admin.DaftarLaporan

class ModelLaporanMasalah {
    var id_laporan_masalah: String = ""
    var uid : String = ""
    var akun : String = ""
    var type_akun : String = ""
    var deskripsi_masalah: String = ""
    var jenis_laporan : String = ""
    var timestamp : Long = 0

    constructor()

    constructor(
        id_laporan_masalah: String,
        uid : String,
        akun : String,
        type_akun : String,
        deskripsi_masalah : String,
        jenis_laporan : String,
        timestamp : Long
    ) {
        this.id_laporan_masalah = id_laporan_masalah
        this.uid = uid
        this.akun = akun
        this.type_akun = type_akun
        this.deskripsi_masalah = deskripsi_masalah
        this.jenis_laporan = jenis_laporan
        this.timestamp = timestamp
    }

}