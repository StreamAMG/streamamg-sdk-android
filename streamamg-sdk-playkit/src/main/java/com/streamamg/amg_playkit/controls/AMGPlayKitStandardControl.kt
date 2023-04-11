package com.streamamg.amg_playkit.controls

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.annotation.ColorRes
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.marginEnd
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.streamamg.amg_playkit.AMGPlayKit
import com.streamamg.amg_playkit.R
import com.streamamg.amg_playkit.constants.AMGMediaType
import com.streamamg.amg_playkit.constants.AMGPlayKitPlayState
import com.streamamg.amg_playkit.interfaces.AMGControlInterface
import com.streamamg.amg_playkit.interfaces.AMGPlayerInterface
import com.streamamg.amg_playkit.models.AMGPlayKitStandardControlsConfigurationModel
import com.streamamg.amg_playkit.models.MediaTrack
import com.streamamg.amg_playkit.playkitExtensions.FlavorAsset
import java.util.*


class AMGPlayKitStandardControl : LinearLayout, AMGControlInterface {

    lateinit var controlsView: ConstraintLayout
    lateinit var playPauseButton: ImageView
    lateinit var skipForwardButton: ImageView
    lateinit var skipBackwardButton: ImageView
    lateinit var fullScreenButton: ImageView
    lateinit var settingsButton: ImageButton
    lateinit var subtitleButton: ImageButton
    lateinit var scrubBar: SeekBar
    lateinit var startTime: TextView
    //lateinit var endTime: TextView
    lateinit var liveButton: TextView
    var showingTimes = false
    var playIcon: Int = R.drawable.ic_play_button
    var pauseIcon: Int = R.drawable.ic_video_pause
    var fullScreenIcon: Int = R.drawable.ic_fullscreen_button
    var minimiseIcon: Int = R.drawable.ic_minimise_button

    var playerState: AMGPlayKitPlayState = AMGPlayKitPlayState.idle

    var player: AMGPlayerInterface? = null

    var duration: Long = 0L

    var fadeTime: Long = 5000

    var selectedBitrate = 0
    var selectedCaption: Int = 0

    var shouldHideOnOrientation = -1
    lateinit var mainView: ConstraintLayout
    lateinit var bottomScrubBar: LinearLayout
    lateinit var bottomScrubBarTrack: View
    lateinit var bottomScrubBarBlank: View

    lateinit var mainScrubBarView: LinearLayout
    lateinit var spoilerFreeView: LinearLayout
    lateinit var bitrateSelectorView: LinearLayout
    lateinit var subtitleSelectorView: LinearLayout
    lateinit var subtitleContainerView: CardView
    lateinit var spoilerFreeLeftView: View
    lateinit var spoilerFreeRightView: View


    private var scrubBarLiveColour: Int = R.color.amg_live_track
    private var scrubBarVODColour: Int = R.color.amg_vod_track

    var bottomTrackShouldShow = false

    var spoilerFreeEnabled = false


    constructor(context: Context, partnerID: Int) : super(context) {
        setUpView(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setUpAttributes(context, attributeSet)
        setUpView(context)
    }

    private fun setUpView(context: Context) {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.controls_view, this, false)
        controlsView = view as ConstraintLayout
        playPauseButton = controlsView.findViewById(R.id.play_pause_button)
        skipBackwardButton = controlsView.findViewById(R.id.skip_backwards_button)
        skipForwardButton = controlsView.findViewById(R.id.skip_forwards_button)
        fullScreenButton = controlsView.findViewById(R.id.fullscreen_button)
        settingsButton = controlsView.findViewById(R.id.settings_button)
        subtitleButton = controlsView.findViewById(R.id.subtitle_button)
        scrubBar = controlsView.findViewById(R.id.scrub_bar)
        startTime = controlsView.findViewById(R.id.start_time)
        liveButton = controlsView.findViewById(R.id.live_button)
      //  endTime = controlsView.findViewById(R.id.end_time)
        mainView = controlsView.findViewById(R.id.main_view)
        bottomScrubBar = controlsView.findViewById(R.id.bottom_scrub_view)
        bottomScrubBarTrack = controlsView.findViewById(R.id.bottom_scrub_view_track)
        bottomScrubBarBlank = controlsView.findViewById(R.id.bottom_scrub_view_track_blank)

        mainScrubBarView = controlsView.findViewById(R.id.scrub_bar_view)
        spoilerFreeView = controlsView.findViewById(R.id.spoiler_free_view)
        spoilerFreeLeftView = controlsView.findViewById(R.id.spoiler_free_left_view)
        spoilerFreeRightView = controlsView.findViewById(R.id.spoiler_free_right_view)
        bitrateSelectorView = controlsView.findViewById(R.id.bitrate_selector_view)
        subtitleSelectorView = controlsView.findViewById(R.id.subtitle_selector_view)
        subtitleContainerView = controlsView.findViewById(R.id.cardSubtitle)

        addView(controlsView, MATCH_PARENT, MATCH_PARENT)
        configureView()

    }

