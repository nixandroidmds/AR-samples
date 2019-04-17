package com.nixsolutions.arsamples.presenter

import android.animation.ObjectAnimator
import android.net.Uri
import android.util.Log
import android.view.animation.LinearInterpolator
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import com.nixsolutions.arsamples.contract.TableActivityContract

class TablePresenter(private val view: TableActivityContract.View) : TableActivityContract.Presenter {

    override var arSceneView: ArSceneView? = null
    override var transformationSystem: TransformationSystem? = null

    private var phageRenderable: ModelRenderable? = null
    private var tableRenderable: ModelRenderable? = null
    private var table: TransformableNode? = null
    private var nextAnimation: Int = 0
    private var animator: ModelAnimator? = null
    private var sphere: ModelRenderable? = null
    private var cube: ModelRenderable? = null
    private var cylinder: ModelRenderable? = null

    override fun enableVerticalSurfaceDetection(session: Session?) {
        session?.apply {
            val config = Config(this)
            pause()
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            try {
                resume()
            } catch (e: CameraNotAvailableException) {
                e.printStackTrace()
                Log.d(TAG, "Camera not available")
            }

            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            configure(config)
        }
    }

    override fun placeTable(hitResult: HitResult) {
        if (phageRenderable == null || tableRenderable == null) {
            return
        }

        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arSceneView?.scene)

        table = TransformableNode(transformationSystem).apply {
            setParent(anchorNode)
            renderable = tableRenderable
        }

        placePhage()
        disableSurfaceDetection()
    }

    override fun placePhage() {
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
            pos.y += PHAGE_Y_COORDINATE_OFFSET
            worldPosition = pos

        }
        playSkeletonAnimation()
        startRotating(phageNode)
    }

    override fun startRotating(node: Node) {
        val quaternionsList = getRotationQuaternions()
        val rotatingAnimator = ObjectAnimator()

        rotatingAnimator.apply {
            setObjectValues(*quaternionsList.toTypedArray())
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

    private fun getRotationQuaternions(): List<Quaternion> {
        val yVector = Vector3.up()
        val quaternionsList = ArrayList<Quaternion>()
        val step = END_AXIS_ANGLE / 4
        for (angle in START_AXIS_ANGLE..END_AXIS_ANGLE step step) {
            quaternionsList.add(Quaternion.axisAngle(yVector, angle.toFloat()))
        }
        return quaternionsList
    }

    override fun playSkeletonAnimation() {
        phageRenderable?.apply {
            val data = getAnimationData(nextAnimation)
            nextAnimation = (nextAnimation + 1) % animationDataCount
            animator = ModelAnimator(data, this).apply { start() }
        }
    }

    override fun disableSurfaceDetection() {
        arSceneView?.planeRenderer?.isVisible = false
    }

    override fun setupModels() {
        val cubeVector = Vector3(CUBE_VECTOR_COORDINATE, CUBE_VECTOR_COORDINATE, CUBE_VECTOR_COORDINATE)

        view.makeOpaqueMaterial {
            sphere = ShapeFactory.makeSphere(POINT_RADIUS, Vector3.zero(), it)
            cube = ShapeFactory.makeCube(cubeVector, Vector3.zero(), it)
            cylinder = ShapeFactory.makeCylinder(CYLINDER_COORDINATE, CYLINDER_COORDINATE, Vector3.zero(), it)
        }

        view.loadRenderable(Uri.parse(PHAGE_MODEL_NAME)) { phageRenderable = it }
        view.loadRenderable(Uri.parse(TABLE_MODEL_NAME)) { tableRenderable = it }
    }

    override fun placeModel(hitResult: HitResult, renderable: Renderable) {
        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(transformationSystem)

        anchorNode.setParent(arSceneView?.scene)
        node.setParent(anchorNode)
        node.renderable = renderable
    }

    private companion object {

        private val TAG = TablePresenter::class.java.canonicalName
        private const val PHAGE_BONE_NAME = "bone_name"
        private const val POINT_RADIUS = 0.1f
        private const val CUBE_VECTOR_COORDINATE = 0.1f
        private const val CYLINDER_COORDINATE = 0.1f
        private const val PHAGE_MODEL_NAME = "phage.sfb"
        private const val TABLE_MODEL_NAME = "table.sfb"
        private const val PHAGE_Y_COORDINATE_OFFSET = .715f
        private const val START_AXIS_ANGLE = 0
        private const val END_AXIS_ANGLE = 360
    }
}