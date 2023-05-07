package com.example.mapmates.ui.people.friends

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.*
import okhttp3.*
import org.json.JSONArray
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.CountDownLatch

class FriendsFragment : Fragment() {

    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var adapter: FriendsAdapter
    private lateinit var searchViewFriends: SearchView
    private lateinit var friendsList: List<FriendData>
    private lateinit var pendingRequestButton: ImageButton
    private var username = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        pendingRequestButton = view.findViewById(R.id.pendingRequestButton)
        friendsRecyclerView = view.findViewById(R.id.friendCardRecyclerView)

        val sharedPrefs = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE)
        username = sharedPrefs.getString("Username",null).toString()
        if(username.isBlank()){
            //Run EntryActivity
            val intent = Intent(requireActivity(), EntryActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
        setFriendsRecycler()

        searchViewFriends = view.findViewById(R.id.searchViewFriends)
        searchFriends()
        val fab: View = view.findViewById(R.id.fab)
        pendingRequestButton.setOnClickListener {
            val intent = Intent(activity, PendingRequestActivity::class.java)
            startActivity(intent)
        }

        fab.setOnClickListener {
            val intent = Intent(activity, AddFriendActivity::class.java)
            startActivity(intent)

        }

        return view
    }
    private fun setFriendsRecycler(){
        friendsRecyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = FriendsAdapter(emptyList<FriendData>())
        friendsRecyclerView.adapter = adapter
        friendsList = getFriendsList()
        adapter.updateList(friendsList)
    }

    private fun searchFriends(){
        searchViewFriends.clearFocus()
        searchViewFriends.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(text: String) {
        val searchList= mutableListOf<FriendData>()
        for(data in friendsList)
            if(data.name.lowercase().contains(text.lowercase())){
                searchList.add(data)
            }
        if(searchList.isNotEmpty()){
            adapter.setFilteredList(searchList)
        }
    }

    private fun getFriendsList(): List<FriendData> {
        val friendsList = mutableListOf<FriendData>()

        val jsonString = getFriendsDetails(username)
        if(jsonString!=null){
            val jsonObjectArray = parseJson(jsonString)
            if (jsonObjectArray != null) {
                for(item in jsonObjectArray){
                    friendsList.add(FriendData(item.first,"https://mapsapp-1-m9050519.deta.app/users/${item.first}/profile_picture",item.second))
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
            val name = jsObj.getString("email")
            friendsList.add(Pair(username,name))
        }
        return friendsList
    }
    private fun getFriendsDetails(username: String): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users/$username/friends")
            .build()
        val latch = CountDownLatch(1)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("friendsf").e(e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                Timber.tag("Friends").i(responseString.toString())
                latch.countDown()
            }
        }
        )
        latch.await()
        return responseString
    }
}