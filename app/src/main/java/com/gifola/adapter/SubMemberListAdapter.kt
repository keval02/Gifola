package com.gifola.adapter

import android.app.Activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gifola.R
import com.gifola.model.SubMemberModelItem
import kotlinx.android.synthetic.main.layout_sub_member_list_items.view.*


abstract class SubMemberListAdapter(var context: Activity, var locationList: MutableList<SubMemberModelItem>) : RecyclerView.Adapter<SubMemberListAdapter.SubMemberListViewHolder>() {


    class SubMemberListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubMemberListViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.layout_sub_member_list_items, parent, false)
        return SubMemberListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    override fun onBindViewHolder(holder: SubMemberListViewHolder, position: Int) {

        val cardTitle: String = locationList[position].mem_name
        val cardNumber: String = locationList[position].mem_mob
        val location: String = locationList[position].member_address


        holder.itemView.text_name.text = cardTitle
        holder.itemView.text_mobileno.text = cardNumber
        holder.itemView.text_place.text = location


        holder.itemView.img_delete.setOnClickListener {
            deleteRFCard(locationList[position].mem_id, locationList[position].mem_site_id)
        }
    }

    abstract fun deleteRFCard(appUsrRfId: Int, memSiteId: Int)
}