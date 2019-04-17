package com.nixsolutions.tv.activity

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.nixsolutions.tv.R
import com.nixsolutions.tv.contract.VideoActivityContract
import com.nixsolutions.tv.presenter.VideoPresenter

class VideoActivity : AppCompatActivity(), VideoActivityContract.View {

    private val arFragment: ArFragment?
        get() = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?

    private lateinit var presenter: VideoActivityContract.Presenter
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val texture = ExternalTexture()
        presenter = VideoPresenter(this)

        presenter.arSceneView = arFragment?.arSceneView
        mediaPlayer = MediaPlayer.create(this, R.raw.video).apply {
            setSurface(texture.surface)
            isLooping = true
        }

        setupVideoRenderable(texture)
        mediaPlayer?.also {
            arFragment?.setOnTapArPlaneListener { hitResult: HitResult, _, _ ->
                presenter.onArPlaneTap(hitResult, texture, it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    private fun setupVideoRenderable(texture: ExternalTexture) {
        val keyColor = Color(KEY_COLOR_RED, KEY_COLOR_GREEN, KEY_COLOR_BLUE)

        ModelRenderable
            .builder()
            .setSource(this, Uri.parse(VIDEO_RENDERABLE_NAME))
            .build()
            .thenAccept {
                it.material.setExternalTexture(VIDEO_TEXTURE_KEY, texture)
                it.material.setFloat4(KEY_COLOR, keyColor)
                presenter.videoRenderable = it
            }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply { release() }
        mediaPlayer = null
    }

    private companion object {

        private const val VIDEO_RENDERABLE_NAME = "video_model.sfb"
        private const val VIDEO_TEXTURE_KEY = "videoTexture"
        private const val KEY_COLOR = "keyColor"
        private const val KEY_COLOR_RED = 0.1843f
        private const val KEY_COLOR_GREEN = 1.0f
        private const val KEY_COLOR_BLUE = 0.098f
    }
}
