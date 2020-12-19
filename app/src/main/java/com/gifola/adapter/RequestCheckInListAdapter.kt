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
import com.gifola.model.RFCardModelItem
import kotlinx.android.synthetic.main.layout_allow_checkin_item_view.view.*


abstract class RequestCheckInListAdapter(var context: Activity, var checkInList: MutableList<CheckInListModelItem>) : RecyclerView.Adapter<RequestCheckInListAdapter.RequestCheckInListViewHolder>() {


    class RequestCheckInListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestCheckInListViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.layout_allow_checkin_item_view, parent, false)
        return RequestCheckInListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return checkInList.size
    }

    override fun onBindViewHolder(holder: RequestCheckInListViewHolder, position: Int) {
        holder.itemView.text_name.text = checkInList[position].MemName
        holder.itemView.text_mobileno.text = checkInList[position].MobileNo
        holder.itemView.text_date.text = checkInList[position].Reqdate

        Glide.with(context)
                .load(ApiURLs.IMAGE_URL + checkInList[position].ProPic)
                .apply(RequestOptions().placeholder(R.drawable.user1).error(R.drawable.user1))
                .into(holder.itemView.profile_image)


        holder.itemView.clicked.setOnClickListener {
            onCheckedList(checkInList[position].MobileNo , checkInList[position].MemName)
        }
    }

    fun searchResult(searchText : String): ArrayList<CheckInListModelItem> {
        val searchedList : ArrayList<CheckInListModelItem> = ArrayList()
        checkInList.forEach {
            if(it.MobileNo.contains(searchText)){
                searchedList.add(it)
            }
        }
        return searchedList
    }

    abstract fun onCheckedList(contactNum : String, contactName : String)
}