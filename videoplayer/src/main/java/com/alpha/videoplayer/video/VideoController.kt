package com.reconova.operation.widget.video

import android.util.Log
import android.view.View
import java.util.*

abstract class VideoController {
    companion object {
        private const val TAG = "VideoController"
    }

    private var timer: Timer? = null
    private val uploadInterval: Long = 100
    private var timerTask: TimerTask? = null

    abstract fun containerView(): View
    abstract fun setPlayBtnClickListener(listener: View.OnClickListener)
    abstract fun setFullScreenClickListener(listener: View.OnClickListener)
    abstract fun setVideoListener(listener: VideoListener)
    abstract fun onLoad()

    // 开始播放，第一次播放不会回调此方法，而是回调onRenderingStart
    abstract fun onPlay()
    abstract fun onPause()
    abstract fun onStop()
    abstract fun onRelease()

    //开始渲染页面，表示第一次开始播放
    abstract fun onRenderingStart()
    abstract fun onBufferStart()
    abstract fun onBufferComplete()
    abstract fun onPlayComplete()
    abstract fun onError()
    protected abstract fun onProgressUpdate()
    abstract fun showController(visible: Boolean)
    protected fun stopTimer() {
        Log.d(TAG, "stopTimer: ")
        timer?.cancel()
    }


    protected fun startTimer() {
        stopTimer()
        Log.d(TAG, "startTimer")
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                onProgressUpdate()
            }
        }
        timer?.schedule(timerTask, uploadInterval, uploadInterval)
    }
}