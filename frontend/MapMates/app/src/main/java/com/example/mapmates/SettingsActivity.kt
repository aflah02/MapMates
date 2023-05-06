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
import com.example.mapmates.ui.people.friends.FriendsAdapter
import com.example.mapmates.ui.people.groups.AddContactData
import com.example.mapmates.ui.people.groups.GroupMemberAdapter

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
        //TODO: post call to delete group from users DB and call on backpressed
        }
    }
    private fun setPageDetails(){
        groupMembers.layoutManager = LinearLayoutManager(this)
        adapter = GroupMemberAdapter(emptyList<FriendData>())
        groupMembers.adapter = adapter
        memberList = getDetails()
//        TODO: see these text views are set from api
        groupTitle.text = "Kya baat hai"
        groupCode.text = "Hmmmmmmmm"
        adapter.updateList(memberList)
    }
    private fun getDetails(): List<FriendData> {
        val friendsList = mutableListOf<FriendData>()
//        TODO: Parse Json and return list here
        friendsList.add(FriendData("John Doe", "https://picsum.photos/200","32094190412"))
        friendsList.add(FriendData("Jane Smith", "https://picsum.photos/200","12889421894"))
        friendsList.add(FriendData("Bob Johnson", "https://picsum.photos/200","84391249"))
        friendsList.add(FriendData("John das", "https://picsum.photos/200","32094190412"))
        friendsList.add(FriendData("Jane adsa", "https://picsum.photos/200","12889421894"))
        friendsList.add(FriendData("Bob Johfdsanson", "https://picsum.photos/200","84391249"))
        friendsList.add(FriendData("John hdf", "https://picsum.photos/200","32094190412"))
        friendsList.add(FriendData("Jane fdha", "https://picsum.photos/200","12889421894"))
        friendsList.add(FriendData("Bob jrts", "https://picsum.photos/200","84391249"))
        return friendsList

    }
}