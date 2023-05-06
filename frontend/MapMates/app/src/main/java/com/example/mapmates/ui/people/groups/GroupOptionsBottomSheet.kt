package com.example.mapmates.ui.people.groups

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.mapmates.CreateGroupActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.mapmates.R


class GroupOptionsBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var createGroup: Button
    private lateinit var joinGroup: Button
    private lateinit var codeGroup: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_group_options, container, false)
        // set click listeners for the "Create group" and "Join group" buttons here
        createGroup = view.findViewById(R.id.btnCreateGroup)
        joinGroup = view.findViewById(R.id.btnJoinGroup)
        codeGroup = view.findViewById(R.id.groupCode)

        createGroup.setOnClickListener {
            Toast.makeText(requireContext(),"Clicked on create",Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, CreateGroupActivity::class.java)
            startActivity(intent)
        }
        joinGroup.setOnClickListener {
            Toast.makeText(requireContext(),"Entered ${codeGroup.text}",Toast.LENGTH_SHORT).show()
//            Post this call on response ok go back, on fail response display toast no group found
        }

        return view
    }
}