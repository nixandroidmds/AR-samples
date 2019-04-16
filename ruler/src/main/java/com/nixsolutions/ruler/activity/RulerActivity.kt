package com.nixsolutions.ruler.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.nixsolutions.ruler.R
import com.nixsolutions.ruler.contract.RulerActivityContract
import com.nixsolutions.ruler.presenter.RulerPresenter
import kotlinx.android.synthetic.main.length_counter.view.*
import java.util.concurrent.CompletableFuture

class RulerActivity : AppCompatActivity(), RulerActivityContract.View {

    private lateinit var presenter: RulerActivityContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = RulerPresenter(this).apply { initModel() }
        val arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            presenter.onSceneTap(hitResult, arFragment.arSceneView?.scene)
        }
    }

    override fun getRulerMaterial(): CompletableFuture<Material> {
        val color = Color().apply { set(getColor(R.color.ruler_material_color)) }
        return MaterialFactory.makeOpaqueWithColor(this, color)
    }

    override fun setupLengthTextView(renderable: ViewRenderable, length: Float) {
        val formattedLength = presenter.getFormattedLength(length)
        val lengthFormatString = getString(R.string.length_label_format_meters, formattedLength)
        renderable.view.lengthTextView.text = lengthFormatString
    }
}