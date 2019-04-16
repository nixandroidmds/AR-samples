package com.nixsolutions.ruler.presenter

import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.ViewRenderable
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
            val newVector = anchorNode.worldPosition
            val length = ArUtils.drawLineBetweenPoints(newVector, it, anchorNode, blueMaterial)
            setupLengthRenderable(scene, anchorNode, length)
            newVector
        } ?: anchorNode.worldPosition

        ArUtils.attachToAnchorNode(anchorNode, pointRenderable)
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

    private fun setupLengthRenderable(scene: Scene?, anchorNode: AnchorNode, length: Float) {
        ViewRenderable.builder()
            .setView(scene?.view?.context, R.layout.length_counter)
            .build()
            .thenAccept { setupLengthLabel(it, anchorNode, length, scene) }
    }

    private fun setupLengthLabel(renderable: ViewRenderable, anchorNode: AnchorNode, length: Float, scene: Scene?) {
        renderable.isShadowCaster = false
        view.setupLengthTextView(renderable, length)
        val linePos = anchorNode.worldPosition
        val cameraPos = scene?.camera?.worldPosition
        val direction = Vector3.subtract(cameraPos, linePos)
        val quaternion = Quaternion.lookRotation(direction, Vector3.up())
        val lengthNode = Node()

        lengthNode.renderable = renderable
        lengthNode.setParent(anchorNode)
        lengthNode.localPosition = Vector3.up().apply { y = LENGTH_NODE_Y_POSITION }
        lengthNode.worldRotation = quaternion
    }

    companion object {

        private const val POINT_RADIUS = 0.02f
        private const val LENGTH_LABEL_FORMAT = "#.###"
        private const val LENGTH_NODE_Y_POSITION = 0.2f
    }
}