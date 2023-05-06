package com.example.mapmates.ui.people.groups

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.mapmates.CreateGroupActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.mapmates.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch


class GroupOptionsBottomSheetFragment() : BottomSheetDialogFragment() {
    private lateinit var createGroup: Button
    private lateinit var joinGroup: Button
    private lateinit var codeGroup: EditText
    private lateinit var groupTitle: EditText
    private lateinit var generatedCode: TextView
    private lateinit var copyButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_group_options, container, false)
        createGroup = view.findViewById(R.id.btnCreateGroup)
        joinGroup = view.findViewById(R.id.btnJoinGroup)
        codeGroup = view.findViewById(R.id.groupCode)
        groupTitle = view.findViewById(R.id.groupTitle)
        generatedCode = view.findViewById(R.id.generatedCode)
        copyButton = view.findViewById(R.id.copyButton)
        createGroup.isEnabled = !groupTitle.text.isNullOrEmpty()
        groupTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                createGroup.isEnabled = !s.isNullOrEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        joinGroup.isEnabled = !codeGroup.text.isNullOrEmpty()
        codeGroup.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                joinGroup.isEnabled = !s.isNullOrEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        createGroup.setOnClickListener {

            Toast.makeText(requireContext(),"Clicked on create",Toast.LENGTH_SHORT).show()
            generatedCode.visibility = View.VISIBLE
            copyButton.visibility = View.VISIBLE
            createGroup.visibility = View.GONE
            groupTitle.visibility = View.GONE
//            TODO:Post An API to make a group here!!!
            val cgResponse = createGroup(groupTitle.text.toString(), "Aflah")
//            TODO:Set the generated Code as the response here!!!
            val parseJson = JSONObject(cgResponse)
            val inviteCode = parseJson.getString("invite_code")
            generatedCode.text = inviteCode
        }
        copyButton.setOnClickListener{
            val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", generatedCode.text.toString())
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_LONG).show()
        }
        joinGroup.setOnClickListener {
            Toast.makeText(requireContext(),"Entered ${codeGroup.text}",Toast.LENGTH_SHORT).show()
//            TODO: Post this code to add a group on response ok go back, on fail response display toast no group found
            val apiJoinResponse = joinGroup(codeGroup.text.toString(), "Aflah")
            if (apiJoinResponse != ""){
                Toast.makeText(requireContext(),"Joined ${codeGroup.text}",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(),"No group found",Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun createGroup(GroupName: String, UserName: String): String{
        val url = "https://mapsapp-1-m9050519.deta.app/groups?group_name=$GroupName&user_name=$UserName"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()

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
                Log.e("CreateFragment", "Failed to create group")
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("CreateFragment", "Successfully created group")
                APIresponse = response.body!!.string()
                latch.countDown()
            }
        })

        latch.await()

        return APIresponse

    }

    private fun joinGroup(GroupInviteCode: String, UserName: String): String{
        val url = "https://mapsapp-1-m9050519.deta.app/groups/$GroupInviteCode/add_user_by_invite_code?user_name=$UserName"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()

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
                Log.e("CreateFragment", "Failed to join group")
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("CreateFragment", "Successfully joined group")
                APIresponse = response.body!!.string()
                latch.countDown()
            }
        })

        latch.await()

        return APIresponse

    }
}