package com.zebra.rfid.demo.sdksample

import android.content.Context
import android.util.Log
import com.zebra.rfid.api3.*
import com.zebra.rfid.api3.Readers.RFIDReaderEventHandler
import com.zebra.rfid.demo.sdksample.ui.BaseActivity

class RFIDHandler : RFIDReaderEventHandler {
    private var eventHandler: EventHandler? = null

    // UI and context
    //    TextView textView;
    private var context: Context? = null

    // general
    private var MAX_POWER = 270

    // In case of RFD8500 change reader name with intended device below from list of paired RFD8500
    var readername = "RFD8500123"
    var callBack: ResponseHandlerInterface? = null
    var baseActivity: BaseActivity? = null
    var TAG = "RFID_SAMPLE"
    fun init(context1: Context?, callBack1: ResponseHandlerInterface?) {
        context = context1
        baseActivity = context1 as BaseActivity?
        callBack = callBack1
        // SDK
        InitSDK()
    }

    // TEST BUTTON functionality
    // following two tests are to try out different configurations features
    fun Test1(): String {
        // check reader connection
        if (!isReaderConnected) return "Not connected"
        // set antenna configurations - reducing power to 200
        try {
            val config = reader!!.Config.Antennas.getAntennaRfConfig(1)
            config.transmitPowerIndex = 100
            config.setrfModeTableIndex(0)
            config.tari = 0
            reader!!.Config.Antennas.setAntennaRfConfig(1, config)
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
            return e.results.toString() + " " + e.vendorMessage
        }
        return "Antenna power Set to 220"
    }

    fun Test2(): String {
        // check reader connection
        if (!isReaderConnected) return "Not connected"
        // Set the singulation control to S2 which will read each tag once only
        try {
            val s1_singulationControl = reader!!.Config.Antennas.getSingulationControl(1)
            s1_singulationControl.session = SESSION.SESSION_S2
            s1_singulationControl.Action.inventoryState = INVENTORY_STATE.INVENTORY_STATE_A
            s1_singulationControl.Action.slFlag = SL_FLAG.SL_ALL
            reader!!.Config.Antennas.setSingulationControl(1, s1_singulationControl)
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
            return e.results.toString() + " " + e.vendorMessage
        }
        return "Session set to S2"
    }

    fun Defaults(): String {
        // check reader connection
        if (!isReaderConnected) return "Not connected"
        try {
            // Power to 270
            val config = reader!!.Config.Antennas.getAntennaRfConfig(1)
            config.transmitPowerIndex = MAX_POWER
            config.setrfModeTableIndex(0)
            config.tari = 0
            reader!!.Config.Antennas.setAntennaRfConfig(1, config)
            // singulation to S0
            val s1_singulationControl = reader!!.Config.Antennas.getSingulationControl(1)
            s1_singulationControl.session = SESSION.SESSION_S0
            s1_singulationControl.Action.inventoryState = INVENTORY_STATE.INVENTORY_STATE_A
            s1_singulationControl.Action.slFlag = SL_FLAG.SL_ALL
            reader!!.Config.Antennas.setSingulationControl(1, s1_singulationControl)
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
            return e.results.toString() + " " + e.vendorMessage
        }
        return "Default settings applied"
    }

    private val isReaderConnected: Boolean
        private get() = if (reader != null && reader!!.isConnected) true else {
            Log.d(TAG, "reader is not connected")
            false
        }

    //
    //  Activity life cycle behavior
    //
    fun onResume(): String {
        return connect()
    }

    fun onPause() {
        disconnect()
    }

    fun onDestroy() {
        dispose()
    }

    //
    // RFID SDK
    //
    private fun InitSDK() {
        Log.d(TAG, "InitSDK")
        if (readers == null) {
            CreateInstanceTaskFun()
        } else ConnectionTaskFun()
    }

