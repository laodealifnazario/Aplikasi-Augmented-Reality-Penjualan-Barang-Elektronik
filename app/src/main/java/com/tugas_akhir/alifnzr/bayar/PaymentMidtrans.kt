package com.tugas_akhir.alifnzr.bayar

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.models.BillingAddress
import com.midtrans.sdk.corekit.models.CustomerDetails
import com.midtrans.sdk.corekit.models.ItemDetails
import com.midtrans.sdk.corekit.models.ShippingAddress
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import com.tugas_akhir.alifnzr.R
import java.util.ArrayList

class PaymentMidtrans : AppCompatActivity() {

    private var id_pesanan = ""
    private var harga = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bayar)

        val beli = findViewById<Button>(R.id.beli)

        // Dapatkan id_pesanan dan harga dari intent
        id_pesanan = intent.getStringExtra("id_pesanan").toString()

        // Inisialisasi Midtrans
        SdkUIFlowBuilder.init()
            .setClientKey("SB-Mid-client-LEgl_slDrW1ZfCHo") // Client Key dari Midtrans Dashboard
            .setContext(applicationContext)
            .setTransactionFinishedCallback(TransactionFinishedCallback { result ->
                // Handle hasil transaksi
            })
            .setMerchantBaseUrl("http://localhost/midtrans/index.php/")
            .enableLog(true)
            .setLanguage("id")
            .buildSDK()

        // Set onClickListener untuk tombol beli
        beli.setOnClickListener {
            val orderId = "ORDER-${System.currentTimeMillis()}" // Pastikan ini tidak null
            val transactionRequest = TransactionRequest(orderId, 10000.0)
            val detail = ItemDetails("sdfs", 20000.0, 1, "Deskripsi Item")
            val itemDetails = ArrayList<ItemDetails>()
            itemDetails.add(detail)
            uiKitDetails(transactionRequest, "data nama disini/ variabel")
            transactionRequest.itemDetails = itemDetails
            MidtransSDK.getInstance().transactionRequest = transactionRequest
            MidtransSDK.getInstance().startPaymentUiFlow(this)
        }
    }

    fun uiKitDetails(transactionRequest: TransactionRequest, name: String) {
        val customerDetails = CustomerDetails()
        customerDetails.customerIdentifier = "Orang"
        customerDetails.phone = "89909"
        customerDetails.firstName = "Suf"
        customerDetails.lastName = "Lf"
        customerDetails.email = "aa@ga.com"

        val shippingAddress = ShippingAddress()
        shippingAddress.address = "Baturan, Gtiwaeno"
        shippingAddress.city = "Sleman"
        shippingAddress.postalCode = "4332"
        customerDetails.shippingAddress = shippingAddress

        val billingAddress = BillingAddress()
        billingAddress.address = "sdf,sdf"
        billingAddress.city = "Skm"
        billingAddress.postalCode = "32214"
        customerDetails.billingAddress = billingAddress

        transactionRequest.customerDetails = customerDetails
    }
}
