package com.example.platys.tagcontext

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.platys.R
import kotlinx.android.synthetic.main.card_carousel_item.view.*

class ContextTagCarouselAdapter(private val modelList: List<ContextCardCarouselModel>)
    : RecyclerView.Adapter<ContextTagCarouselAdapter.ContextTagCarouselViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ContextTagCarouselViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_carousel_item, parent, false)
        return ContextTagCarouselViewHolder(view)
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    override fun onBindViewHolder(holder: ContextTagCarouselViewHolder, position: Int) {
        holder.bind(modelList[position])
    }

    class ContextTagCarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(model: ContextCardCarouselModel) {
            itemView.ivBackground.setImageResource(model.backgroundColor)
            itemView.tvCaption.text = model.caption
            itemView.tvMessage.text = model.message
            itemView.ivCardIcon.setImageResource(model.imageId)
        }
    }
}

