package com.tugas_akhir.alifnzr.Diskusi

class ModelDetailDiskusi {
    var messageId : String = ""
    var messageType: String = ""
    var message: String = ""
    var fromUid: String = ""
    var imageUri: String = ""
    var toUid:String = ""
    var timestamp: Long = 0


    constructor()

    constructor(
        messageId: String,
        messageType: String,
        message: String,
        fromUid: String,
        imageUri: String,
        toUid: String,
        timestamp: Long
    ) {
        this.messageId = messageId
        this.messageType = messageType
        this.message = message
        this.fromUid = fromUid
        this.imageUri = imageUri
        this.toUid = toUid
        this.timestamp = timestamp
    }
}