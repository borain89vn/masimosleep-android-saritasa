package com.mymasimo.masimosleep.ui.device_onboarding.screens

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.databinding.FragmentDeviceOnboardingScreenBinding
import com.mymasimo.masimosleep.ui.device_onboarding.view.FullLayoutVideoView
import com.mymasimo.masimosleep.util.getRawMp4URI
import com.sprylab.android.widget.TextureVideoView
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

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
    private var isFirstResumed = false
    private var mediaPlayer: MediaPlayer? = null


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
        Log.d("DeviceOnboard_created",isResumed.toString())
        receiveNextEvent()
    }

    override fun onResume() {
        super.onResume()


    }

    override fun onPause() {
        super.onPause()
//        if(viewBinding.videoView.isPlaying) {
//            viewBinding.videoView.stopPlayback()
//
//        }
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

    private fun loadMp4(videoView: TextureVideoView, mp4Res: Int) {
      //  videoView.alpha = 0F
        videoView.setVideoURI(getRawMp4URI(mp4Res))
        videoView.setOnPreparedListener { mp ->

            mp.isLooping = true
            startMp4FromBeginning(videoView)
        }

    }

    private fun startMp4FromBeginning(videoView: TextureVideoView) {
        if (!videoView.isPlaying) {
            videoView.seekTo(0)
            videoView.start()
        }
    }
    private fun receiveNextEvent(){
       parentFragmentManager?.setFragmentResultListener("test",viewLifecycleOwner){ _,_ ->
             if(viewBinding.videoView.isPlaying) {
                 viewBinding.videoView.stopPlayback()
                 viewBinding.videoView.setOnPreparedListener(null)

             }
         }
    }

    private fun test(){
        val shared = MutableSharedFlow<String>("a")
        shared.subscriptionCount
    }
}