package com.nixsolutions.tv

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity() {

    private var arFragment: ArFragment? = null
    private var videoRenderable: ModelRenderable? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        val texture = ExternalTexture()
        val keyColor = Color(0.1843f, 1.0f, 0.098f)
        mediaPlayer = MediaPlayer.create(this, R.raw.video).apply {
            setSurface(texture.surface)
            isLooping = true
        }

        ModelRenderable.builder()
            .setSource(this, Uri.parse("video_model.sfb"))
            .build()
            .thenAccept {
                videoRenderable = it
                it.material.setExternalTexture("videoTexture", texture)
                it.material.setFloat4("keyColor", keyColor)
            }

        arFragment?.setOnTapArPlaneListener { hitResult: HitResult, _, _ -> onArPlaneTap(hitResult, texture) }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.apply { release() }
        mediaPlayer = null
    }

    private fun onArPlaneTap(hitResult: HitResult, texture: ExternalTexture) {
        if (videoRenderable == null) {
            return
        }

        val videoHeightMeters = 0.85f
        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        val videoNode = Node()

        anchorNode.setParent(arFragment!!.arSceneView.scene)
        videoNode.setParent(anchorNode)

        mediaPlayer?.apply {
            val videoWidth = videoWidth.toFloat()
            val videoHeight = videoHeight.toFloat()
            videoNode.localScale = Vector3(
                videoHeightMeters * (videoWidth / videoHeight), videoHeightMeters, 1.0f
            )
        }


        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()

            texture.surfaceTexture
                .setOnFrameAvailableListener {
                    videoNode.renderable = videoRenderable
                    texture.surfaceTexture.setOnFrameAvailableListener(null)
                }
        } else {
            videoNode.renderable = videoRenderable
        }

        disableSurfaceDetection()
    }


    private fun disableSurfaceDetection() {
        arFragment!!.arSceneView.planeRenderer.isVisible = false
    }
}
