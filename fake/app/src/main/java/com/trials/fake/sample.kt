package com.trials.fake

import android.app.Instrumentation
import android.app.UiAutomation
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.Manifest.permission
import android.content.Context
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.util.*


class sample(val context: Context) {

    fun ex(permission: String) {
        val automation = Instrumentation().uiAutomation
        val command = String.format(Locale.ENGLISH, "pm grant %s %s", context.packageName, permission)
        val pfd = automation.executeShellCommand(command)
        val stream = FileInputStream(pfd.fileDescriptor)
        try {
            val buffer = ByteArray(1024)
            while (stream.read(buffer) != -1) {

            }
            Log.d(sample::class.java.simpleName,"result: $buffer")
        } catch (ignored: IOException) {
        } finally {
            try {
                stream.close()
            } catch (ignored: IOException) {
            }
        }
    }
}
