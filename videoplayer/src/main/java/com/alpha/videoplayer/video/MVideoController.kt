package com.reconova.operation.widget.video

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import com.alpha.videoplayer.R
import kotlinx.android.synthetic.main.layout_video_controller.view.*

class MVideoController(private val videoView: MVideoView) : VideoController() {
    companion object {
        private const val TAG = "MVideoController"
    }

    private val pauseIcon: Int
    private val playIcon: Int
    private val controllerView: View =
        LayoutInflater.from(videoView.context).inflate(R.layout.layout_video_controller, null)

    private var showController = true
    private var isTouch: Boolean = false
    private var isLoading = false

    private var listener: VideoListener? = null

    init {
        videoView.setController(this)
        playIcon = R.mipmap.icon_play
        pauseIcon = R.mipmap.icon_pause
        initView()
        initListener()
    }

    override fun containerView(): View {
        return controllerView
    }

    override fun setPlayBtnClickListener(listener: View.OnClickListener) {
        controllerView.ivStart.setOnClickListener(listener)
    }

    override fun setFullScreenClickListener(listener: View.OnClickListener) {
        controllerView.ivFullScreen.setOnClickListener(listener)
    }

    override fun setVideoListener(listener: VideoListener) {
        this.listener = listener
    }

    override fun onLoad() {
        loading()
    }

    override fun onPlay() {
        controllerView.ivStart?.setImageResource(pauseIcon)
        startTimer()
        listener?.onStart()
    }

    override fun onPause() {
        stopTimer()
        controllerView.rlLoading.visibility = View.GONE
        controllerView.rlBuffering.visibility = View.GONE
        controllerView.ivStart?.setImageResource(playIcon)
        listener?.onPause()
    }

    override fun onStop() {
        stopTimer()
        controllerView.ivStart?.setImageResource(playIcon)
        listener?.onStop()
    }

    override fun onRelease() {
        stopTimer()
        defaultUI()
        controllerView.ivStart?.setImageResource(playIcon)
        listener?.onRelease()
    }

    override fun onRenderingStart() {
        controllerView.sbProgress.isEnabled = true
        loadingComplete()
    }

    override fun onBufferStart() {
        buffering()
    }

    override fun onBufferComplete() {
        bufferComplete()
    }

    override fun onPlayComplete() {
        controllerView.sbProgress?.progress = 0
        controllerView.sbProgress?.max = 100
        playComplete()
        listener?.onComplete()
    }

    override fun onError() {
        playError()
    }

    private fun initView() {
        controllerView.rlController.visibility = if (showController) {
            View.VISIBLE
        } else {
            View.GONE
        }
        controllerView.rlLoading.visibility = View.GONE
        controllerView.rlBuffering.visibility = View.GONE
        controllerView.rlError.visibility = View.GONE
    }

    private fun initListener() {
        Log.d(TAG, "initListener")
        controllerView.sbProgress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTouch = false
                listener?.seekTo(seekBar?.progress ?: 0)
                videoView.seekTo((seekBar?.progress ?: 0).toLong())
            }
        })
        controllerView.ivStart.setOnClickListener {
            if (videoView.isPlaying()) {
                videoView.pause()
            } else {
                videoView.start()
            }
        }
        controllerView.ivFullScreen.setOnClickListener {
            if (videoView.isFullScreen()) {
                videoView.exitFullScreen()
            } else {
                videoView.enterFullScreen()
            }
        }
    }

    private fun loading() {
        Log.d(TAG, "loading")
        isLoading = true
        controllerView.rlDefault.visibility = View.GONE
        controllerView.rlBuffering.visibility = View.GONE
        controllerView.rlLoading.visibility = View.VISIBLE
        controllerView.rlError.visibility = View.GONE
        controllerView.rlComplete.visibility = View.GONE
        if (showController) {
            controllerView.rlController.visibility = View.GONE
        }
    }

    private fun loadingComplete() {
        Log.d(TAG, "loadingComplete")
        isLoading = false
        controllerView.rlDefault.visibility = View.GONE
        controllerView.rlBuffering.visibility = View.GONE
        controllerView.rlLoading.visibility = View.GONE
        controllerView.rlError.visibility = View.GONE
        controllerView.rlComplete.visibility = View.GONE
        if (showController) {
            controllerView.rlController.visibility = View.VISIBLE
        }
    }

    private fun defaultUI() {
        controllerView.rlDefault.visibility = View.VISIBLE
        controllerView.rlBuffering.visibility = View.GONE
        controllerView.rlError.visibility = View.GONE
        controllerView.rlLoading.visibility = View.GONE
        controllerView.rlComplete.visibility = View.GONE
    }

    private fun buffering() {
        controllerView.rlDefault.visibility = View.GONE
        controllerView.rlBuffering.visibility = View.VISIBLE
        controllerView.rlError.visibility = View.GONE
        controllerView.rlLoading.visibility = View.GONE
        controllerView.rlComplete.visibility = View.GONE
    }

    private fun bufferComplete() {
        controllerView.rlDefault.visibility = View.GONE
        controllerView.rlBuffering.visibility = View.GONE
        controllerView.rlError.visibility = View.GONE
        controllerView.rlComplete.visibility = View.GONE
        controllerView.rlLoading.visibility = View.GONE
    }

    private fun playError() {
        isLoading = false
        controllerView.rlLoading.visibility = View.GONE
        controllerView.rlBuffering.visibility = View.GONE
        controllerView.rlComplete.visibility = View.GONE
        controllerView.rlDefault.visibility = View.GONE
        controllerView.rlError.visibility = View.VISIBLE
    }

    private fun playComplete() {
        isLoading = false
        controllerView.rlLoading.visibility = View.GONE
        controllerView.rlBuffering.visibility = View.GONE
        controllerView.rlError.visibility = View.GONE
        controllerView.rlDefault.visibility = View.GONE
        controllerView.rlComplete.visibility = View.VISIBLE
    }

    override fun onProgressUpdate() {
        //                Log.d(TAG, "is touch ${isTouch}")
        if (!isTouch) {
            controllerView.sbProgress?.post {
                val result = videoView.updateProgress()
                result?.let {
                    controllerView.sbProgress?.progress = result[0]
                    controllerView.sbProgress?.max = result[1]
                    listener?.onProgressUpdate(result[0].toLong())
                }
            }
        }
    }

    override fun showController(visible: Boolean) {
        showController = visible

        controllerView.rlController.visibility = if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

interface VideoListener {

    fun onProgressUpdate(progress: Long)

    fun seekTo(progress: Int)

    fun onPause()

    fun onStart()

    fun onStop()

    fun onRelease()

    fun onComplete()
}