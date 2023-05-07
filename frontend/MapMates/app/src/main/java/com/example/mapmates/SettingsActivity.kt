package com.example.mapmates

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.ui.people.friends.FriendData
import com.example.mapmates.ui.people.groups.GroupMemberAdapter
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch


class SettingsActivity : AppCompatActivity() {
    private lateinit var groupTitle:EditText
    private lateinit var groupCode:TextView
    private lateinit var groupMembers:RecyclerView
    private lateinit var leaveButton:Button
    private lateinit var copyButton:ImageButton
    private lateinit var editTitle:ImageButton
    private lateinit var saveTitle:ImageButton
    private lateinit var changePicture:ImageButton
    private lateinit var groupPicture:ImageView
    private lateinit var adapter:GroupMemberAdapter
    private lateinit var memberList:List<FriendData>
    private var user = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        var groupID = intent.getStringExtra("groupID")
        Log.d("groupID", groupID.toString())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //TODO: get group title, group members in the form of friends data like friend fragment using API call using setPageDetails()
        groupTitle = findViewById(R.id.GroupTitle)
        groupCode = findViewById(R.id.groupCode)
        copyButton = findViewById(R.id.copyButton)
        groupMembers = findViewById(R.id.GrpMembersRecyclerView)
        groupPicture = findViewById(R.id.groupPicture)
        editTitle = findViewById(R.id.editTitleButton)
        saveTitle = findViewById(R.id.saveTitleButton)
        changePicture = findViewById(R.id.changePicture)

        setPageDetails(groupID)
        leaveButton = findViewById(R.id.leaveButton)
        copyButton.setOnClickListener{
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", groupCode.text.toString())
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
        }

        //Load SharedPrefs user
        val sharedPrefs = getSharedPreferences("Login", Context.MODE_PRIVATE)
        user = sharedPrefs.getString("Username",null).toString()
        if(user.isBlank()){
            //Run EntryActivity
            val intent = Intent(this, EntryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        leaveButton.setOnClickListener {

            val leaveGroupResponse = groupID?.let { it1 -> leaveGroupCall(user, it1) }
            onBackPressed()
        }
        editTitle.setOnClickListener {
            groupTitle.isEnabled = true
            editTitle.visibility = View.GONE
            groupTitle.inputType = InputType.TYPE_CLASS_TEXT
            saveTitle.visibility = View.VISIBLE
        }
        saveTitle.setOnClickListener {
//            TODO: post the value of edit text string
            val groupTitleText = groupTitle.text.toString()
            Log.d("About to set", groupTitleText)
            groupTitle.setText(groupTitleText)
            groupNameUpdateCall(groupID.toString(), groupTitleText)
            Log.d("After set", groupTitleText)
            groupTitle.setText(groupTitleText)
//            setBioCall(UserName, newBio)
            groupTitle.inputType = InputType.TYPE_NULL
            groupTitle.isEnabled = false
            saveTitle.visibility = View.GONE
            editTitle.visibility = View.VISIBLE

        }

        changePicture.setOnClickListener {
//            TODO: Change group picture using API
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }
    }

    private fun groupNameUpdateCall(groupID: String, groupName: String): String {
        val url = "https://mapsapp-1-m9050519.deta.app/groups/$groupID/update_group_name"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()
        requestJSON.put("group_name", groupName)
        val requestBody = requestJSON.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .addHeader("accept","application/json")
            .addHeader("Content-Type","application/json")
            .url(url)
            .post(requestBody)
            .build()
        val client = OkHttpClient()

        val latch = CountDownLatch(1)

        var APIresponse = ""

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("ErrorError",  e.toString())
                Log.e("CreateFragment", "Failed to update bio")
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("CreateFragment", "Successfully updated bio")
                APIresponse = response.body!!.string()
                latch.countDown()
            }
        })

        latch.await()
        Log.i("Response", APIresponse)
        return APIresponse

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Get User from intent
        // print all data in intent
//        Log.d("ProfileFragment", "onActivityResultDATA: $data")
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            Log.d("ProfileFragment", "onActivityResult: $imageUri")
            val contentResolver = applicationContext.contentResolver
            val encodedImage = imageUri?.let {
                getImageAsURLEncodedBinaryString(contentResolver,
                    it
                )
            }
//            Log.d("ProfileFragment", "onActivityResult: $encodedImage")
            val imageID = encodedImage?.let { uploadImage(it) }
            val group_id = intent.getStringExtra("groupID")
            val imageURL = "https://mapsapp-1-m9050519.deta.app/groups/$group_id/cover_image"
            Picasso.get().load(imageURL)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .fit().into(groupPicture)
