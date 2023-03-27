package com.example.mapmates.ui.people.friends

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var adapter: FriendsAdapter
    private lateinit var searchViewFriends: SearchView
    private lateinit var friendsList: List<FriendData>


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
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        friendsRecyclerView = view.findViewById(R.id.friendCardRecyclerView)
        setFriendsRecycler()

        searchViewFriends = view.findViewById(R.id.searchViewFriends)
        searchFriends()

        return view
    }
    private fun setFriendsRecycler(){
        friendsRecyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = FriendsAdapter(emptyList<FriendData>())
        friendsRecyclerView.adapter = adapter
        friendsList = getFriendsList()
        adapter.updateList(friendsList)
    }

    private fun searchFriends(){
        searchViewFriends.clearFocus()
        searchViewFriends.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(text: String) {
        val searchList= mutableListOf<FriendData>()
        for(data in friendsList)
            if(data.name.lowercase().contains(text.lowercase())){
                searchList.add(data)
            }
//        if(searchList.isEmpty()){
//            Toast.makeText(requireContext(),"no data found",Toast.LENGTH_SHORT).show()
//        }
        if(searchList.isNotEmpty()){
            adapter.setFilteredList(searchList)
        }
    }

    private fun getFriendsList(): List<FriendData> {
        val friendsList = mutableListOf<FriendData>()
        friendsList.add(FriendData("Kush","https://picsum.photos/200","I am a disco dancer"))
        friendsList.add(FriendData("Aadit","https://picsum.photos/200","I am a disco dancer too"))
        friendsList.add(FriendData("Mohit","https://picsum.photos/200","I am a disco dancer three"))
        friendsList.add(FriendData("Kush","https://picsum.photos/200","I am a disco dancer"))
        friendsList.add(FriendData("Aadit","https://picsum.photos/200","I am a disco dancer too"))
        friendsList.add(FriendData("Mohit","https://picsum.photos/200","I am a disco dancer three"))
        friendsList.add(FriendData("Kush","https://picsum.photos/200","I am a disco dancer"))
        friendsList.add(FriendData("Aadit","https://picsum.photos/200","I am a disco dancer too"))
        friendsList.add(FriendData("Ritwik","https://picsum.photos/200","I am a disco dancer three"))
        friendsList.add(FriendData("Kush","https://picsum.photos/200","I am a disco dancer"))
        friendsList.add(FriendData("Aadit","https://picsum.photos/200","I am a disco dancer too"))
        friendsList.add(FriendData("Ritwik","https://picsum.photos/200","I am a disco dancer three"))

        return friendsList

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
            FriendsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}