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
        val request = Request.Builder()
            .url(serverUrl + "machine/" + label + "/")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("getChatts", "Failed GET request")
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val machineReceived = try { JSONObject(response.body?.string() ?: "")
                        .getJSONArray("machine-info") } catch (e: JSONException) { JSONArray() }
                    val machine_data = machineReceived as JSONArray
                    machine_name = machine_data[0].toString()
                    machine_instructions = machine_data[1].toString()
                    machine_url = machine_data[2].toString()
                }
            }
        })
    }
}
