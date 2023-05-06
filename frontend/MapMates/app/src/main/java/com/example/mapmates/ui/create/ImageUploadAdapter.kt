package com.example.mapmates.ui.create

import android.graphics.Bitmap
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R
import com.example.mapmates.ui.home.OnGroupItemClickListener

class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val userImage : ImageView = itemView.findViewById(R.id.imageUser)
}

class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val button: ImageButton = itemView.findViewById(R.id.imageUploadButton)
}

class ImageUploadAdapter(private val images: List<Bitmap>, private val listener : OnGroupItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder> (){
    private val VIEW_TYPE_IMAGE = 0
    private val VIEW_TYPE_BUTTON = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_IMAGE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.image_nouser, parent, false)
            return ImageViewHolder(view)
        } else {
            // Create a new view for the button item
            val view = LayoutInflater.from(parent.context).inflate(R.layout.image_upload, parent, false)
            return ButtonViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_IMAGE) {
            val imageHolder = holder as ImageViewHolder
            imageHolder.userImage.setImageBitmap(images[position])
        } else {
            // Bind the data to the button view
            val buttonHolder = holder as ButtonViewHolder
            buttonHolder.button.setOnClickListener {
                //Upload Images from Gallery
                listener.onGroupItemClick(-1)
            }
        }
    }

    override fun getItemCount(): Int {
        // Add 1 to the item count for the button item
        return images.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        // Return the view type based on the position of the item
        return if (position < images.size) VIEW_TYPE_IMAGE else VIEW_TYPE_BUTTON
    }
}