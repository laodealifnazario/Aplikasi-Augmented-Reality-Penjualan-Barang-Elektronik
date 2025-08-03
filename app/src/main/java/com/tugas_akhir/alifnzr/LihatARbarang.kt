package com.tugas_akhir.alifnzr

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugas_akhir.alifnzr.databinding.ActivityLihatArBinding

class LihatARbarang : AppCompatActivity() {

    private lateinit var binding: ActivityLihatArBinding
    private lateinit var arFragment: ArFragment
    private var modelPlaced = false
    private var id_barang = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLihatArBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment

        id_barang = intent.getStringExtra("id_barang").toString()

        val ref = FirebaseDatabase.getInstance().getReference("Barang")
        ref.child(id_barang).child("Objek3D").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val objek3D = snapshot.getValue(Objek3D::class.java)
                    if (objek3D != null && objek3D.objekUrl != null) {
                        // Ambil URL objek 3D
                        val objekUrl = objek3D.objekUrl
                        renderObjek3D(Uri.parse(objekUrl))
                    } else {
                        Toast.makeText(baseContext, "URL objek 3D kosong", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(baseContext, "Gagal parsing URL objek 3D", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
                Toast.makeText(baseContext, "Gagal mengambil data dari database", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun renderObjek3D(uri: Uri) {
        ModelRenderable.builder()
            .setSource(this, uri)
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { modelRenderable ->
                arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
                    if (modelPlaced) return@setOnTapArPlaneListener
                    val anchor = hitResult.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arFragment.arSceneView.scene)
                    val transformableNode = TransformableNode(arFragment.transformationSystem)
                    transformableNode.scaleController.minScale = 0.2f
                    transformableNode.scaleController.maxScale = 2.0f
                    transformableNode.renderable = modelRenderable
                    transformableNode.setParent(anchorNode)
                    transformableNode.select()
                    modelPlaced = true
                }
            }
            .exceptionally { throwable ->
                throwable.printStackTrace()
                Toast.makeText(this, "Gagal load model", Toast.LENGTH_SHORT).show()
                null
            }
    }

}
