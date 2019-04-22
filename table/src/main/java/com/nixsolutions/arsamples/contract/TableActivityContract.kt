package com.nixsolutions.arsamples.contract

import android.net.Uri
import com.google.ar.core.HitResult
import com.google.ar.core.Session
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.TransformationSystem

interface TableActivityContract {

    interface View {

        fun loadRenderable(uri: Uri, callback: (ModelRenderable) -> Unit)

        fun makeOpaqueMaterial(callback: (Material) -> Unit)

        fun changeSurfaceTexture()
    }

    interface Presenter {

        var arSceneView: ArSceneView?

        var transformationSystem: TransformationSystem?

        fun enableVerticalSurfaceDetection(session: Session?)

        fun placeTable(hitResult: HitResult)

        fun setupModels()

        fun placePhage()

        fun startRotating(node: Node)

        fun playSkeletonAnimation()

        fun disableSurfaceDetection()

        fun placeModel(hitResult: HitResult, renderable: Renderable)
    }
}