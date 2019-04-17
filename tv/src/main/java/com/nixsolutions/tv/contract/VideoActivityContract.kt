package com.nixsolutions.tv.contract

import android.media.MediaPlayer
import com.google.ar.core.HitResult
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable

interface VideoActivityContract {

    interface View

    interface Presenter {

        var arSceneView: ArSceneView?

        var videoRenderable: ModelRenderable?

        fun onArPlaneTap(hitResult: HitResult, texture: ExternalTexture, mediaPlayer: MediaPlayer)

        fun disableSurfaceDetection()
    }
}