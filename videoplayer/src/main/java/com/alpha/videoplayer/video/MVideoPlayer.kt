package com.reconova.operation.widget.video

import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.IOException

class MVideoPlayer : VideoPlayer {
    companion object {
        private const val TAG = "MVideoPlayer"
        const val INIT = 0
        const val PREPARING = 1
        const val PREPARED = 2
        const val PLAY_START = 3
        const val PAUSE = 4
        const val STOP = 5
        const val COMPLETE = 6
        const val ERROR = 6
        const val STARTING = 7
    }

    override var isLoading: Boolean = false
    private var player: IjkMediaPlayer? = null
    override var status: Int = 0
    private var isStart = false
    private var listener: PlayListener? = null

    private var controller: VideoController? = null

    override fun init() {
        status = INIT
        player = IjkMediaPlayer()
        player?.apply {
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_VERBOSE)
            // 支持硬解 1：开启 O:关闭
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)
            // 设置播放前的探测时间 1,达到首屏秒开效果
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1)

            /**
             * 播放延时的解决方案
             */
            // rtsp协议，可以优先用tcp(默认是用udp)
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp")
            // 如果没有设置stimeout，在解析时（也就是avformat_open_input）把网线拔掉，av_read_frame会阻塞（时间单位是微妙）
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "stimeout", "5000000");
            // 设置播放前的最大探测时间 （100未测试是否是最佳值）
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 1000L)
            // 每处理一个packet之后刷新io上下文
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L)
            // 需要 准备好后自动播放
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)
            // 不额外优化（使能非规范兼容优化，默认值0 ）
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fast", 1)
            // 是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1)

            // 自动旋屏
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0)
            // 处理分辨率变化
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0)
            // 最大缓冲大小
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 256 * 1024)
            // 默认最小帧数2
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 2)
            // 最大缓存时长
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 5) //300
            // 是否限制输入缓存数
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);
            // 缩短播放的rtmp视频延迟在1s内
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer")
            // 播放前的探测Size，默认是1M, 改小一点会出画面更快
//            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10 * 1024)
            // 播放重连次数
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 5)
            // 设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
            setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48L)
            // 跳过帧 ？？
            setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0)
            // 视频帧处理不过来的时候丢弃一些帧达到同步的效果
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 5)

            // 超时时间，timeout参数只对http设置有效，若果你用rtmp设置timeout，ijkplayer内部会忽略timeout参数。rtmp的timeout参数含义和http的不一样。
//        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 50000);
            // 因为项目中多次调用播放器，有网络视频，rtsp，本地视频，还有wifi上http视频，所以得清空DNS才能播放WIFI上的视频
            setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1)

            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1)

            setOption(
                IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                "overlay-format",
                IjkMediaPlayer.SDL_FCC_RV32.toLong()
            )

            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "http-detect-range-support", 0)

            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1)

            setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
            setVolume(1.0f, 1.0f)
            setScreenOnWhilePlaying(true)
        }
    }

    override fun getVideoWidth(): Int {
        return player?.videoWidth ?: 0
    }

    override fun getVideoHeight(): Int {
        return player?.videoHeight ?: 0
    }

    override fun setDataSource(path: String) {
        Log.d(TAG, "setDataSource $path")
        try {
            player?.dataSource = path
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun setDisplay(holder: SurfaceHolder) {
        player?.setDisplay(holder)
    }

    override fun setSurface(surface: Surface) {
        player?.setSurface(surface)
    }

    override fun hasPrepared(): Boolean {
        return status != INIT
    }

    override fun prepare() {
        Log.d(TAG, "prepare")
        player?.apply {
            setOnPreparedListener {
                Log.d(TAG, "prepared")
                status = PREPARED
                if (isStart) {
                    Log.d(TAG, "start 1")
                    start()
                    isLoading = true
                    controller?.onLoad()
                    controller?.onPlay()
                }
            }
            setOnErrorListener { _, what, extra ->
                val msg = "what $what extra $extra"
                Log.e(TAG, "onError: $msg")

                status = ERROR
                isLoading = false
                controller?.onError()
                listener?.onError(msg)
                true
            }
            setOnCompletionListener {
                status = COMPLETE
                isLoading = false
                controller?.onPlayComplete()
                listener?.onPlayComplete()
            }
            setOnBufferingUpdateListener { player: IMediaPlayer, percent: Int ->
                //                Log.d(tag, "Buffer update $percent")
            }
            setOnVideoSizeChangedListener { mp, width, height, sar_num, sar_den ->
                Log.d(TAG, "prepare:  width：$width， height：$height")
                listener?.onVideoSizeChanged(width, height)
            }
            setOnInfoListener { mp: IMediaPlayer, what: Int, extra: Int ->
                Log.d(TAG, "what $what")
                when (what) {
                    IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                        isLoading = false
                        controller?.onBufferStart()
                        listener?.onBufferStart()
                    }
                    IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        controller?.onBufferComplete()
                        listener?.onBufferEnd()
                    }
                    IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                        Log.d(TAG, "onRenderingStart")
                        status = PLAY_START
                        isLoading = false
                        listener?.onRenderingStart()
                        controller?.onRenderingStart()
                    }
                    IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE -> {
                        Log.d(TAG, "what MEDIA_INFO_NOT_SEEKABLE $extra")
                    }
                }
                true
            }
            setScreenOnWhilePlaying(true)
            prepareAsync()
            status = PREPARING
        }
    }

    override fun currentPosition(): Long {
        return player?.currentPosition ?: 0
    }

    override fun duration(): Long {
        return player?.duration ?: 0
    }

    override fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    override fun isPlaying(): Boolean {
        return player?.isPlaying == true /*&& status == PLAY_START*/
    }

    override fun pause() {
        isStart = false

        if (player?.isPlaying == true) {
            player?.pause()
        }
        status = PAUSE
        controller?.onPause()
    }

    override fun start() {
        Log.d(TAG, "start")
        isStart = true

        if (status == PREPARED || status == PAUSE) {
            player?.start()
            if (status == PREPARED) {
                isLoading = true
                controller?.onLoad()
            }
            controller?.onPlay()
        }
    }

    override fun stop() {
        isStart = false
        Log.d(TAG, "stop $status")
        if (player?.isPlaying == true || status == PAUSE || status == COMPLETE) {
            Log.d(TAG, "stop real")
            player?.stop()
            player?.reset()
            status = STOP
        }
        controller?.onStop()
    }

    override fun release() {
        isStart = false
        if (player?.isPlaying == true || status == PAUSE) {
            player?.stop()
            player?.reset()
        }
        player?.release()
        status = INIT
        controller?.onRelease()
    }

    override fun setListener(listener: PlayListener) {
        this.listener = listener
    }

    override fun setController(controller: VideoController) {
        this.controller = controller
    }
}

interface PlayListener {
    fun onRenderingStart()
    fun onBufferStart()
    fun onBufferEnd()
    fun onPlayComplete()
    fun onError(msg: String)
    fun onVideoSizeChanged(width: Int, height: Int)
}