package com.example.mapmates

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.ui.people.friends.FriendData
import com.example.mapmates.ui.people.friends.PendingRequestsAdapter
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.CountDownLatch

class PendingRequestActivity : AppCompatActivity() {

    private lateinit var pendingRequestView: RecyclerView
    private lateinit var adapter: PendingRequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_request)

        pendingRequestView = findViewById(R.id.pendingRequestRecyclerView)
        pendingRequestView.layoutManager = LinearLayoutManager(this)
        adapter = PendingRequestsAdapter(this,getPendingRequests() as MutableList<FriendData>)
        pendingRequestView.adapter = adapter
    }
//    pendingList.add(FriendData("John Doe", "https://picsum.photos/200","32094190412"))
//    pendingList.add(FriendData("Jane Smith", "https://picsum.photos/200","12889421894"))
//    pendingList.add(FriendData("Bob Johnson", "https://picsum.photos/200","84391249"))

    private fun getPendingRequests(): List<FriendData> {
        val friendsList = mutableListOf<FriendData>()
        val jsonString = friendRequestJsonCall("Aflah")
        if(jsonString!=null){
            val jsonObjectArray = parseJson(jsonString)
            if (jsonObjectArray != null) {
                for(item in jsonObjectArray){
                    friendsList.add(FriendData(item.first,"https://mapsapp-1-m9050519.deta.app/users/${item.first}/profile_picture",(item.second+"@email.com")))
                }
            }
        }
        return friendsList
    }
    private fun parseJson(jsonString: String): ArrayList<Pair<String, String>>? {

        val jsonObject = JSONObject(jsonString)
        val friendsList = ArrayList<Pair<String, String>>()

        if (jsonObject.has("friend_requests")) {
            val friendRequests = jsonObject.getJSONArray("friend_requests")
            for (i in 0 until friendRequests.length()) {
                val name = friendRequests.getString(i)
                friendsList.add(Pair(name,name))
            }
        }
        return friendsList
    }


    private fun friendRequestJsonCall(text:String): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users/${text}/getfriendrequests")
            .build()
        val latch = CountDownLatch(1)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("pendingf").e(e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                Timber.tag("PendingReqs").i(responseString.toString())
                latch.countDown()
            }
        }
        )
        latch.await()
        return responseString
    }

}

//https://mapsapp-1-m9050519.deta.app/users/Aflah/getfriendrequests