package com.example.mapmates.ui.people.groups

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R
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


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

interface OnItemClickListener {
    fun onSettingsClick(position: Int)
}

class GroupsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var groupRecyclerView: RecyclerView
    private lateinit var adapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)

        groupRecyclerView = view.findViewById(R.id.groupCardRecyclerView)
        setGroupRecycler()
//        groupRecyclerView.layoutManager = LinearLayoutManager(activity)
//
//        adapter = GroupsAdapter(emptyList())
//        groupRecyclerView.adapter = adapter
//
//        val groupList = getGroupList()
//
//        adapter.updateList(groupList)
        val fab: View = view.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
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
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}