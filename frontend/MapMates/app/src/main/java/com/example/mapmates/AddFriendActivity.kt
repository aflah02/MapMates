package com.example.mapmates

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.ui.people.friends.FriendData
import com.example.mapmates.ui.people.friends.FriendsAdapter
import com.example.mapmates.ui.people.friends.GlobalFriendsAdapter
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.CountDownLatch

class AddFriendActivity : AppCompatActivity() {
    private lateinit var globalNames: RecyclerView
    private lateinit var searchViewFriends: SearchView
    private lateinit var searchResultAdapter: GlobalFriendsAdapter
    private lateinit var searchResults: List<FriendData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)
        globalNames = findViewById(R.id.global_recycler_view)
        setFriendsRecycler()

        searchViewFriends = findViewById(R.id.global_search_view)
        searchFriends()
    }


    private fun searchFriends(){
        searchViewFriends.clearFocus()
        searchViewFriends.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String): Boolean {

                filterList(newText)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }
    private fun setFriendsRecycler(){
        globalNames.layoutManager = LinearLayoutManager(this)
        searchResultAdapter = GlobalFriendsAdapter(emptyList<FriendData>())
        globalNames.adapter = searchResultAdapter
        searchResults = getGlobalPeople()
        searchResultAdapter.updateList(searchResults)
    }

    private fun filterList(text: String) {
        val searchList = getFilteredPeople(text)
        searchResultAdapter.updateList(searchList)

    }

    private fun getGlobalPeople(): List<FriendData> {
        val friendsList = mutableListOf<FriendData>()
        val jsonString = globalJsonCall()
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
        val jsArray = JSONArray(jsonString)
        val friendsList = ArrayList<Pair<String, String>>()
        for(i in 0 until jsArray.length()){
            val jsObj = jsArray.getJSONObject(i)
            val username = jsObj.getString("username")
            val name = jsObj.getString("username")
            friendsList.add(Pair(username,name))
        }
        return friendsList
    }


    private fun globalJsonCall(): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users")
            .build()
        val latch = CountDownLatch(1)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("globalf").e(e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                Timber.tag("GlobalFriends").i(responseString.toString())
                latch.countDown()
            }
        }
        )
        latch.await()
        return responseString
    }
    private fun getFilteredPeople(text: String): List<FriendData> {
        val friendsList = mutableListOf<FriendData>()
        val jsonString = filteredJsonCall(text)
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



    private fun parseFilteredJson(jsonString: String): ArrayList<Pair<String, String>>? {
        val jsArray = JSONArray(jsonString)
        val friendsList = ArrayList<Pair<String, String>>()
        for(i in 0 until jsArray.length()){
            val jsObj = jsArray.getJSONObject(i)
            val username = jsObj.getString("username")
            val name = jsObj.getString("email")
            friendsList.add(Pair(username,name))
        }
        return friendsList
    }

    private fun filteredJsonCall(username: String): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users/$username/user_search")
            .build()
        val latch = CountDownLatch(1)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("filteredf").e(e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                Timber.tag("GlobalFilteredFriends").i(responseString.toString())
                latch.countDown()
            }
        }
        )
        latch.await()
        return responseString
    }
}