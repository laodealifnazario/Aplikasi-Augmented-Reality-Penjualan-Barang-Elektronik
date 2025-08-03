package com.tugas_akhir.alifnzr

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handlerScreen()
    }
    private fun handlerScreen(){
        Handler().postDelayed(Runnable {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }, 3000)
    }
}