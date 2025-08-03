package com.tugas_akhir.alifnzr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tugas_akhir.alifnzr.Diskusi.ListDiskusiFragment
import com.tugas_akhir.alifnzr.admin.TransaksiAdminFragment
import com.tugas_akhir.alifnzr.beranda.Beranda
import com.tugas_akhir.alifnzr.databinding.ActivityMainBinding
import com.tugas_akhir.alifnzr.pesanan.Pesanan

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tamplina default saat aplikasi dimulai
        val fragment = Beranda()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFl.id, fragment, "HomeFragment")
        fragmentTransaction.commit()

        // Menerima data userType dari Login
        val userType = intent.getStringExtra("userType") ?: ""
        setupBottomNavigationView(userType)
    }

    private fun setupBottomNavigationView(userType: String) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNv)

        // Hilangkan item menu transaksi jika bukan admin
        if (userType != "admin") {
            bottomNavigationView.menu.removeItem(R.id.menu_transaksi)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    binding.toolbarTitleTv.text = "Beranda"
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(binding.fragmentFl.id,
                        Beranda(), "Beranda")
                    fragmentTransaction.commit()
                    true
                }
                R.id.menu_diskusi -> {
                    binding.toolbarTitleTv.text = "Diskusi"
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(binding.fragmentFl.id, ListDiskusiFragment(), "DiskusiFragment")
                    fragmentTransaction.commit()
                    true
                }
                R.id.menu_pesanan -> {
                    binding.toolbarTitleTv.text = "Pesanan"
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(binding.fragmentFl.id, Pesanan(), "Pesanan")
                    fragmentTransaction.commit()
                    true
                }
                R.id.menu_transaksi -> {
                    binding.toolbarTitleTv.text = "Transaksi (Admin)"
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(binding.fragmentFl.id, TransaksiAdminFragment(), "TransaksiFragment")
                    fragmentTransaction.commit()
                    true
                }
                R.id.menu_akun -> {
                    binding.toolbarTitleTv.text = "Akun"
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(binding.fragmentFl.id, ProfilFragment(), "ProfilFragment(Admin)")
                    fragmentTransaction.commit()
                    true
                }
                else -> false
            }
        }
    }
}

