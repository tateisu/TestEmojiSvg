package jp.juggler.testemojisvg

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var activityJob: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + activityJob

    private lateinit var btnStart: View
    private lateinit var etLog: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityJob = Job()
        initUI()

        btnStart = findViewById(R.id.btnStart)
        etLog = findViewById(R.id.etLog)

        btnStart.setOnClickListener { startTest() }

    }

    private fun initUI() {
        setContentView(R.layout.activity_main)
    }

    private fun log(msg: String) = launch(Dispatchers.Main) {
        etLog.setText(msg)
    }

    private fun startTest() = launch {

        etLog.setText("")

        val bitmap = Bitmap.createBitmap(
            256, 256, Bitmap.Config.ARGB_8888
        )

        try {

            val canvas = Canvas(bitmap)

            withContext(Dispatchers.IO) {

                log("start...")

                var countError = 0
                val assetManager = resources.assets
                assetManager.list("")?.sorted()?.let{list->
                    val size = list.size
                    list.forEachIndexed{ index,path ->
                        try {
                            if(!path.contains("emj_")) return@forEachIndexed
                            canvas.drawRGB(255, 255, 255)
                            log("$index/$size $path")
                            val svg = SVG.getFromAsset(assetManager, path)
                            svg.renderToCanvas(canvas)
                        } catch (ex: Throwable) {
                            ++countError
                            Log.e("TestSVG","svg failed! $path",ex)
                            log("$path failed! ${ex.message}")
                        }
                    }
                }

                log("complete! countError=$countError")
            }
        } finally {
            bitmap.recycle()
        }
    }
}
