package com.example.mapmates

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.ui.people.friends.FriendData
import com.example.mapmates.ui.people.groups.AddContactData
import com.example.mapmates.ui.people.groups.AddGroupMemberAdapter
import com.google.android.material.snackbar.Snackbar

class CreateGroupActivity : AppCompatActivity() {
    private lateinit var groupTitle: EditText
    private lateinit var contacts: RecyclerView
    private lateinit var createButton: Button
    private lateinit var adapter: AddGroupMemberAdapter
    private lateinit var contactList: List<AddContactData>
    private lateinit var searchViewContacts: SearchView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        contacts = findViewById(R.id.addContactRecyclerView)
        groupTitle = findViewById(R.id.editTextGroupTitle)
        createButton = findViewById(R.id.createButton)

        setContactRecycler()
        searchViewContacts = findViewById(R.id.searchViewContactsView)
        searchFriends()
        createButton.setOnClickListener { view ->
            val selectedFriends = adapter.getSelectedContacts()
            if (selectedFriends.isNotEmpty()) {
                createGroup(selectedFriends)
            } else {
                Snackbar.make(view, "Please select at least one friend", Snackbar.LENGTH_LONG).show()
            }
        }
    }
    private fun searchFriends(){
        searchViewContacts.clearFocus()
        searchViewContacts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
//                adapter.filter(newText)
                filterList(newText)
                return true
            }
        })
    }
    private fun setContactRecycler() {
        contacts.layoutManager = LinearLayoutManager(this)
        adapter = AddGroupMemberAdapter(emptyList<AddContactData>())
        contacts.adapter = adapter
        contactList = getContactList()
        adapter.updateList(contactList)
    }
    private fun getContactList(): List<AddContactData> {
        val contactDataList = mutableListOf<AddContactData>()
        contactDataList.add(AddContactData("John Doe", "https://picsum.photos/200","32094190412"))
        contactDataList.add(AddContactData("Jane Smith", "https://picsum.photos/200","12889421894"))
        contactDataList.add(AddContactData("Bob Johnson", "https://picsum.photos/200","84391249"))
        contactDataList.add(AddContactData("John das", "https://picsum.photos/200","32094190412"))
        contactDataList.add(AddContactData("Jane adsa", "https://picsum.photos/200","12889421894"))
        contactDataList.add(AddContactData("Bob Johfdsanson", "https://picsum.photos/200","84391249"))
        contactDataList.add(AddContactData("John hdf", "https://picsum.photos/200","32094190412"))
        contactDataList.add(AddContactData("Jane fdha", "https://picsum.photos/200","12889421894"))
        contactDataList.add(AddContactData("Bob jrts", "https://picsum.photos/200","84391249"))
        return contactDataList
    }

    private fun filterList(text: String) {
        val searchList= mutableListOf<AddContactData>()
        for(data in contactList)
            if(data.name.lowercase().contains(text.lowercase())){
                searchList.add(data)
            }
        if(searchList.isNotEmpty()){
            adapter.setFilteredList(searchList)
        }
    }


    private fun createGroup(selectedFriends: List<AddContactData>){
//        Make a post call here to add a group with this title and contacts to the backend
        for (i in selectedFriends) {
            Log.d("selectedFriends", i.name)
        }
        Log.d("selectedFriends", groupTitle.text.toString())
//        do Back pressed here to back to the GroupFragment
    }
}