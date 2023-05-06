package com.example.mapmates.ui.people.groups

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
//            TODO:Set the generated Code as the response here!!!
        }
        copyButton.setOnClickListener{
            val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", generatedCode.text.toString())
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_LONG).show()
        }
        joinGroup.setOnClickListener {
            Toast.makeText(requireContext(),"Entered ${codeGroup.text}",Toast.LENGTH_SHORT).show()
//            TODO:Post this code to add a group on response ok go back, on fail response display toast no group found
        }

        return view
    }
}