package edu.umich.mlyao.gymbrofe

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

object MachineActivity {
    private const val serverUrl = "https://34.73.120.24/"
    private val client = OkHttpClient()
    private var machine_name = ""
    private var machine_instructions = ""
    private var machine_url = ""


    fun getMachine(label: String?) {
        println("In get machine func")
        val request = Request.Builder()
            .url(serverUrl + "machine/" + label + "/")
            .build()
        println(request)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("getMachine", "Failed GET request")
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val machineReceived = try { JSONObject(response.body?.string() ?: "")
                        .getJSONArray("machine-info") } catch (e: JSONException) { JSONArray() }
                    val machineData = machineReceived as JSONArray
                    machine_name = machineData[0].toString()
                    machine_instructions = machineData[1].toString()
                    machine_url = machineData[2].toString()
                }
            }
        })
    }
}
