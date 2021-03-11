package com.mymasimo.masimosleep.ui.device_onboarding.screens

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentDeviceOnboardingScreenBinding
import com.mymasimo.masimosleep.ui.device_onboarding.view.FullLayoutVideoView
import com.mymasimo.masimosleep.util.getRawMp4URI

class DeviceOnboardingScreenFragment : Fragment(R.layout.fragment_device_onboarding_screen) {

    private val viewBinding by viewBinding(FragmentDeviceOnboardingScreenBinding::bind)

    companion object {
        private const val TITLE_KEY = "TITLE"
        private const val RES_KEY = "RES"
        private const val SUBTITLE_KEY = "SUBTITLE"
        private const val CONTENT_KEY = "CONTENT"
        private const val BUTTON_TITLE_KEY = "BUTTON_TITLE"
        private const val IS_VIDEO_GUIDE_KEY = "IS_VIDEO_GUIDE"

        fun newInstance(
            title: String?,
            res: Int,
            subTitle: String,
            content: String,
            buttonTitle: String?,
            isVideoGuide: Boolean = true
        ) = DeviceOnboardingScreenFragment().apply {
            arguments = bundleOf(
                TITLE_KEY to title,
                RES_KEY to res,
                SUBTITLE_KEY to subTitle,
                CONTENT_KEY to content,
                BUTTON_TITLE_KEY to buttonTitle,
                IS_VIDEO_GUIDE_KEY to isVideoGuide
            )
        }
    }

    private var title: String? = null
    private var res: Int = 0
    private var subTitle: String? = null
    private var content: String? = null
    private var buttonTitle: String? = null
    private var isVideoGuide: Boolean = true

    private lateinit var listener: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { arg ->
            title = arg.getString(TITLE_KEY)
            res = arg.getInt(RES_KEY)
            subTitle = arg.getString(SUBTITLE_KEY) as String
            content = arg.getString(CONTENT_KEY) as String
            buttonTitle = arg.getString(BUTTON_TITLE_KEY)
            isVideoGuide = arg.getBoolean(IS_VIDEO_GUIDE_KEY, true)
        } ?: throw IllegalArgumentException()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadViewContent()
    }

    private fun loadViewContent() {
        title?.let {
            viewBinding.titleTextView.text = it
            viewBinding.titleTextView.visibility = View.VISIBLE
        } ?: run {
            viewBinding.titleTextView.visibility = View.INVISIBLE
        }

        if (isVideoGuide) {
            viewBinding.contentImageView.visibility = View.GONE
            viewBinding.videoView.visibility = View.VISIBLE

            loadMp4(viewBinding.videoView, res)
        } else {
            viewBinding.videoView.visibility = View.GONE
            viewBinding.contentImageView.visibility = View.VISIBLE
            viewBinding.contentImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, res, null))
        }

        viewBinding.contentTextView.text = content
    }

    fun setOnButtonClickListener(listener: () -> Unit) {
        this.listener = listener
    }

    private fun loadMp4(videoView: FullLayoutVideoView, mp4Res: Int) {
        videoView.alpha = 0F
        videoView.setVideoURI(getRawMp4URI(mp4Res))
        videoView.setOnPreparedListener { mp ->
            //Workaround for black screen when loading
            mp.setOnInfoListener { _, what, _ ->
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    videoView.alpha = 1F
                    return@setOnInfoListener true
                }
                false
            }
            mp.isLooping = true
            startMp4FromBeginning(videoView)
        }
    }

    private fun startMp4FromBeginning(videoView: FullLayoutVideoView) {
        if (!videoView.isPlaying) {
            videoView.setZOrderOnTop(false)
            videoView.seekTo(0)
            videoView.start()
        }
    }
}