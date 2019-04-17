package com.nixsolutions.ruler.presenter

import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.nixsolutions.ruler.R
import com.nixsolutions.ruler.contract.RulerActivityContract
import com.nixsolutions.ruler.utils.ArUtils
import java.text.DecimalFormat

class RulerPresenter(private val view: RulerActivityContract.View) : RulerActivityContract.Presenter {

    private var lastVector: Vector3? = null
    private var pointRenderable: ModelRenderable? = null
    private var blueMaterial: Material? = null

    override fun onSceneTap(hitResult: HitResult, scene: Scene?) {
        if (pointRenderable == null) {
            return
        }

        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(scene)

        lastVector = lastVector?.let {
            val newVector = setupLine(anchorNode, it, scene)
            newVector
        } ?: anchorNode.worldPosition

        ArUtils.attachRenderableToNode(anchorNode, pointRenderable)
    }

    private fun setupLine(anchorNode: AnchorNode, lastVector: Vector3, scene: Scene?): Vector3? {
        val newVector = anchorNode.worldPosition
        val length = ArUtils.drawLineBetweenPoints(newVector, lastVector, anchorNode, blueMaterial)
        val localPositionVector = Vector3.up().apply { y = LENGTH_NODE_Y_POSITION }
        ArUtils.attachViewToNode(
            anchorNode,
            scene,
            R.layout.length_text_view,
            localPositionVector
        ) { renderable ->
            view.setupLengthTextView(renderable, length)
        }
        return newVector
    }

    override fun initModel() {
        if (pointRenderable == null && blueMaterial == null) {
            view.getRulerMaterial().thenAccept {
                blueMaterial = it
                pointRenderable = ShapeFactory.makeSphere(POINT_RADIUS, Vector3.zero(), blueMaterial)
            }
        }
    }

    override fun getFormattedLength(length: Float): String =
        DecimalFormat(LENGTH_LABEL_FORMAT).format(length.toDouble())

    companion object {

        private const val POINT_RADIUS = 0.02f
        private const val LENGTH_LABEL_FORMAT = "#.###"
        private const val LENGTH_NODE_Y_POSITION = 0.2f
    }
}