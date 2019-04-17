package com.nixsolutions.ruler.utils

import android.support.annotation.LayoutRes
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.ViewRenderable

object ArUtils {

    private const val CUBE_SHAPE_COORDINATE = 0.01f

    fun drawLineBetweenPoints(
        pointFrom: Vector3,
        pointTo: Vector3,
        anchorNode: AnchorNode,
        material: Material?
    ): Float {
        val difference = Vector3.subtract(pointFrom, pointTo)
        val directionFromTopToBottom = difference.normalized()
        val rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up())
        val length = difference.length()
        val sizeVector = Vector3(CUBE_SHAPE_COORDINATE, CUBE_SHAPE_COORDINATE, length)
        val lineRenderable = ShapeFactory.makeCube(sizeVector, Vector3.zero(), material)
        val lineNode = Node()

        lineNode.apply {
            setParent(anchorNode)
            renderable = lineRenderable
            worldPosition = Vector3.add(pointFrom, pointTo).scaled(.5f)
            worldRotation = rotationFromAToB
        }

        return length
    }

    fun attachRenderableToNode(node: Node, renderable: ModelRenderable?) {
        val pointNode = Node()
        pointNode.setParent(node)
        pointNode.renderable = renderable
    }

    fun attachViewToNode(
        node: Node,
        scene: Scene?,
        @LayoutRes layoutRes: Int,
        localPositionVector: Vector3,
        viewSetupCallback: (ViewRenderable) -> Unit
    ) {
        initViewRenderable(scene, layoutRes) {
            it.isShadowCaster = false
            viewSetupCallback(it)
            val linePos = node.worldPosition
            val cameraPos = scene?.camera?.worldPosition
            val direction = Vector3.subtract(cameraPos, linePos)
            val quaternion = Quaternion.lookRotation(direction, Vector3.up())
            val lengthNode = Node()

            lengthNode.renderable = it
            lengthNode.setParent(node)
            lengthNode.localPosition = localPositionVector
            lengthNode.worldRotation = quaternion
        }
    }

    fun initViewRenderable(
        scene: Scene?,
        @LayoutRes layoutRes: Int,
        initCallback: (ViewRenderable) -> Unit
    ) {
        ViewRenderable.builder()
            .setView(scene?.view?.context, layoutRes)
            .build()
            .thenAccept { initCallback(it) }
    }
}