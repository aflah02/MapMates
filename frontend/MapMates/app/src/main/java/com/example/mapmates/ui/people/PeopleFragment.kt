package com.example.mapmates.ui.people

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.mapmates.databinding.FragmentPeopleBinding
import com.example.mapmates.ui.people.groups.GroupsFragment

import com.example.mapmates.R
import com.example.mapmates.ui.people.friends.FriendsFragment
import com.google.android.material.tabs.TabLayout


class PeopleFragment : Fragment() {

    private lateinit var peopleViewModel: PeopleViewModel
    private var _binding: FragmentPeopleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        peopleViewModel =
            ViewModelProvider(this).get(PeopleViewModel::class.java)

        _binding = FragmentPeopleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val viewPager: ViewPager = root.findViewById(R.id.peopleChildFragment)
        val adapter = PeoplePageAdapter(childFragmentManager)
        adapter.addFragment(GroupsFragment(), "Groups")
        adapter.addFragment(FriendsFragment(), "Friends")
        viewPager.adapter = adapter

        val tabLayout: TabLayout = root.findViewById(R.id.peopleChildTabs)
        tabLayout.setupWithViewPager(viewPager)

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//         Create the child fragment and add it to the child_container
//        val groupsFragment = GroupsFragment()
//        val transaction = childFragmentManager.beginTransaction()
//        transaction.replace(R.id.GroupFragmentLayout, groupsFragment)
//        transaction.commit()
//        val friendsFragment = FriendsFragment()
//        val transaction2 = childFragmentManager.beginTransaction()
//        transaction2.replace(R.id.FriendsFragmentLayout, friendsFragment)
//        transaction2.commit()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}