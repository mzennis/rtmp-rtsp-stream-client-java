package com.pedro.testapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.pedro.encoder.input.gl.SpriteGestureController
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtplibrary.RtmpCamera
import com.pedro.rtplibrary.util.BitrateAdapter
import com.pedro.rtplibrary.view.LightOpenGlView
import net.ossrs.rtmp.ConnectCheckerRtmp

class MainActivity : AppCompatActivity(),
        ConnectCheckerRtmp,
        SurfaceHolder.Callback {

    private val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var lightOpenGlView: LightOpenGlView
    private lateinit var btnStartStop: Button
    private lateinit var btnSwitchCamera: Button
//    private lateinit var btnImage: Button
//    private lateinit var btnGif: Button
    private lateinit var etUrl: EditText

    private lateinit var rtmpCamera: RtmpCamera
    private var bitrateAdapter: BitrateAdapter? = null

    private val spriteGestureController = SpriteGestureController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_open_gl)

        if (!hasPermissions(this, *permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }

        setupView()
        setupBroadcaster()
    }

    private fun setupView() {
        lightOpenGlView = findViewById(R.id.light_open_gl_view)
        btnStartStop = findViewById(R.id.b_start_stop)
        btnSwitchCamera = findViewById(R.id.switch_camera)
//        btnGif = findViewById(R.id.btn_gif)
//        btnImage = findViewById(R.id.btn_image)
        etUrl = findViewById(R.id.et_rtp_url)
        etUrl.setText("rtmp://broadcast.tokopedia.com/play_record/b522523d-ad6e-11eb-aa19-aac07be6a46e?auth_key=1622789656-0-0-bcf868762c138fcad62afa713f214e59")

        lightOpenGlView.holder.addCallback(this)
//        lightOpenGlView.setOnTouchListener(object : View.OnTouchListener {
//            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
//                if (spriteGestureController.spriteTouched(view, motionEvent)) {
//                    spriteGestureController.moveSprite(view, motionEvent)
//                    spriteGestureController.scaleSprite(motionEvent)
//                    return true
//                }
//                return false
//            }
//
//        })

        btnStartStop.setOnClickListener {
            if (!rtmpCamera.isStreaming) {
                // need to re-prepare every time start streaming
                val audio = rtmpCamera.prepareAudio()
                val video = rtmpCamera.prepareVideo(1280, 720, 900 * 1024)
                if (audio && video) {
                    btnStartStop.setText(R.string.stop_button)
                    rtmpCamera.startStream(etUrl.text.toString())
                } else showToaster("Error preparing stream, This device cant do it")
            } else {
                btnStartStop.setText(R.string.start_button)
                rtmpCamera.stopStream()
            }
        }

        btnSwitchCamera.setOnClickListener {
            try {
                rtmpCamera.switchCamera()
            } catch (e: CameraOpenException) {
                showToaster(e.message.orEmpty())
            }
        }

//        btnImage.setOnClickListener { setTextToStream() }
//        btnGif.setOnClickListener { setGifToStream() }
    }

    private fun setupBroadcaster() {
        rtmpCamera = RtmpCamera(lightOpenGlView, this)
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String?) {
        showToaster("Connection started")
    }

    override fun onConnectionSuccessRtmp() {
        showToaster("Connection success")
        bitrateAdapter = BitrateAdapter { bitrate -> rtmpCamera.setVideoBitrateOnFly(bitrate) }
        bitrateAdapter?.setMaxBitrate(rtmpCamera.bitrate)
    }

    override fun onConnectionFailedRtmp(reason: String?) {
        showToaster("Connection failed. $reason")
        rtmpCamera.stopStream()
        btnStartStop.setText(R.string.start_button)
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        bitrateAdapter?.adaptBitrate(bitrate, rtmpCamera.hasCongestion())
    }

    override fun onDisconnectRtmp() {
        showToaster("Disconnected")
    }

    override fun onAuthErrorRtmp() {
        showToaster("Auth error")
    }

    override fun onAuthSuccessRtmp() {
        showToaster("Auth success")
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        rtmpCamera.startPreview()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        if (rtmpCamera.isStreaming) {
            rtmpCamera.stopStream()
            btnStartStop.text = resources.getString(R.string.start_button)
        }
        rtmpCamera.stopPreview()
    }

    private fun showToaster(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    /**
     *
    private fun setTextToStream() {
        spriteGestureController.stopListener()
        val textObjectFilterRender = TextObjectFilterRender()
        rtmpCamera.glInterface.setFilter(textObjectFilterRender)
        textObjectFilterRender.setText("Hello world", 22f, Color.RED)
        textObjectFilterRender.setDefaultScale(rtmpCamera.streamWidth, rtmpCamera.streamHeight)
        textObjectFilterRender.setPosition(TranslateTo.CENTER)
        spriteGestureController.setBaseObjectFilterRender(textObjectFilterRender) //Optional
    }

    private fun setImageToStream() {
        spriteGestureController.stopListener()
        val imageObjectFilterRender = ImageObjectFilterRender()
            rtmpCamera.glInterface.setFilter(imageObjectFilterRender)
        imageObjectFilterRender.setImage(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        imageObjectFilterRender.setDefaultScale(rtmpCamera.streamWidth, rtmpCamera.streamHeight)
        imageObjectFilterRender.setPosition(TranslateTo.RIGHT)
        spriteGestureController.setBaseObjectFilterRender(imageObjectFilterRender) //Optional
        spriteGestureController.setPreventMoveOutside(false) //Optional
    }

    private fun setGifToStream() {
        try {
            spriteGestureController.stopListener()
            val gifObjectFilterRender = GifObjectFilterRender()
            gifObjectFilterRender.setGif(resources.openRawResource(R.raw.banana))
            rtmpCamera.glInterface.setFilter(gifObjectFilterRender)
            gifObjectFilterRender.setDefaultScale(rtmpCamera.streamWidth, rtmpCamera.streamHeight)
            gifObjectFilterRender.setPosition(TranslateTo.BOTTOM)
            spriteGestureController.setBaseObjectFilterRender(gifObjectFilterRender) //Optional
        } catch (e: IOException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }
     */
}