package com.zebra.rfid.demo.sdksample


import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.zebra.rfid.api3.TagData
import com.zebra.rfid.demo.sdksample.ui.BaseActivity

class MainActivity : BaseActivity() {
    lateinit var statusTextViewRFID: TextView
    lateinit var textrfid: TextView
    lateinit var testStatus: TextView

    lateinit var rfidHandler: RFIDHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)


        // UI
        statusTextViewRFID = findViewById(R.id.textStatus)
        textrfid = findViewById(R.id.textViewdata)
        testStatus = findViewById(R.id.testStatus)

        // RFID Handler
        rfidHandler = RFIDHandler()
        rfidHandler.init(this, object : RFIDHandler.ResponseHandlerInterface {
            override fun TextChanged(text: String) {
                statusTextViewRFID.text = text
            }

            override fun handleTagdata(tagData: Array<TagData>) {
                val sb = StringBuilder()
                for (tagDatum in tagData) {
                    sb.append(tagDatum.tagID).append("\n")
                }
                runOnUiThread { textrfid.append(sb.toString()) }
            }


            override fun handleTriggerPress(pressed: Boolean) {
                if (pressed) {
                    runOnUiThread { textrfid.text = "" }
                    rfidHandler.performInventory()
                } else rfidHandler.stopInventory()
            }

        })


        // set up button click listener
        val test = findViewById<Button>(R.id.button)
        test.setOnClickListener { v: View? ->
            val result = rfidHandler.Test1()
            testStatus.text = result
        }
        val test2 = findViewById<Button>(R.id.button2)
        test2.setOnClickListener { v: View? ->
            val result = rfidHandler.Test2()
            testStatus.text = result
        }
        val defaultButton = findViewById<Button>(R.id.button3)
        defaultButton.setOnClickListener { v: View? ->
            val result = rfidHandler.Defaults()
            testStatus.text = result
        }
    }

    override fun onPause() {
        super.onPause()
        rfidHandler.onPause()
    }

    override fun onPostResume() {
        super.onPostResume()
        val status = rfidHandler.onResume()
        statusTextViewRFID.text = status
    }

    override fun onDestroy() {
        super.onDestroy()
        rfidHandler.onDestroy()
    }
}