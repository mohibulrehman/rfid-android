package com.zebra.rfid.demo.sdksample.utils

import kotlinx.coroutines.*


class Executor
{

    private val mainThreadHandler: CoroutineScope = MainScope()
    private val workerThreadHandler: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun release()
    {
        mainThreadHandler.cancel()
        workerThreadHandler.cancel()
    }

    fun runWorker(callback: () -> Unit)
    {
        workerThreadHandler.launch { callback() }
    }

    fun runMain(callback: () -> Unit)
    {
        mainThreadHandler.launch { callback() }
    }

    fun runWorker(delay:Long,callback: () -> Unit) {
        workerThreadHandler.launch {
            delay(delay)
            callback()
        }
    }
}