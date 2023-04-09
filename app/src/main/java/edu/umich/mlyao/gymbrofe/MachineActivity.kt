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
    private var machine_label = ""
    private var machine_name = ""
    private var machine_instructions = ""
    private var machine_url = ""
    private var muscles_targeted = ""
    private var muscles_targeted_img = ""
    private lateinit var machine: Machine


    fun getMachine(label: String?): Machine {
        println("IN GET MACHINE")
        machine = Machine(
            "No machine found",
            "Could not recognize machine. Please retry with another picture.",
            null
        )
        val requestBody = label?.toRequestBody()
        val request = Request.Builder()
            .method("POST", requestBody)
            .url(serverUrl + "machine/" + label + "/")
            .build()
        Log.d("postRequest", "request posted!")
        println(request)

        client.newCall(request).execute().use {response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val machineReceived = try {
                JSONObject(response.body?.string() ?: "")
                    .getJSONArray("machine-info")
            } catch (e: JSONException) {
                JSONArray()
            }
            val machineData = machineReceived as JSONArray
            machine_label = machineData[0].toString()
            val words: List<String> = machine_label.split("-")
            println("WORDSSSS")
            println(words)
            machine_name = words[0].capitalize() + " " + words[1].capitalize()
            machine_instructions = machineData[1].toString()
            machine_url = machineData[2].toString()
            muscles_targeted = machineData[3].toString()
            muscles_targeted_img = machineData[4].toString()
            println("MUSCLES TARGETED IMAGE")
            println(muscles_targeted_img)
            machine = Machine(
                machine_name,
                machine_instructions,
                machine_url,
                muscles_targeted,
                muscles_targeted_img
            )
            println(machine)
        }


//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                e.printStackTrace()
//                Log.e("getMachine", "Failed POST request")
//            }
//
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    println("RESPONSE SUCCESSFUL")
//                    val machineReceived = try {
//                        JSONObject(response.body?.string() ?: "")
//                            .getJSONArray("machine-info")
//                    } catch (e: JSONException) {
//                        JSONArray()
//                    }
//                    val machineData = machineReceived as JSONArray
//                    machine_name = machineData[0].toString()
//                    machine_instructions = machineData[1].toString()
//                    machine_url = machineData[2].toString()
//                    muscles_targeted = machineData[3].toString()
//                    muscles_targeted_img = machineData[4].toString()
//                    machine = Machine(
//                        machine_name,
//                        machine_instructions,
//                        machine_url,
//                        muscles_targeted,
//                        muscles_targeted_img
//                    )
//                    println(machine)
//                }
//            }
//        })
        return machine
    }
}