    fun configureView(config: AMGPlayKitStandardControlsConfigurationModel? = null) {
        var configModel = config
        if (configModel == null) {
            configModel = AMGPlayKitStandardControlsConfigurationModel()
        }

        configModel.let {
            fadeTime = it.fadeOutAfter

            var playPauseParams = playPauseButton.layoutParams as ConstraintLayout.LayoutParams

            playPauseParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            playPauseParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

            settingsButton.visibility = if (it.bitrateSelector) View.VISIBLE else View.GONE
            subtitleButton.visibility = if (it.subtitleSelector) View.VISIBLE else View.GONE

            if (settingsButton.visibility == VISIBLE) subtitleContainerView.margin(end = 75F)

            showingTimes = it.trackTimeShowing
            if (showingTimes) {
                startTime.visibility = View.VISIBLE
        //        endTime.visibility = View.VISIBLE
            } else {
                startTime.visibility = View.INVISIBLE
          //      endTime.visibility = View.GONE
            }

            shouldHideOnOrientation = -1
            if (it.hideFullscreenOnFS){
                shouldHideOnOrientation = 0
            }

            if (it.hideFullscreen){
                shouldHideOnOrientation = 1
                hideFullScreenButton(0)
            }

            if (it.playImage > 0) {
                playIcon = it.playImage
                playPauseButton.setImageResource(playIcon)
            }

            if (it.pauseImage > 0) {
                pauseIcon = it.pauseImage
            }

            if (it.skipBackwardImage > 0) {
                skipBackwardButton.setImageResource(it.skipBackwardImage)
            }

            if (it.skipForwardImage > 0) {
                skipForwardButton.setImageResource(it.skipForwardImage)
            }

            if (it.fullScreenImage > 0) {
                fullScreenButton.setImageResource(it.fullScreenImage)
            }

            if (it.minimiseImage > 0) {
                fullScreenButton.setImageResource(it.fullScreenImage)
            }

            if (it.scrubBarLiveColour > 0) {
                scrubBarLiveColour = it.scrubBarLiveColour
            }

            if (it.scrubBarVODColour > 0) {
                scrubBarVODColour = it.scrubBarVODColour
            }

            liveButton.setOnClickListener {
                player?.goLive()
            }

        }
        wireControls()
    }

