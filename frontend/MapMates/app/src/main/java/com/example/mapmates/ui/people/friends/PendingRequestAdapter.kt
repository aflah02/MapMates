package com.example.mapmates.ui.people.friends

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.EntryActivity
import com.example.mapmates.R
import com.squareup.picasso.Picasso
import okhttp3.*
import timber.log.Timber
import java.io.IOException

class PendingRequestsAdapter(private val activity: Activity,private var requests: MutableList<FriendData>) :
    RecyclerView.Adapter<PendingRequestsAdapter.PendingRequestViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingRequestViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pending_request, parent, false)
        return PendingRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingRequestViewHolder, position: Int) {
        val currentItem = requests[holder.adapterPosition]
        holder.contact_name.text = currentItem.name
        Picasso.get().load(currentItem.imageUrl).into(holder.profile_picture)
        holder.contact_number.text = currentItem.bio
        val sharedPrefs = activity.getSharedPreferences("Login", Context.MODE_PRIVATE)
        val username = sharedPrefs.getString("Username",null).toString()
        if(username.isBlank()){
            //Run EntryActivity
            val intent = Intent(activity, EntryActivity::class.java)
            startActivity(activity,intent,null)
            activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
        val friendName = currentItem.name // get the name of the friend whose request is being accepted
        holder.acceptButton.setOnClickListener {
            jsonCall("acceptfriendrequest",username,friendName,holder)
        }
        holder.rejectButton.setOnClickListener {
            jsonCall("declinefriendrequest",username, friendName, holder)
        }
    }


    private fun jsonCall(requestString: String, userName:String, friendName:String, holder: PendingRequestViewHolder,){
        val currentItem = requests[holder.adapterPosition]

        val url = "https://mapsapp-1-m9050519.deta.app/users/$userName/$friendName/${requestString}"
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
                        // remove the accepted request from the list
                        requests.remove(currentItem)
                        notifyItemRemoved(holder.adapterPosition)
                        notifyItemRangeChanged(holder.adapterPosition, itemCount)
                        hideView(holder.itemView)
                    }

                } else {
                    // handle unsuccessful response
                }
            }
        })
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