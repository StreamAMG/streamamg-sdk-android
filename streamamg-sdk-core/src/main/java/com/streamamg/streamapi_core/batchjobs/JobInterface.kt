package com.streamamg.streamapi_core.batchjobs

interface JobInterface {
    var delegate: BatchInterface?

    open fun fireRequest(){
    }

    open fun runCallback(){
    }

    open fun reset(){
    }

    fun isComplete(): Boolean


}