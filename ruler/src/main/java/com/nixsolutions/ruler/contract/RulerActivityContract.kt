package com.nixsolutions.ruler.contract

import com.google.ar.core.HitResult
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ViewRenderable
import java.util.concurrent.CompletableFuture

interface RulerActivityContract {

    interface View {

        fun setupLengthTextView(renderable: ViewRenderable, length: Float)

        fun getRulerMaterial(): CompletableFuture<Material>
    }

    interface Presenter {

        fun onSceneTap(hitResult: HitResult, scene: Scene?)

        fun initModel()

        fun getFormattedLength(length: Float): String
    }
}