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
import kotlinx.android.synthetic.main.custom_fav.view.*


abstract class DashboardFrequentVisitorListAdapter(var context: Activity, var checkInList: MutableList<CheckInListModelItem>) : RecyclerView.Adapter<DashboardFrequentVisitorListAdapter.DashboardFrequentVisitorListViewHolder>() {


    class DashboardFrequentVisitorListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardFrequentVisitorListViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.custom_fav, parent, false)
        return DashboardFrequentVisitorListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return checkInList.size
    }

    override fun onBindViewHolder(holder: DashboardFrequentVisitorListViewHolder, position: Int) {
        if(checkInList[position].MemName == null || checkInList[position].MemName.isEmpty()){
            holder.itemView.text_top_name.text = "-"
        }else {
            holder.itemView.text_top_name.text = checkInList[position].MemName
        }
        Glide.with(context)
                .load(ApiURLs.IMAGE_URL + checkInList[position].ProPic)
                .apply(RequestOptions().error(R.drawable.user1).error(R.drawable.user1))
                .into(holder.itemView.profile_image)

        holder.itemView.frequentVisitorLayout.setOnClickListener {
            selectedFrequentUser(checkInList[position].MobileNo, checkInList[position].MemName)
        }
    }

    abstract fun selectedFrequentUser(mobileNo: String, memName: String)
}