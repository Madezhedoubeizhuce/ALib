package com.reconova.operation.widget.video

import android.view.Surface
import android.view.SurfaceHolder

interface VideoPlayer {
    var status: Int
    var isLoading: Boolean

    fun init()
    fun getVideoWidth(): Int
    fun getVideoHeight(): Int
    fun setDataSource(path: String)
    fun setDisplay(holder: SurfaceHolder)
    fun setSurface(surface: Surface)
    fun hasPrepared(): Boolean
    fun prepare()
    fun currentPosition(): Long
    fun duration(): Long
    fun seekTo(position: Long)
    fun isPlaying(): Boolean
    fun pause()
    fun start()
    fun stop()
    fun release()
    fun setListener(listener: PlayListener)
    fun setController(controller: VideoController)
}