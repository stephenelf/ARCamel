package com.stephenelf.arcamel


import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.animation.ModelAnimator.INFINITE
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode





class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG:String="ARCamel"
    }

    var camelRenderable: ModelRenderable? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }
        setContentView(R.layout.activity_main)


        ModelRenderable.builder()
            // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
            .setSource(this, Uri.parse("camelrace_camel2.sfb"))

            // Instead, load as a resource from the 'res/raw' folder ('src/main/res/raw/andy.sfb'):
            //.setSource(this, R.raw.andy)

            .build()
            .thenAccept({ renderable -> camelRenderable = renderable })
            .exceptionally(
                { throwable ->
                    Log.e(TAG, "Unable to load Renderable.", throwable)
                    null
                })

        val arFragment: ArFragment? =
            supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?

        arFragment?.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (camelRenderable == null) {
                return@setOnTapArPlaneListener
            }

            val danceData = camelRenderable?.getAnimationData("metarig|metarigAction")
            val camelAnimator = ModelAnimator(danceData, camelRenderable)
            camelAnimator.repeatCount=INFINITE
            camelAnimator.start()

            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment?.getArSceneView().scene)

            // Create the transformable andy and add it to the anchor.
            val camelNode = TransformableNode(arFragment.getTransformationSystem())


            camelNode.scaleController.maxScale=0.1f
            camelNode.scaleController.minScale=0.01f

            camelNode.setParent(anchorNode)
            camelNode.renderable = camelRenderable
            camelNode.select()
        }
    }

    fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        val openGlVersionString =
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < 3.0) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }
}
