package com.example.mapmates.ui.people.friends

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R
import com.squareup.picasso.Picasso
import okhttp3.*
import timber.log.Timber
import java.io.IOException


class GlobalFriendsAdapter(var activity: Activity,var searchResults: List<RequestFriendData>): RecyclerView.Adapter<GlobalFriendsAdapter.GlobalFriendsViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlobalFriendsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_friend_row, parent, false)
        return GlobalFriendsViewHolder(view)
    }

    override fun onBindViewHolder(holder: GlobalFriendsViewHolder, position: Int) {
        val currentItem = searchResults[position]
        holder.contact_name.text = currentItem.name
        Picasso.get().load(currentItem.imageUrl).into(holder.profile_picture)
        holder.contact_number.text = currentItem.number
        if(currentItem.status.equals("Yes")){
            holder.requestStatus.setText("sent")
        }
//        holder.requestStatus.setText(currentItem.status)//change according currentItem.status in if conditions only in case of pending
        holder.requestStatus.setOnClickListener {
            // Call API to post friend request and change request to pending
            val userName = "Aflah" // replace with the user name of the logged-in user
            val friendName = currentItem.name // get the name of the friend whose request is being accepted
            val url = "https://mapsapp-1-m9050519.deta.app/users/$userName/$friendName/sendfriendrequest"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .post(RequestBody.create(null, ""))
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // handle network errors
                    Timber.tag("pendingf").e(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        activity.runOnUiThread {
                            holder.requestStatus.setText("sent")
                        }
//                        holder.requestStatus.setText("sent")

                    } else {
                        // handle unsuccessful response
                    }
                }
            })

        }
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }


    fun setFilteredList(filteredList: List<RequestFriendData>) {
        searchResults = filteredList
        notifyDataSetChanged()
    }

    fun updateList(newList: List<RequestFriendData>) {
        searchResults = newList
        notifyDataSetChanged()
    }

    inner class GlobalFriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profile_picture: ImageView = itemView.findViewById(R.id.profile_picture)
        val contact_name: TextView = itemView.findViewById(R.id.contact_name)
        val contact_number: TextView = itemView.findViewById(R.id.contact_number)
        val requestStatus: Button = itemView.findViewById(R.id.requestStatusButton)
    }
}