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
import com.squareup.picasso.Picasso

class PendingRequestsAdapter(private var requests: MutableList<FriendData>) :
    RecyclerView.Adapter<PendingRequestsAdapter.PendingRequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingRequestViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pending_request, parent, false)
        return PendingRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingRequestViewHolder, position: Int) {
        val request = requests[position]
//        val currentItem = searchResults[position]
        holder.contact_name.text = request.name
        Picasso.get().load(request.imageUrl).into(holder.profile_picture)
        holder.contact_number.text = request.number
        holder.acceptButton.setOnClickListener {
            // Perform the accept operation
            requests.remove(request)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
            hideView(holder.itemView)
        }
        holder.rejectButton.setOnClickListener {
            // Perform the reject operation
            requests.remove(request)
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

//        fun bind(request: FriendRequest) {
//            nameTextView.text = request.name
//            numberTextView.text = request.number
//            Glide.with(itemView.context)
//                .load(request.imageUrl)
//                .centerCrop()
//                .placeholder(R.drawable.ic_user)
//                .into(profileImageView)
//        }
    }
}