package com.example.mapmates.ui.people.groups

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.AddFriendActivity
import com.example.mapmates.EntryActivity
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
    private lateinit var groupList: MutableList<GroupData>
    private var user = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)
        //Add a broadcast receiver listener
        groupList = mutableListOf<GroupData>()
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

        getGroupList()
    }


    private fun getGroupList(){
        val groupList = mutableListOf<GroupData>()
        val sharedPrefs = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE)
        user = sharedPrefs.getString("Username",null).toString()
        if(user.isBlank()){
            //Run EntryActivity
            val intent = Intent(requireActivity(), EntryActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        getGroupDetails(user)
    }


    private fun parseJson(jsonString: String): ArrayList<GroupData>? {

        val jsonObject = JSONObject(jsonString)
        val groupDataList = ArrayList<GroupData>()

        if (jsonObject.has("groups")) {
            val groupIDs = jsonObject.getJSONArray("groups")
            val groupNames = jsonObject.getJSONArray("group_names")
            val group_invite_codes = jsonObject.getJSONArray("group_invite_codes")
            for (i in 0 until groupIDs.length()) {
                val groupID = groupIDs[i].toString()
                val groupName = groupNames[i].toString()
                val groupInviteCode = group_invite_codes[i].toString()
                groupDataList.add(GroupData(groupName, "https://mapsapp-1-m9050519.deta.app/groups/$groupID/cover_image", groupID, groupInviteCode))
            }
        }

        return groupDataList
    }

    fun getGroupDetails(username: String){
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users/$username/groups")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("groups").e(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    return
                }
                activity?.runOnUiThread {
                    val jsonString = responseString
                    if(jsonString!=null){
                        val jsonObjectArray = parseJson(jsonString)
                        if (jsonObjectArray != null) {
                            for(item in jsonObjectArray){
//                    Log.d("Groups", item.toString())
                                groupList.add(item)
                            }
                        }
                    }
                    adapter.updateList(groupList)
                }
                Timber.tag("Groups").i(responseString.toString())
            }
        }
        )
    }
}