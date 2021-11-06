package com.example.selfieground.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.selfieground.R
import com.example.selfieground.adapters.BackgroundListAdapter.BackgroundListHolder

public class BackgroundListAdapter( private val context : Context?, private val bg_list : List<Bitmap>, private val listener : BackgroundListOnClick ) : RecyclerView.Adapter<BackgroundListAdapter.BackgroundListHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundListHolder {
        val view  =  LayoutInflater.from(parent.context).inflate( R.layout.bg_layout_view, parent, false );
        return BackgroundListHolder( view, listener )
    }

    override fun onBindViewHolder(holder: BackgroundListHolder, position: Int) {
        val view = holder.itemView as ImageView
        view.setImageBitmap( bg_list[position] )
    }

    override fun getItemCount(): Int {
        return bg_list.size
    }

    class BackgroundListHolder(itemView: View, listener: BackgroundListOnClick) : RecyclerView.ViewHolder(itemView) {

        val imageView : ImageView;
        val listener : BackgroundListOnClick;

        init{
            this.imageView = itemView as ImageView
            this.listener = listener
            imageView.setOnClickListener{
                listener.onClick(adapterPosition)
            }
        }

    }
}

public interface BackgroundListOnClick{

    fun onClick( position  : Int);
}