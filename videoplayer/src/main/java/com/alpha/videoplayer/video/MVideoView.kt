package com.reconova.operation.widget.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.alpha.videoplayer.R
import com.alpha.videoplayer.util.ActivityManager
import com.alpha.videoplayer.util.NiceUtil
import kotlinx.android.synthetic.main.layout_video_view.view.*

class MVideoView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    private val player: VideoPlayer = MVideoPlayer()
) :
    FrameLayout(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener,
    VideoPlayer by player {
    companion object {
        private const val TAG = "MVideoView"
        const val MODE_NORMAL = 1
        const val MODE_FULLSCREEN = 2
    }

    private var mContext: Context
    private var mContainer: FrameLayout
    private var mSurfaceTexture: SurfaceTexture? = null
    private var isBuffering = false

    private var controller: VideoController? = null

    private var max: Long = 0
    private var mCurrentMode: Int =
        MODE_NORMAL
    private var lastPlayTime: Long = 0
    private var progress: Long = 0
    private var playerListener = object :
        PlayListener {
        override fun onRenderingStart() {
            Log.d(TAG, "onRenderingStart")

            lastPlayTime = System.currentTimeMillis()
        }

        override fun onBufferStart() {
            Log.d(TAG, "onBufferStart")
            isBuffering = true
        }

        override fun onBufferEnd() {
            Log.d(TAG, "onBufferEnd")
            isBuffering = false
        }

        override fun onPlayComplete() {
            Log.d(TAG, "onPlayComplete")
            progress = 0
            isBuffering = false
            lastPlayTime = 0
            pause()
        }

        override fun onError(msg: String) {
            Log.e(TAG, "video play error $msg")
            isBuffering = false
            progress = 0
            lastPlayTime = 0
        }

        override fun onVideoSizeChanged(width: Int, height: Int) {
            textureView?.adaptVideoSize(width, height)
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        Log.d(TAG, "constructor")
        mContext = context
        val view = LayoutInflater.from(context).inflate(R.layout.layout_video_view, null)
        mContainer = if (view is FrameLayout) {
            view
        } else {
            view.findViewById(R.id.flContainer)
        }
        addView(mContainer)
    }

    override fun setController(videoController: VideoController) {
        controller = videoController
        player.setController(videoController)
        mContainer.addView(controller?.containerView())
    }

    fun getController(): VideoController? {
        return controller
    }

    override fun init() {
        player.init()
        player.setListener(playerListener)
        if (textureView.isAvailable) {
            player.setSurface(Surface(textureView.surfaceTexture))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow")

        initView()
        initListener()
    }

    private fun initView() {
        textureView.keepScreenOn = true
    }

    fun setMax(duration: Long) {
        max = duration
    }

    private fun initListener() {
        Log.d(TAG, "initListener")
        textureView.surfaceTextureListener = this
    }

    fun updateProgress(): Array<Int>? {
        if (player.isPlaying() && !isBuffering) {
            var duration = player.duration().toInt()
            val progress: Int
            // duration为0时，表示当前视频为直播流，此时以手动设置的max值作为duration，
            if (duration == 0) {
                // lastPlayTime为0时表示还未开始播放视频，此时不计时，返回true
                if (lastPlayTime == 0L) {
                    return null;
                }

                duration = max.toInt()
                val curr = System.currentTimeMillis()
                this@MVideoView.progress += curr - lastPlayTime
                lastPlayTime = curr
                progress = this@MVideoView.progress.toInt()

                if (max in 1..progress) {
                    Log.d(TAG, "updateProgress: mock play complete event")
                    controller?.onPlayComplete()
                }
            } else {
                progress = player.currentPosition().toInt()
            }

            return arrayOf(progress, duration)
        }
        return null
    }

    override fun seekTo(position: Long) {
        progress = position
        if (player.duration() != 0L) {
            player.seekTo(position)
        }
    }

    fun isStarting(): Boolean {
        player.apply {
            return status >= MVideoPlayer.PREPARING && status < MVideoPlayer.PLAY_START
        }
    }

    override fun stop() {
        if (player.status == MVideoPlayer.INIT || player.status == MVideoPlayer.PREPARED) {
            return
        }

        Log.d(TAG, "stop")
        player.stop()
        isBuffering = false
        progress = 0
        lastPlayTime = 0
        if (textureView.isAvailable) {
            player.setSurface(Surface(textureView.surfaceTexture))
        }
    }

    override fun release() {
        isBuffering = false
        progress = 0
        lastPlayTime = 0
        player.release()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "onDetachedFromWindow")
        mSurfaceTexture?.release()
        mSurfaceTexture = null
    }

    /**
     * 全屏，将mContainer(内部包含mTextureView和mController)从当前容器中移除，并添加到android.R.content中.
     * 切换横屏时需要在manifest的activity标签下添加android:configChanges="orientation|keyboardHidden|screenSize"配置，
     * 以避免Activity重新走生命周期
     */
    @SuppressLint("SourceLockedOrientationActivity", "ClickableViewAccessibility")
    fun enterFullScreen() {
        if (mCurrentMode == MODE_FULLSCREEN) return
        // 隐藏ActionBar、状态栏，并横屏
        val activity = NiceUtil.scanForActivity(mContext)
        ActivityManager.getInstance().hideNavigationBar(activity)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val contentView = activity.findViewById(android.R.id.content) as ViewGroup
        removeView(mContainer)
        val params =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        contentView.addView(mContainer, params)
        mContainer.setOnTouchListener { view: View, motionEvent: MotionEvent ->
            true
        }
        mCurrentMode =
            MODE_FULLSCREEN
    }

    fun onBackPressed(): Boolean {
        if (isFullScreen()) {
            exitFullScreen()
            return true
        }
        return false
    }

    @SuppressLint("SourceLockedOrientationActivity", "ClickableViewAccessibility")
    fun exitFullScreen(): Boolean {
        if (mCurrentMode == MODE_FULLSCREEN) {
            val activity = NiceUtil.scanForActivity(mContext)
            ActivityManager.getInstance().restoreActivity(activity)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val contentView = activity.findViewById(android.R.id.content) as ViewGroup
            contentView.removeView(mContainer)
            val params =
                LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            this.addView(mContainer, params)
            mContainer.setOnTouchListener(null)
            mCurrentMode =
                MODE_NORMAL
            return true
        }
        return false
    }

    fun isFullScreen(): Boolean {
        return mCurrentMode == MODE_FULLSCREEN
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
//        Log.d(tag, "onSurfaceTextureSizeChanged")
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
//        Log.d(tag, "onSurfaceTextureUpdated")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
//        Log.d(tag, "onSurfaceTextureDestroyed")
//        surface?.release()
        return mSurfaceTexture == null
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
//        Log.d(tag, "onSurfaceTextureAvailable")
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface
            player.setSurface(Surface(mSurfaceTexture))
        } else {
            textureView.surfaceTexture = mSurfaceTexture
        }
    }
}