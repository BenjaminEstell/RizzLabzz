package edu.umich.mlyao.gymbrofe

import android.util.Log
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


object MachineActivity {
    private const val serverUrl = "https://34.73.169.40/"
    private val client = OkHttpClient()
    private var machine_name = ""
    private var machine_instructions = ""
    private var machine_url = ""
    private lateinit var machine: Machine


    fun getMachine(label: String?) : Machine {
        machine = Machine("No machine found", "Could not recognize machine. Please retry with another picture.", null)
        val requestBody = label?.toRequestBody()
        val request = Request.Builder()
            .method("POST", requestBody)
            .url(serverUrl + "machine/" + label + "/")
            .build()
        println(request)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("getMachine", "Failed POST request")
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println("RESPONSE SUCCESSFUL")
                    val machineReceived = try { JSONObject(response.body?.string() ?: "")
                        .getJSONArray("machine-info") } catch (e: JSONException) { JSONArray() }
                    val machineData = machineReceived as JSONArray
                    machine_name = machineData[0].toString()
                    machine_instructions = machineData[1].toString()
                    machine_url = machineData[2].toString()
                    machine = Machine(machine_name, machine_instructions, machine_url)
                }
            }
        })
        return machine
    }

}
