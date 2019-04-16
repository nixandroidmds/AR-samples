package com.nixsolutions.ruler.utils

import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory

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

    fun attachToAnchorNode(anchorNode: AnchorNode, renderable: ModelRenderable?) {
        val pointNode = Node()
        pointNode.setParent(anchorNode)
        pointNode.renderable = renderable
    }
}