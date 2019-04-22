package com.nixsolutions.arsamples.activity

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.nixsolutions.arsamples.R
import com.nixsolutions.arsamples.contract.TableActivityContract
import com.nixsolutions.arsamples.presenter.TablePresenter

class TableActivity : AppCompatActivity(), TableActivityContract.View {

    private val arFragment: ArFragment?
        get() = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment?

    private lateinit var presenter: TableActivityContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = TablePresenter(this)

        presenter.arSceneView = arFragment?.arSceneView
        presenter.transformationSystem = arFragment?.transformationSystem
        presenter.setupModels()

        arFragment?.setOnTapArPlaneListener { hitResult, _, _ -> presenter.placeTable(hitResult) }
    }

    override fun onResume() {
        super.onResume()
        presenter.enableVerticalSurfaceDetection(arFragment?.arSceneView?.session)
    }

    override fun loadRenderable(uri: Uri, callback: (ModelRenderable) -> Unit) {
        ModelRenderable
            .builder()
            .setSource(this, uri)
            .build()
            .thenAccept { callback(it) }
    }

    override fun makeOpaqueMaterial(callback: (Material) -> Unit) {
        val color = Color().apply { set(getColor(R.color.defaultModelColor)) }
        MaterialFactory.makeOpaqueWithColor(this, color).thenAccept {
            callback(it)
        }
    }

    override fun changeSurfaceTexture() {
        val textureCompletable = Texture.builder().setSource(this, R.drawable.plane).build()
        arFragment?.apply {
            arSceneView.planeRenderer
                .material
                .thenAcceptBoth<Texture>(textureCompletable) { material, texture ->
                    material.setTexture(PlaneRenderer.MATERIAL_TEXTURE, texture)
                }

        }
    }
}
