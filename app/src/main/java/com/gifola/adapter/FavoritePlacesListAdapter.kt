package com.gifola.adapter

import android.app.Activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gifola.R
import com.gifola.apis.ApiURLs
import com.gifola.model.CheckInListModelItem
import com.gifola.model.FavLocationModelItem
import kotlinx.android.synthetic.main.custom_fav.view.*


abstract class FavoritePlacesListAdapter(var context: Activity, var checkInList: MutableList<FavLocationModelItem>) : RecyclerView.Adapter<FavoritePlacesListAdapter.FavoritePlacesListViewHolder>() {


    class FavoritePlacesListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritePlacesListViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.custom_fav, parent, false)
        return FavoritePlacesListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return checkInList.size
    }

    override fun onBindViewHolder(holder: FavoritePlacesListViewHolder, position: Int) {

        if(checkInList[position].Name == null || checkInList[position].Name.isEmpty()){
            holder.itemView.text_top_name.text = "-"
        }else {
            holder.itemView.text_top_name.text = checkInList[position].Name + " - " + checkInList[position].Location
        }
        Glide.with(context)
                .load(ApiURLs.IMAGE_URL + checkInList[position].ProPic)
                .apply(RequestOptions().error(R.drawable.user1).error(R.drawable.user1))
                .into(holder.itemView.profile_image)

        holder.itemView.frequentVisitorLayout.setOnClickListener {
            frequentList(checkInList[position].MobileNo, checkInList[position].Name)
        }
    }

    abstract fun frequentList(mobileNo: String, memName: String)
}