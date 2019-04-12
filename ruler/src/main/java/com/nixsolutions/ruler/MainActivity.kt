package com.nixsolutions.ruler

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private var lastVector: Vector3? = null
    private var point: ModelRenderable? = null
    private var blueMaterial: Material? = null
    private var arFragment: ArFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        arFragment?.setOnTapArPlaneListener { hitResult, _, _ -> onSceneTap(hitResult) }
    }

    override fun onStart() {
        super.onStart()
        initModel()
    }

    private fun onSceneTap(hitResult: HitResult) {
        if (point == null) {
            return
        }

        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment?.arSceneView?.scene)

        lastVector = if (lastVector == null) {
            anchorNode.worldPosition
        } else {
            val newVector = anchorNode.worldPosition
            drawLineBetweenPoints(newVector, lastVector!!, anchorNode)
            newVector
        }

        val pointNode = Node()
        pointNode.setParent(anchorNode)
        pointNode.renderable = point
    }

    private fun drawLineBetweenPoints(point1: Vector3, point2: Vector3, anchorNode: AnchorNode) {
        val difference = Vector3.subtract(point1, point2)
        val directionFromTopToBottom = difference.normalized()
        val rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up())
        val length = difference.length()
        val lineRenderable = ShapeFactory.makeCube(Vector3(.01f, .01f, length), Vector3.zero(), blueMaterial)
        val line = Node()

        line.apply {
            setParent(anchorNode)
            renderable = lineRenderable
            worldPosition = Vector3.add(point1, point2).scaled(.5f)
            worldRotation = rotationFromAToB
        }

        ViewRenderable.builder()
            .setView(this, R.layout.length_counter)
            .build()
            .thenAccept { setupLengthTextView(it, anchorNode, length) }
    }

    private fun setupLengthTextView(viewRenderable: ViewRenderable, anchorNode: AnchorNode, length: Float) {
        viewRenderable.isShadowCaster = false
        val decimalFormat = DecimalFormat("#.###")
        val counter = viewRenderable.view
        val textView = counter.findViewById<TextView>(R.id.counter)
        val lengthFormatString = getString(R.string.counter_format_meters, decimalFormat.format(length.toDouble()))
        textView.text = lengthFormatString

        val linePos = anchorNode.worldPosition
        val cameraPos = arFragment!!.arSceneView.scene.camera.worldPosition
        val direction = Vector3.subtract(cameraPos, linePos)
        val quaternion = Quaternion.lookRotation(direction, Vector3.up())

        val lengthNode = Node()
        lengthNode.renderable = viewRenderable
        lengthNode.setParent(anchorNode)
        lengthNode.localPosition = Vector3(0f, 0.2f, 0f)
        lengthNode.worldRotation = quaternion
    }

    private fun initModel() {
        if (point == null && blueMaterial == null) {
            MaterialFactory.makeOpaqueWithColor(this, Color(0f, 255f, 244f)).thenAccept {
                blueMaterial = it
                point = ShapeFactory.makeSphere(pointRadius, Vector3.zero(), blueMaterial)
            }
        }
    }

    companion object {

        private const val pointRadius = 0.02f
    }
}
