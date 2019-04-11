package com.nixsolutions.arsamples

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.LinearInterpolator
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {

    private val PHAGE_BONE_NAME = "bone_name"
    private var arFragment: ArFragment? = null
    private var phageRenderable: ModelRenderable? = null
    private var table: TransformableNode? = null
    private var animator: ModelAnimator? = null
    private var nextAnimation: Int = 0
    private var tableRenderable: ModelRenderable? = null
    private var sphere: ModelRenderable? = null
    private var cube: ModelRenderable? = null
    private var cylinder: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment?
        arFragment?.setOnTapArPlaneListener { hitResult, _, _ -> placeTable(hitResult) }
        setupDefaultModels()
        loadRenderable(Uri.parse("phage.sfb")) { model -> phageRenderable = model }
        loadRenderable(Uri.parse("table.sfb")) { model -> tableRenderable = model }
    }

    override fun onResume() {
        super.onResume()
        enableVerticalSurfaceDetection()
    }

    private fun placeTable(hitResult: HitResult) {
        if (phageRenderable == null || tableRenderable == null) {
            return
        }

        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment?.arSceneView?.scene)

        table = TransformableNode(arFragment?.transformationSystem).apply {
            setParent(anchorNode)
            renderable = tableRenderable
        }

        placePhage()
        disableSurfaceDetection()
    }

    private fun startRotating(node: Node) {
        val yVector = Vector3.up()
        val orientation1 = Quaternion.axisAngle(yVector, 0f)
        val orientation2 = Quaternion.axisAngle(yVector, 120f)
        val orientation3 = Quaternion.axisAngle(yVector, 240f)
        val orientation4 = Quaternion.axisAngle(yVector, 360f)

        val rotatingAnimator = ObjectAnimator()
        rotatingAnimator.apply {
            setObjectValues(orientation1, orientation2, orientation3, orientation4)
            propertyName = "localRotation"
            setEvaluator(QuaternionEvaluator())
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
            setAutoCancel(true)
            target = node
            duration = 1000
            start()
        }
    }

    private fun playSkeletonAnimation() {
        phageRenderable?.apply {
            val data = getAnimationData(nextAnimation)
            nextAnimation = (nextAnimation + 1) % animationDataCount
            animator = ModelAnimator(data, this).apply { start() }
        }
    }

    private fun setupDefaultModels() {
        val color = Color(0f, 255f, 244f)
        val pointRadius = 0.1f
        val cubeVector = Vector3(0.1f, 0.1f, 0.1f)

        MaterialFactory.makeOpaqueWithColor(this, color).thenAccept {
            sphere = ShapeFactory.makeSphere(pointRadius, Vector3.zero(), it)
            cube = ShapeFactory.makeCube(cubeVector, Vector3.zero(), it)
            cylinder = ShapeFactory.makeCylinder(0.1f, 1.0f, Vector3.zero(), it)
        }
    }

    private fun changeSurfaceTexture() {
        val build = Texture.builder().setSource(this, R.drawable.plane).build()
        arFragment!!.arSceneView
            .planeRenderer
            .material
            .thenAcceptBoth<Texture>(build) { material, texture ->
                material.setTexture(PlaneRenderer.MATERIAL_TEXTURE, texture)
            }
    }

    private fun placeModel(hitResult: HitResult, renderable: Renderable) {
        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(arFragment?.transformationSystem)

        anchorNode.setParent(arFragment?.arSceneView?.scene)
        node.setParent(anchorNode)
        node.renderable = renderable
    }

    private fun enableVerticalSurfaceDetection() {
        val session = arFragment?.arSceneView?.session
        session?.apply {
            val config = Config(this)
            pause()
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            try {
                resume()
            } catch (e: CameraNotAvailableException) {
                e.printStackTrace()
            }

            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            configure(config)
        }
    }

    private fun placePhage() {
        val phageNode = Node()
        val phageSkeletonNode = SkeletonNode()

        phageSkeletonNode.apply {
            setParent(table)
            setBoneAttachment(PHAGE_BONE_NAME, phageSkeletonNode)
        }
        phageNode.apply {
            renderable = phageRenderable
            setParent(phageSkeletonNode)
            worldScale = Vector3.one()
            worldRotation = Quaternion.identity()
            val pos = worldPosition
            pos.y += .715f
            worldPosition = pos

        }
        playSkeletonAnimation()
        startRotating(phageNode)
    }

    private fun disableSurfaceDetection() {
        arFragment?.arSceneView?.planeRenderer?.isVisible = false
    }

    private fun loadRenderable(uri: Uri, callback: (ModelRenderable) -> Unit) {
        ModelRenderable.builder()
            .setSource(this, uri)
            .build()
            .thenApply { callback(it) }
    }
}
