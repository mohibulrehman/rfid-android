package com.zebra.rfid.demo.sdksample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zebra.rfid.demo.sdksample.utils.Executor


abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var mContext: Context
    protected val TAG = this.javaClass.simpleName
    protected val executor = Executor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this

    }

    override fun onClick(view: View?) {
        Log.d(TAG, "onClick: clicked view  = ${view?.id}")
    }


    override fun onDestroy() {
        super.onDestroy()
        executor.release()
    }

    protected fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    fun openNextActivity(mainActivityClass: Class<*>?, destroyThis: Boolean = false) {
        startActivity(Intent(this, mainActivityClass))
        if (destroyThis) finish()
    }

    fun openPreviousActivity(mainActivityClass: Class<*>?, destroyThis: Boolean = false) {
        startActivity(Intent(this, mainActivityClass))
        if (destroyThis) finish()
    }

}