    fun CreateInstanceTaskFun() {
        baseActivity!!.executor.runWorker {
            Log.d(TAG, "CreateInstanceTask")
            // Based on support available on host device choose the reader type
            val invalidUsageException: InvalidUsageException? = null
            readers = Readers(context, ENUM_TRANSPORT.ALL)
            try {
                availableRFIDReaderList = readers!!.GetAvailableRFIDReaderList()
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            }
            if (invalidUsageException != null) {
                readers!!.Dispose()
                readers = null
                if (readers == null) {
                    readers = Readers(context, ENUM_TRANSPORT.BLUETOOTH)
                }
            }
            baseActivity!!.executor.runMain {
                ConnectionTaskFun()
                null
            }
            null
        }
    }

    var responseStr = ""
    fun ConnectionTaskFun() {
        responseStr = ""
        baseActivity!!.executor.runWorker {
            Log.d(TAG, "ConnectionTask")
            GetAvailableReader()
            responseStr = if (reader != null) {
                connect()
            } else "Failed to find or connect reader"

            baseActivity!!.executor.runMain {
                callBack!!.TextChanged(responseStr)
                null
            }
            null
        }
    }

    // Enumerates SDK based on host device
    @Synchronized
    private fun GetAvailableReader() {
        Log.d(TAG, "GetAvailableReader")
        if (readers != null) {
            Readers.attach(this)
            try {
                if (readers!!.GetAvailableRFIDReaderList() != null) {
                    availableRFIDReaderList = readers!!.GetAvailableRFIDReaderList()
                    if (availableRFIDReaderList.size != 0) {
                        // if single reader is available then connect it
                        var readerDevice: ReaderDevice
                        if (availableRFIDReaderList.size == 1) {
                            readerDevice = availableRFIDReaderList[0]
                            reader = readerDevice.rfidReader
                        } else {
                            // search reader specified by name
                            for (device in availableRFIDReaderList) {
                                if (device.name == readername) {
                                    readerDevice = device
                                    reader = readerDevice.rfidReader
                                }
                            }
                        }
                    }
                }
            } catch (ie: InvalidUsageException) {
                Log.e(TAG, "GetAvailableReader: InvalidUsageException " + ie.vendorMessage)
            }
        }
    }

    // handler for receiving reader appearance events
    override fun RFIDReaderAppeared(readerDevice: ReaderDevice) {
        Log.d(TAG, "RFIDReaderAppeared " + readerDevice.name)
        ConnectionTaskFun()
    }

    override fun RFIDReaderDisappeared(readerDevice: ReaderDevice) {
        Log.d(TAG, "RFIDReaderDisappeared " + readerDevice.name)
        if (readerDevice.name == reader!!.hostName) disconnect()
    }

    @Synchronized
    private fun connect(): String {
        if (reader != null) {
            Log.d(TAG, "connect " + reader!!.hostName)
            try {
                if (!reader!!.isConnected) {
                    // Establish connection to the RFID Reader
                    reader!!.connect()
                    ConfigureReader()
                    return "Connected"
                }
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            } catch (e: OperationFailureException) {
                e.printStackTrace()
                Log.d(TAG, "OperationFailureException " + e.vendorMessage)
                val des = e.results.toString()
                return "Connection failed" + e.vendorMessage + " " + des
            }
        }
        return ""
    }

