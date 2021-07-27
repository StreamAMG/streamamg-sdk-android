package com.streamamg.amg_playkit.analytics

import com.kaltura.playkit.PKLog
import java.util.*

class AMGViewTimer {
    private val log = PKLog.get("ViewTimer")

    val REFRESH_PERIOD_IN_MILLISECONDS = 6000 // 10000
    private val ONE_SECOND_IN_MS: Long = 1000
    val MAX_ALLOWED_VIEW_IDLE_TIME: Long = 30000

    private var viewEventTimeCounter = 0
    private var viewEventIdleCounter = 0

    private var isPaused = false
    private var viewEventsEnabled = true

    private var viewEventTimer: Timer? = null
    private var viewEventTrigger: ViewEventTrigger? = null

    interface ViewEventTrigger {
        /**
         * Called when VIEW event should be sent.
         */
        fun onTriggerViewEvent()

        /**
         * Called when VIEW event was not sent for 30 seconds.
         */
        fun onResetViewEvent()

        /**
         * Triggered every 1000ms
         */
        fun onTick()
    }

    fun start() {
        log.d("Kava - StartTimer")
        stop()
        viewEventTimer = Timer()
        viewEventTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (viewEventsEnabled) {
                    if (isPaused) {
                        viewEventIdleCounter += ONE_SECOND_IN_MS.toInt()
                        if (viewEventTrigger != null && viewEventIdleCounter >= MAX_ALLOWED_VIEW_IDLE_TIME) {
                            resetCounters()
                            viewEventTrigger!!.onResetViewEvent()
                        }
                    } else {
                        viewEventTimeCounter += ONE_SECOND_IN_MS.toInt()
                        if (viewEventTrigger != null && viewEventTimeCounter >= REFRESH_PERIOD_IN_MILLISECONDS) {
                            resetCounters()
                            viewEventTrigger!!.onTriggerViewEvent()
                        }
                    }
                }
                if (viewEventTrigger != null) {
                    viewEventTrigger!!.onTick()
                }
            }
        }, 0, ONE_SECOND_IN_MS)
    }

    fun stop() {
        if (viewEventTimer == null) {
            return
        }
        viewEventTimer!!.cancel()
        viewEventTimer = null
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun setViewEventTrigger(viewEventTrigger: ViewEventTrigger?) {
        this.viewEventTrigger = viewEventTrigger
    }

    private fun resetCounters() {
        viewEventIdleCounter = 0
        viewEventTimeCounter = 0
    }

    fun setViewEventsEnabled(viewEventsEnabled: Boolean) {
        if (this.viewEventsEnabled != viewEventsEnabled) {
            resetCounters()
            this.viewEventsEnabled = viewEventsEnabled
        }
    }
}