package com.example.mapmates

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.ui.people.friends.FriendData
import com.example.mapmates.ui.people.friends.FriendsAdapter
import com.example.mapmates.ui.people.friends.GlobalFriendsAdapter
import com.example.mapmates.ui.people.friends.RequestFriendData
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
    private lateinit var searchResults: List<RequestFriendData>
    private var user = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)
        globalNames = findViewById(R.id.global_recycler_view)
        val sharedPrefs = getSharedPreferences("Login", Context.MODE_PRIVATE)
        user = sharedPrefs.getString("Username",null).toString()
        if(user.isBlank()){
            //Run EntryActivity
            val intent = Intent(this, EntryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
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
        searchResultAdapter = GlobalFriendsAdapter(this,emptyList<RequestFriendData>())
        globalNames.adapter = searchResultAdapter
        searchResults = getGlobalPeople()
        searchResultAdapter.updateList(searchResults)
    }

    private fun filterList(text: String) {
        val searchList = getFilteredPeople(text)
        searchResultAdapter.updateList(searchList)

    }

    private fun getGlobalPeople(): List<RequestFriendData> {
        val friendsList = mutableListOf<RequestFriendData>()
        val jsonString = globalJsonCall(user)
        if(jsonString!=null){
            val jsonObjectArray = parseJson(jsonString)
            if (jsonObjectArray != null) {
                for(item in jsonObjectArray){
                    friendsList.add(item)
                }
            }
        }
        return friendsList
    }

    private fun parseJson(jsonString: String): ArrayList<RequestFriendData>? {
        val jsArray = JSONArray(jsonString)
        val friendsList = ArrayList<RequestFriendData>()
        for(i in 0 until jsArray.length()){
            val jsObj = jsArray.getJSONObject(i)
            val username = jsObj.getString("username")
            val name = jsObj.getString("username")
            val status = jsObj.getString("sent_request")

            friendsList.add(RequestFriendData(username,"https://mapsapp-1-m9050519.deta.app/users/${username}/profile_picture",username+"@gmail.com",status))
        }
        return friendsList
    }


    private fun globalJsonCall(myname:String): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users/${myname}/search_users_not_friend")
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
    private fun getFilteredPeople(text: String): List<RequestFriendData> {
        val friendsList = mutableListOf<RequestFriendData>()
        val jsonString = filteredJsonCall(user,text)
        if(jsonString!=null){
            val jsonObjectArray = parseJson(jsonString)
            if (jsonObjectArray != null) {
                for(item in jsonObjectArray){
                    friendsList.add(item)
                }
            }
        }

        return friendsList

    }



    private fun parseFilteredJson(jsonString: String): ArrayList<RequestFriendData>? {

        val jsArray = JSONArray(jsonString)
        val friendsList = ArrayList<RequestFriendData>()
        for(i in 0 until jsArray.length()){
            val jsObj = jsArray.getJSONObject(i)
            val username = jsObj.getString("username")
            val name = jsObj.getString("email")
            val status = jsObj.getString("sent_request")
            friendsList.add(RequestFriendData(username,"https://mapsapp-1-m9050519.deta.app/users/${username}/profile_picture",name,status))
        }
        return friendsList
    }

    private fun filteredJsonCall(myname:String,username: String): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users/${myname}/$username/search_users_not_friend_with_match")
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