package com.example.mapmates

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.ui.people.friends.FriendData
import com.example.mapmates.GroupAllData
import com.example.mapmates.ui.people.friends.FriendsAdapter
import com.example.mapmates.ui.people.groups.AddContactData
import com.example.mapmates.ui.people.groups.GroupMemberAdapter
import okhttp3.*
import org.json.JSONArray
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.CountDownLatch

class SettingsActivity : AppCompatActivity() {
    private lateinit var groupTitle:TextView
    private lateinit var groupCode:TextView
    private lateinit var groupMembers:RecyclerView
    private lateinit var leaveButton:Button
    private lateinit var copyButton:ImageButton
    private lateinit var adapter:GroupMemberAdapter
    private lateinit var memberList:List<FriendData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        //TODO: get group title, group members in the form of friends data like friend fragment using API call using setPageDetails()
        groupTitle = findViewById(R.id.GroupTitle)
        groupCode = findViewById(R.id.groupCode)
        copyButton = findViewById(R.id.copyButton)
        groupMembers = findViewById(R.id.GrpMembersRecyclerView)
        setPageDetails()
        leaveButton = findViewById(R.id.leaveButton)
        copyButton.setOnClickListener{
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", groupCode.text.toString())
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
        }

        leaveButton.setOnClickListener {
            val userName = "Mohit"
            val groupID = "1"
            val leaveGroupResponse = leaveGroupCall(userName, groupID)
        }
    }
    private fun setPageDetails(){
        groupMembers.layoutManager = LinearLayoutManager(this)
        adapter = GroupMemberAdapter(emptyList<FriendData>())
        groupMembers.adapter = adapter
        val groupID = "1"
        val getGroupDataResponse = getGroupData(groupID)
        val groupData = parseJson(getGroupDataResponse)
        val inviteCode = groupData.invite_code
        val group_id = groupData._id
        val group_name = groupData.title
        val memberList = groupData.members
        val groupMemberDetails = getGroupMemberDetails(memberList)
        groupTitle.text = group_name
        groupCode.text = inviteCode
        adapter.updateList(groupMemberDetails)
    }

    private fun parseJson(jsonString: String?): GroupAllData {
        if (jsonString == null) {
            return GroupAllData("","","", emptyList())
        }
        val jsArray = JSONArray(jsonString)
        val jsObj = jsArray.getJSONObject(0)
        val group_id = jsObj.getString("_id")
        val group_name = jsObj.getString("group_name")
        val invite_code = jsObj.getString("invite_code")
        val members = jsObj.getJSONArray("users")
        // Add members to ArrayList
        val membersList = ArrayList<String>()
        for(j in 0 until members.length()){
            membersList.add(members.getString(j))
        }
        val gData = GroupAllData(group_id, group_name, invite_code, membersList)
        return gData
    }
    private fun getGroupData(groupID: String): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/groups/$groupID")
            .build()
        val latch = CountDownLatch(1)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("Group Data Fetch").e(e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                Timber.tag("Group Data Fetch").i(responseString.toString())
                latch.countDown()
            }
        }
        )
        latch.await()
        return responseString
    }

    private fun getGroupMemberDetails(groupMemberNames: List<String>): List<FriendData> {
        val groupMemberList = mutableListOf<FriendData>()
        for (member in groupMemberNames) {
            val getMemberDataResponse = getGroupMemberData(member)
            val memberData = parseGroupMemberJson(getMemberDataResponse)
            groupMemberList.add(memberData)
        }
        return groupMemberList

    }

    private fun parseGroupMemberJson(jsonString: String?): FriendData {
        if (jsonString == null) {
            return FriendData("","","")
        }
        val jsArray = JSONArray(jsonString)
        val jsObj = jsArray.getJSONObject(0)
        val name = jsObj.getString("username")
        val image_url = jsObj.getString("https://mapsapp-1-m9050519.deta.app/users/$name/profile_picture")
        val bio = jsObj.getString("bio")
        val members = jsObj.getJSONArray("users")
        val friendData = FriendData(name, image_url, bio)
        return friendData
    }
    private fun getGroupMemberData(userName: String): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users/$userName")
            .build()
        val latch = CountDownLatch(1)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("Group Data Fetch").e(e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                Timber.tag("Group Data Fetch").i(responseString.toString())
                latch.countDown()
            }
        }
        )
        latch.await()
        return responseString
    }

    private fun leaveGroupCall(userName: String, groupID: String): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/groups/$groupID/remove_user?user_name=$userName")
            .build()
        val latch = CountDownLatch(1)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("Group Data Fetch").e(e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                Timber.tag("Group Data Fetch").i(responseString.toString())
                latch.countDown()
            }
        }
        )
        latch.await()
        return responseString
    }

}