    private fun ConfigureReader() {
        Log.d(TAG, "ConfigureReader " + reader!!.hostName)
        if (reader!!.isConnected) {
            val triggerInfo = TriggerInfo()
            triggerInfo.StartTrigger.triggerType = START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE
            triggerInfo.StopTrigger.triggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE
            try {
                // receive events from reader
                if (eventHandler == null) eventHandler = EventHandler()
                reader!!.Events.addEventsListener(eventHandler)
                // HH event
                reader!!.Events.setHandheldEvent(true)
                // tag event with tag data
                reader!!.Events.setTagReadEvent(true)
                reader!!.Events.setAttachTagDataWithReadEvent(false)
                // set trigger mode as rfid so scanner beam will not come
                reader!!.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
                // set start and stop triggers
                reader!!.Config.startTrigger = triggerInfo.StartTrigger
                reader!!.Config.stopTrigger = triggerInfo.StopTrigger
                // power levels are index based so maximum power supported get the last one
                MAX_POWER = reader!!.ReaderCapabilities.transmitPowerLevelValues.size - 1
                // set antenna configurations
                val config = reader!!.Config.Antennas.getAntennaRfConfig(1)
                config.transmitPowerIndex = MAX_POWER
                config.setrfModeTableIndex(0)
                config.tari = 0
                reader!!.Config.Antennas.setAntennaRfConfig(1, config)
                // Set the singulation control
                val s1_singulationControl = reader!!.Config.Antennas.getSingulationControl(1)
                s1_singulationControl.session = SESSION.SESSION_S0
                s1_singulationControl.Action.inventoryState = INVENTORY_STATE.INVENTORY_STATE_A
                s1_singulationControl.Action.slFlag = SL_FLAG.SL_ALL
                reader!!.Config.Antennas.setSingulationControl(1, s1_singulationControl)
                // delete any prefilters
                reader!!.Actions.PreFilters.deleteAll()
                //
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            } catch (e: OperationFailureException) {
                e.printStackTrace()
            }
        }
    }

    @Synchronized
    private fun disconnect() {
        Log.d(TAG, "disconnect " + reader)
        try {
            if (reader != null) {
                reader!!.Events.removeEventsListener(eventHandler)
                reader!!.disconnect()
                baseActivity!!.runOnUiThread { callBack!!.TextChanged("Disconnected") }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun dispose() {
        try {
            if (readers != null) {
                reader = null
                readers!!.Dispose()
                readers = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun performInventory() {
        // check reader connection
        if (!isReaderConnected) return
        try {
            reader!!.Actions.Inventory.perform()
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun stopInventory() {
        // check reader connection
        if (!isReaderConnected) return
        try {
            reader!!.Actions.Inventory.stop()
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        }
    }

    // Read/Status Notify handler
    // Implement the RfidEventsLister class to receive event notifications
    inner class EventHandler : RfidEventsListener {
        // Read Event Notification
        override fun eventReadNotify(e: RfidReadEvents) {
            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            val myTags = reader!!.Actions.getReadTags(100)
            if (myTags != null) {
                for (myTag in myTags) {
                    Log.d(TAG, "Tag ID " + myTag.tagID)
                    if (myTag.opCode === ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ && myTag.opStatus === ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (myTag.memoryBankData.length > 0) {
                            Log.d(TAG, " Mem Bank Data " + myTag.memoryBankData)
                        }
                    }
                    if (myTag.isContainsLocationInfo) {
                        val dist = myTag.LocationInfo.relativeDistance
                        Log.d(TAG, "Tag relative distance $dist")
                    }
                }
                // possibly if operation was invoked from async task and still busy
                // handle tag data responses on parallel thread thus THREAD_POOL_EXECUTOR
//                new AsyncDataUpdate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, myTags);
                AsyncDataUpdateFun(myTags)
            }
        }

        // Status Event Notification
        override fun eventStatusNotify(rfidStatusEvents: RfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.statusEventType)
            if (rfidStatusEvents.StatusEventData.statusEventType === STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    baseActivity!!.executor.runWorker {
                        callBack!!.handleTriggerPress(true)
                        null
                    }
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    baseActivity!!.executor.runWorker {
                        callBack!!.handleTriggerPress(false)
                        null
                    }
                }
            }
        }
    }

    fun AsyncDataUpdateFun(params: Array<TagData>) {

        baseActivity!!.executor.runWorker {
            callBack!!.handleTagdata(params)
            null
        }
    }

    interface ResponseHandlerInterface {
        fun handleTagdata(tagData: Array<TagData>)
        fun handleTriggerPress(pressed: Boolean)
        fun TextChanged(text: String)

        //void handleStatusEvents(Events.StatusEventData eventData);
    }

    companion object {
        // RFID Reader
        private var readers: Readers? = null
        private var availableRFIDReaderList = ArrayList<ReaderDevice>()
        private var reader: RFIDReader? = null
    }
}