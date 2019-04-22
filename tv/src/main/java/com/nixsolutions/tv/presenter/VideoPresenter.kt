package com.nixsolutions.tv.presenter

import android.media.MediaPlayer
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.nixsolutions.tv.contract.VideoActivityContract

class VideoPresenter(private val view: VideoActivityContract.View) : VideoActivityContract.Presenter {

    override var arSceneView: ArSceneView? = null

    override var videoRenderable: ModelRenderable? = null

    override fun onArPlaneTap(hitResult: HitResult, texture: ExternalTexture, mediaPlayer: MediaPlayer) {
        if (videoRenderable == null) {
            return
        }

        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        val videoNode = Node()

        anchorNode.setParent(arSceneView?.scene)
        videoNode.setParent(anchorNode)

        mediaPlayer.apply {
            val videoWidth = videoWidth.toFloat()
            val videoHeight = videoHeight.toFloat()
            videoNode.localScale = Vector3(
                VIDEO_HEIGHT_METERS * (videoWidth / videoHeight),
                VIDEO_HEIGHT_METERS,
                VIDEO_NODE_Z_COORDINATE
            )
        }

        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()

            texture.surfaceTexture.setOnFrameAvailableListener {
                videoNode.renderable = videoRenderable
                texture.surfaceTexture.setOnFrameAvailableListener(null)
            }
        } else {
            videoNode.renderable = videoRenderable
        }

        disableSurfaceDetection()
    }


    override fun disableSurfaceDetection() {
        arSceneView?.planeRenderer?.isVisible = false
    }

    companion object {

        private const val VIDEO_HEIGHT_METERS = 0.85f
        private const val VIDEO_NODE_Z_COORDINATE = 1.0f
    }

}