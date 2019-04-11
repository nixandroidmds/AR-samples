package com.nixsolutions.tv

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Toast
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
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

    private val CHROMA_KEY_COLOR = Color(0.1843f, 1.0f, 0.098f)
    private val VIDEO_HEIGHT_METERS = 0.85f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?

        val texture = ExternalTexture()

        mediaPlayer = MediaPlayer.create(this, R.raw.video)
        mediaPlayer!!.setSurface(texture.surface)
        mediaPlayer!!.isLooping = true

        ModelRenderable.builder()
            .setSource(this, Uri.parse("video_model.sfb"))
            .build()
            .thenAccept {
                videoRenderable = it
                it.material.setExternalTexture("videoTexture", texture)
                it.material.setFloat4("keyColor", CHROMA_KEY_COLOR)
            }
            .exceptionally {
                val toast = Toast.makeText(this, "Unable to load video renderable", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                null
            }

        arFragment?.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (videoRenderable == null) {
                return@setOnTapArPlaneListener
            }

            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment!!.arSceneView.scene)

            val videoNode = Node()
            videoNode.setParent(anchorNode)

            val videoWidth = mediaPlayer!!.videoWidth.toFloat()
            val videoHeight = mediaPlayer!!.videoHeight.toFloat()
            videoNode.localScale = Vector3(
                VIDEO_HEIGHT_METERS * (videoWidth / videoHeight), VIDEO_HEIGHT_METERS, 1.0f
            )

            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()

                texture
                    .surfaceTexture
                    .setOnFrameAvailableListener { surfaceTexture: SurfaceTexture ->
                        videoNode.renderable = videoRenderable
                        texture.surfaceTexture.setOnFrameAvailableListener(null)
                    }
            } else {
                videoNode.renderable = videoRenderable
            }

            disableSurfaceDetection()
        }
    }


    private fun disableSurfaceDetection() {
        arFragment!!.arSceneView.planeRenderer.isVisible = false
    }

    public override fun onDestroy() {
        super.onDestroy()

        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }
}
