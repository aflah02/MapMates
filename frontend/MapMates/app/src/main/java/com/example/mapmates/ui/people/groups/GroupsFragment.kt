package com.example.mapmates.ui.people.groups

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.AddFriendActivity
import com.example.mapmates.R
import com.example.mapmates.SettingsActivity
import com.example.mapmates.ui.people.friends.FriendData
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.CountDownLatch

class GroupsFragment : Fragment() {

    private lateinit var groupRecyclerView: RecyclerView
    private lateinit var adapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)

        groupRecyclerView = view.findViewById(R.id.groupCardRecyclerView)
        setGroupRecycler()

        val fab: View = view.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val bottomSheetFragment = GroupOptionsBottomSheetFragment()
            bottomSheetFragment.show(parentFragmentManager, "groupOptionsBottomSheet")
//            val intent = Intent(activity, SettingsActivity::class.java)
//            startActivity(intent)
//            Open a floating menu here with the options Create group or Join a group
        }

        return view
    }

    private fun setGroupRecycler(){
        groupRecyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = GroupsAdapter(emptyList())
        groupRecyclerView.adapter = adapter

        val groupList = getGroupList()

        adapter.updateList(groupList)
    }


    private fun getGroupList(): List<GroupData> {
        val groupList = mutableListOf<GroupData>()

        val jsonString = getGroupDetails("Aflah")
        if(jsonString!=null){
            val jsonObjectArray = parseJson(jsonString)
            if (jsonObjectArray != null) {
                for(item in jsonObjectArray){
                    Log.d("Groups",item)
                    groupList.add(GroupData(item,"https://picsum.photos/200"))
                }
            }
        }

        return groupList

    }


    private fun parseJson(jsonString: String): ArrayList<String>? {

        val jsonObject = JSONObject(jsonString)
        val groupList = ArrayList<String>()

        if (jsonObject.has("groups")) {
            val friendRequests = jsonObject.getJSONArray("groups")
            for (i in 0 until friendRequests.length()) {
                val name = friendRequests.getString(i)
                groupList.add(name)
            }
        }

        return groupList
    }

    fun getGroupDetails(username: String): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users/$username/groups")
            .build()
        val latch = CountDownLatch(1)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("groups").e(e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                Timber.tag("Groups").i(responseString.toString())
                latch.countDown()
            }
        }
        )
        latch.await()
        return responseString
    }
}