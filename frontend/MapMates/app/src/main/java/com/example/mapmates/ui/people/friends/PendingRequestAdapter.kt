package com.example.mapmates.ui.people.friends

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import okhttp3.*
import timber.log.Timber
import java.io.IOException

class PendingRequestsAdapter(private var requests: MutableList<FriendData>) :
    RecyclerView.Adapter<PendingRequestsAdapter.PendingRequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingRequestViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pending_request, parent, false)
        return PendingRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingRequestViewHolder, position: Int) {
        val currentItem = requests[position]
//        val currentItem = searchResults[position]
        holder.contact_name.text = currentItem.name
        Picasso.get().load(currentItem.imageUrl).into(holder.profile_picture)
        holder.contact_number.text = currentItem.number
        holder.acceptButton.setOnClickListener {
            // Perform the accept operation
//            requests.remove(currentItem)
//            notifyItemRemoved(position)
//            notifyItemRangeChanged(position, itemCount)
//            hideView(holder.itemView)

            val userName = "your_user_name" // replace with the user name of the logged-in user
            val friendName = currentItem.name // get the name of the friend whose request is being accepted
            val url = "https://your_api_url.com/users/$userName/$friendName/acceptfriendrequest"
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
                        // remove the accepted request from the list
                        requests.remove(currentItem)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, itemCount)
                        hideView(holder.itemView)
                    } else {
                        // handle unsuccessful response
                    }
                }
            })

        }
        holder.rejectButton.setOnClickListener {
            // Perform the reject operation
            requests.remove(currentItem)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
            hideView(holder.itemView)
        }
    }

    override fun getItemCount(): Int = requests.size

    fun updateList(requests: List<FriendData>) {
        this.requests.clear()
        this.requests.addAll(requests)
        notifyDataSetChanged()
    }

    private fun hideView(view: View) {
        view.visibility = View.GONE
        Handler().postDelayed({ view.visibility = View.VISIBLE }, 3000)
    }

    inner class PendingRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profile_picture: ImageView = itemView.findViewById(R.id.profile_picture)
        val contact_name: TextView = itemView.findViewById(R.id.contact_name)
        val contact_number: TextView = itemView.findViewById(R.id.contact_number)
        val acceptButton: Button = itemView.findViewById(R.id.accept_button)
        val rejectButton: Button = itemView.findViewById(R.id.reject_button)
    }
}