//            Picasso.get().load("https://mapsapp-1-m9050519.deta.app/users/$userName/profile_picture").into(profilePicture)
            Log.d("SA", "onActivityResult: $imageID")
        }
        else{
            Log.d("SA", "onActivityResult: $resultCode")
        }
    }

    private fun setPageDetails(groupID: String?){
        groupMembers.layoutManager = LinearLayoutManager(this)
        adapter = GroupMemberAdapter(emptyList<FriendData>())
        groupMembers.adapter = adapter
        val getGroupDataResponse = groupID?.let { getGroupData(it) }
        val groupData = parseJson(getGroupDataResponse)
        val inviteCode = groupData.invite_code
        val group_id = groupData._id
        val group_name = groupData.title
        val memberList = groupData.members
        val groupMemberDetails = getGroupMemberDetails(memberList)
        val imageURL = "https://mapsapp-1-m9050519.deta.app/groups/$group_id/cover_image"
        Picasso.get().load(imageURL)
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .fit().into(groupPicture)
        groupTitle.setText(group_name)
        groupCode.text = inviteCode
        adapter.updateList(groupMemberDetails)
    }

    private fun uploadImage(encodedImage: String): String? {
        var imageID : String? = null
        val groupNumber = intent.getStringExtra("groupID")
        val url = "https://mapsapp-1-m9050519.deta.app/groups/$groupNumber/update_group_cover_image"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()
        requestJSON.put("image", encodedImage)
        Log.d("uploadImage", "JSON Constructed")
        val requestBody = requestJSON.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .addHeader("accept","application/json")
            .addHeader("Content-Type","application/json")
            .url(url)
            .post(requestBody)
            .build()
        Log.d("uploadImage", "Request Built")
        val client = OkHttpClient()

        val latch = CountDownLatch(1)

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Login API",e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("uploadImage", "Request Executed: ${responseBody}")
                val responseData = responseBody
                val jsonResponse = responseData?.let { JSONObject(it) }
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                if (jsonResponse != null) {
                    imageID = jsonResponse.get("image_id") as String
                }
                if (jsonResponse != null) {
                    Timber.tag("Login").i(jsonResponse.toString(4))
                }
                latch.countDown()
            }
        }
        )

        latch.await()
        Log.d("uploadImage", "Request Executed")
        return imageID
    }

    fun getImageAsURLEncodedBinaryString(contentResolver: ContentResolver, uri: Uri): String? {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
            // URL encode the base64 string
            val urlEncoded = URLEncoder.encode(base64, "UTF-8")
            return urlEncoded

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun parseJson(jsonString: String?): GroupAllData {
        if (jsonString == null) {
            return GroupAllData("","","", emptyList())
        }
        val jsObj = JSONObject(jsonString)
//        val jsObj = jsArray.getJSONObject(0)
        val group_id = jsObj.getString("_id")
        val group_name = jsObj.getString("group_name")
        val invite_code = jsObj.getString("invite_code")
        val members = jsObj.getString("users_as_string")
        val membersListArr = members.split("<DELIMITER069>")
        val membersListRemoveEmptyStrings = membersListArr.filter { it.isNotEmpty() }
        val membersList = membersListRemoveEmptyStrings.map { it.trim() }
//        TODO: replace this URL with response from JSON by editing API backend
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
        val jsObj = JSONObject(jsonString)
//        val jsObj = jsArray.getJSONObject(0)
        val name = jsObj.getString("username")
        val image_url = "https://mapsapp-1-m9050519.deta.app/users/$name/profile_picture"
        val bio = jsObj.getString("bio")
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

    private fun leaveGroupCall(userName: String, groupID: String): String {
        val url = "https://mapsapp-1-m9050519.deta.app/groups/$groupID/remove_user?user_name=$userName"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()
        val requestBody = requestJSON.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .addHeader("accept","application/json")
            .addHeader("Content-Type","application/json")
            .url(url)
            .put(requestBody)
            .build()
        val client = OkHttpClient()

        val latch = CountDownLatch(1)

        var APIresponse = ""

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("ErrorError",  e.toString())
                Log.e("CreateFragment", "Failed to leave group")
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("CreateFragment", "Successfully left group")
                APIresponse = response.body!!.string()
                latch.countDown()
            }
        })

        latch.await()
        Log.i("Response", APIresponse)
        return APIresponse

    }
}