    private fun wireControls() {
        playPauseButton.setOnClickListener {
            if (playerState == AMGPlayKitPlayState.playing) {
                player?.pause()
            } else {
                player?.play()
            }
        }

        scrubBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player?.scrub((progress * 1000).toLong())
                    player?.startControlVisibilityTimer()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                player?.cancelTimer()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                player?.startControlVisibilityTimer()
            }
        })

        skipForwardButton.setOnClickListener {
            player?.skipForward()
            player?.startControlVisibilityTimer()
        }

        skipBackwardButton.setOnClickListener {
            player?.skipBackward()
            player?.startControlVisibilityTimer()
        }


        fullScreenButton.setOnClickListener {
            player?.swapOrientation()
            player?.startControlVisibilityTimer()
        }

        settingsButton.setOnClickListener {
            toggleSubtitleSelector(true)
            toggleBitrateSelector(bitrateSelectorView.visibility == View.VISIBLE)
        }

        subtitleButton.setOnClickListener {
            toggleBitrateSelector(true)
            toggleSubtitleSelector(subtitleSelectorView.visibility == View.VISIBLE)
        }
    }

    internal fun toggleBitrateSelector(hide: Boolean) {
        if (hide) {
            bitrateSelectorView.visibility = View.GONE
            settingsButton.setBackgroundResource(R.color.transparent)
            player?.startControlVisibilityTimer()
        } else {
            bitrateSelectorView.visibility = View.VISIBLE
            settingsButton.setBackgroundResource(R.drawable.rounded_bg)
            player?.cancelTimer()
        }
    }

    internal fun toggleSubtitleSelector(hide: Boolean) {
        if (hide) {
            subtitleSelectorView.visibility = View.GONE
            subtitleButton.setBackgroundResource(R.color.transparent)
            player?.startControlVisibilityTimer()
        } else {
            subtitleSelectorView.visibility = View.VISIBLE
            subtitleButton.setBackgroundResource(R.drawable.rounded_bg)
            player?.cancelTimer()
        }
    }

    internal fun hideFullScreenButton(orientation: Int) {
        if (orientation == 0){
            fullScreenButton.setImageResource(minimiseIcon)
        } else {
            fullScreenButton.setImageResource(fullScreenIcon)
        }
        if (shouldHideOnOrientation == -1){
            fullScreenButton.visibility = VISIBLE
            return
        }
        if (shouldHideOnOrientation == 1){
            fullScreenButton.visibility = GONE
            return
        }
        if (orientation == 0){
            fullScreenButton.visibility = GONE
        } else {
            fullScreenButton.visibility = VISIBLE
        }
    }

    private fun setUpAttributes(context: Context, attributeSet: AttributeSet) {

    }

    fun setIsLive(){
        setLiveColours()
    }

    fun setIsVOD() {
        setVodColours()
    }

    fun setIsAudio() {
        setVodColours()
    }

    fun setIsAudioLive() {
        setLiveColours()
    }

    internal fun setLiveColours(){
        bottomScrubBarTrack.setBackgroundResource(scrubBarLiveColour)
        scrubBar.progressDrawable.colorFilter =  PorterDuffColorFilter(ContextCompat.getColor(context, scrubBarLiveColour), PorterDuff.Mode.MULTIPLY) ///setColorFilter(scrubBarLiveColour, PorterDuff.Mode.MULTIPLY)
        spoilerFreeLeftView.setBackgroundResource(scrubBarLiveColour)
        spoilerFreeRightView.setBackgroundResource(scrubBarLiveColour)
        setSpoilerFree(spoilerFreeEnabled)
        startTime.visibility = INVISIBLE
        liveButton.visibility = VISIBLE
        liveButton.setTextColor(ContextCompat.getColor(context, scrubBarLiveColour))
    }

    internal fun setVodColours(){
        bottomScrubBarTrack.setBackgroundResource(scrubBarVODColour)
        scrubBar.progressDrawable.colorFilter =  PorterDuffColorFilter(ContextCompat.getColor(context, scrubBarVODColour), PorterDuff.Mode.MULTIPLY) ///setColorFilter(scrubBarLiveColour, PorterDuff.Mode.MULTIPLY)
        spoilerFreeLeftView.setBackgroundResource(scrubBarVODColour)
        spoilerFreeRightView.setBackgroundResource(scrubBarVODColour)
        setSpoilerFree(spoilerFreeEnabled)
        startTime.visibility = VISIBLE
        liveButton.visibility = GONE
        liveButton.setTextColor(ContextCompat.getColor(context, scrubBarVODColour))
    }

    fun setSpoilerFree(isSF: Boolean){
        spoilerFreeEnabled = isSF
        spoilerFreeView.visibility = if (spoilerFreeEnabled) VISIBLE else GONE
        mainScrubBarView.visibility = if (!spoilerFreeEnabled) VISIBLE else GONE
    }

    override fun play() {
        playerState = AMGPlayKitPlayState.playing
        playPauseButton.setImageResource(pauseIcon)
    }

    override fun pause() {
        playerState = AMGPlayKitPlayState.paused
        playPauseButton.setImageResource(playIcon)
    }

    override fun changePlayHead(position: Long) {
 //       Log.d("AMGISLIVE", "Playhead: $position - duration: $duration - scrubmax: ${scrubBar.max}")
        scrubBar.progress = (position / 1000).toInt()
        val timeRemaining = duration - position
        startTime.text = ""
        startTime.append("${timeForDisplay(position)}", R.color.white)
        startTime.append(" / ${timeForDisplay(duration)}", R.color.white_opacity_70)
      //  endTime.text = timeForDisplay(timeRemaining)

        val percentage = position.toFloat() / duration.toFloat()
        val lpt = LayoutParams(0, MATCH_PARENT, percentage)
        bottomScrubBarTrack.layoutParams = lpt
        val lpb = LayoutParams(0, MATCH_PARENT, 1 - percentage)
        bottomScrubBarBlank.layoutParams = lpb

        if (scrubBar.progress < scrubBar.max - 50) {
            liveButton.setText(R.string.go_live)
        } else {
            liveButton.setText(R.string.live)
        }
    }

    fun paddedInt(toPad: Int): String {
        var rtn = toPad.toString()
        if (rtn.count() == 1){
            return "0$rtn"
        }
        return rtn
    }

    fun timeForDisplay(time: Long): String {
        var totalSeconds: Int = time.toInt() / 1000

        var totalMinutes = totalSeconds / 60
        totalSeconds -= (totalMinutes * 60)

        var totalHours = totalMinutes / 60
        totalMinutes -= (totalHours * 60)

        var display = ""

        if (totalHours > 0) {
            display = "${paddedInt(totalHours)}:"
        }

         return "$display${paddedInt(totalMinutes)}:${paddedInt(totalSeconds)}"

    }


    override fun changeMediaLength(length: Long) {
        if (length <= 1000) {
            scrubBar.max = 1
        } else {
            scrubBar.max = (length / 1000).toInt()
        }
        duration = length
        scrubBar.progress = 0
        updateTimes()
    }

    private fun updateTimes() {
        if (showingTimes) {
            startTime.text = "0:00"
      //      endTime.text = timeFromLong(duration)
        }
    }

    private fun timeFromLong(time: Long): String {
        var seconds: Long = (time / 1000)
        var hoursText = ""
        if (seconds >= 3600) {
            val hours = seconds / 3600
            seconds -= (hours * 3600)
            hoursText = "${longToTimeString(hours)}:"
        }
        val minutes = seconds / 60
        seconds -= (minutes * 60)
        return "$hoursText${longToTimeString(minutes)}:${longToTimeString(seconds)}"
    }

    private fun longToTimeString(time: Long): String {
        var returner = time.toString()
        if (returner.isEmpty()) {
            return "00"
        }

        if (returner.length == 1) {
            return "0$returner"
        }

        return returner
    }

    fun showControls(shouldShow: Boolean) {
        if (shouldShow) {
            mainView.visibility = VISIBLE
            bottomScrubBar.visibility = GONE
        } else {
            mainView.visibility = INVISIBLE
            bitrateSelectorView.visibility = GONE
            subtitleSelectorView.visibility = GONE
            if (bottomTrackShouldShow) {
                bottomScrubBar.visibility = VISIBLE
            }
        }

    }

    fun setMediaType(mediaType: AMGMediaType) {
        when (mediaType) {
            AMGMediaType.Live -> setIsLive()
            AMGMediaType.Live_Audio -> setIsAudioLive()
            AMGMediaType.VOD -> setIsVOD()
            AMGMediaType.Audio -> setIsAudio()
        }
    }

    fun TextView.append(string: String?, @ColorRes color: Int) {
        if (string == null || string.isEmpty()) {
            return
        }

        val spannable: Spannable = SpannableString(string)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, color)),
            0,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        append(spannable)
    }

    fun createSubtitlesSelector(subtitles: List<MediaTrack>) {
        subtitleSelectorView.removeAllViews()
        subtitles.forEachIndexed { index, mediaTrack ->
            subtitleSelectorView.addView(listDivider())
            subtitleSelectorView.addView(subtitleButton(mediaTrack, index))
        }
    }

    private fun subtitleButton(track: MediaTrack, index: Int): Button {
        val btnSubtitle = standardButton()
        btnSubtitle.tag = index
        track?.label?.let {
            if (it == "none") {
                btnSubtitle.setText(R.string.subtitle_off)
            } else {
                btnSubtitle.text = it
            }
        }

        toggleSubtitleButton(index, btnSubtitle, selectedCaption == index)

        btnSubtitle.setOnClickListener {
            Log.d("SUBTITLE","Selected subtitle: ${track?.label}")

            // Previous subtitle back to normal status
            val btnPrevSubtitle = subtitleSelectorView.findViewWithTag<Button>(selectedCaption)
            toggleSubtitleButton(selectedCaption, btnPrevSubtitle, false)
            // Select new subtitle
            toggleSubtitleButton(index, btnSubtitle, true)

            player?.setTrack(track)
            subtitleSelectorView.visibility = View.GONE
            subtitleButton.setBackgroundResource(R.color.transparent)
            selectedCaption = index
        }

        return btnSubtitle
    }

    private fun toggleSubtitleButton(index: Int, button: Button?, selected: Boolean) {
        if (selected) {
            // Text bold, background darker and V icon
            button?.setBackgroundResource(R.color.option_dark_gray)
            button?.setTypeface(null, Typeface.BOLD)
            button?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checkmark, 0)
        } else {
            // Normal state
            button?.setBackgroundResource(R.color.option_medium_gray)
            button?.setTypeface(null, Typeface.NORMAL)
            button?.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
        }

        // Rounded top corner for first element (the rounded corner for last element is in CardView [parent container])
        if (index == 0) {
            val shapeAppearanceModel: ShapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 8 * resources.displayMetrics.density)
                .setTopRightCorner(CornerFamily.ROUNDED, 8 * resources.displayMetrics.density)
                .build()

            val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
            shapeDrawable.fillColor = ContextCompat.getColorStateList(context, if (selected) R.color.option_dark_gray else R.color.option_medium_gray)
            button?.let { ViewCompat.setBackground(it, shapeDrawable) }
        }
    }

    fun createBitrateSelector(bitrates: List<FlavorAsset>? = null) {
        bitrateSelectorView.removeAllViews()
        bitrateSelectorView.addView(bitrateButton(bitrates?.lastOrNull(), 0, "Auto"))
        bitrates?.forEachIndexed { index, bitrate ->
            bitrateSelectorView.addView(listDivider())
            bitrateSelectorView.addView(bitrateButton(bitrate, index+1, "${bitrate.bitrate}"))
        }
    }

    private fun toggleBitrateButton(index: Int, button: Button?, selected: Boolean) {
        if (selected) {
            // Text bold, background darker and V icon
            button?.setBackgroundResource(R.color.option_dark_gray)
            button?.setTypeface(null, Typeface.BOLD)
            button?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checkmark, 0)
        } else {
            // Normal state
            button?.setBackgroundResource(R.color.option_medium_gray)
            button?.setTypeface(null, Typeface.NORMAL)
            button?.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
        }

        // Rounded top corner for first element (the rounded corner for last element is in CardView [parent container])
        if (index == 0) {
            val shapeAppearanceModel: ShapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 8 * resources.displayMetrics.density)
                .setTopRightCorner(CornerFamily.ROUNDED, 8 * resources.displayMetrics.density)
                .build()

            val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
            shapeDrawable.fillColor = ContextCompat.getColorStateList(context, if (selected) R.color.option_dark_gray else R.color.option_medium_gray)
            button?.let { ViewCompat.setBackground(it, shapeDrawable) }
        }
    }

    private fun standardButton(): Button {
        val button = Button(context)
        button.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        button.isAllCaps = false
        button.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        button.setPadding(
            (16 * resources.displayMetrics.density).toInt(),
            (6 * resources.displayMetrics.density).toInt(),
            (16 * resources.displayMetrics.density).toInt(),
            (6 * resources.displayMetrics.density).toInt()
        )
        button.setTextColor(ContextCompat.getColor(context, R.color.white))

        return button
    }

    private fun bitrateButton(bitrate: FlavorAsset?, index: Int, text: String = bitrate.toString()): Button {
        val btnBitrate = standardButton()
        btnBitrate.tag = index
        btnBitrate.text = text

        toggleBitrateButton(index, btnBitrate, selectedBitrate == index)

        btnBitrate.setOnClickListener {
            Log.d("BITRATE","Selected bitrate: ${bitrate?.bitrate}")

            // Previous bitrate back to normal status
            val btnPrevBitrate = bitrateSelectorView.findViewWithTag<Button>(selectedBitrate)
            toggleBitrateButton(selectedBitrate, btnPrevBitrate, false)
            // Select new bitrate
            toggleBitrateButton(index, btnBitrate, true)

            player?.setMaximumBitrate(bitrate)
            bitrateSelectorView.visibility = View.GONE
            settingsButton.setBackgroundResource(R.color.transparent)
            selectedBitrate = index
        }

        return btnBitrate
    }

    private fun listDivider(): View {
        val divider = View(context)
        val layout = FrameLayout.LayoutParams(MATCH_PARENT, (0.5 * resources.displayMetrics.density).toInt())
        layout.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        divider.layoutParams = layout
        divider.setBackgroundResource(R.color.option_dark_gray)

        return divider
    }

    fun setCaptionOnSelector(index: Int) {
        val btnPrevSubtitle = subtitleSelectorView.findViewWithTag<Button>(selectedCaption)
        val btnNewSubtitle = subtitleSelectorView.findViewWithTag<Button>(index)
        toggleSubtitleButton(selectedCaption, btnPrevSubtitle, false)
        // Select new subtitle
        toggleSubtitleButton(index, btnNewSubtitle, true)
        selectedCaption = index
    }
}

fun View.margin(end: Float? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        end?.run { marginEnd = dpToPx(this) }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
