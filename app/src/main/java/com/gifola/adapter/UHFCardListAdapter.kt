package com.gifola.adapter

import android.app.Activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gifola.R
import com.gifola.model.UHFCardModelItem
import kotlinx.android.synthetic.main.layout_uhf_card_items.view.*


abstract class UHFCardListAdapter(var context: Activity, var locationList: MutableList<UHFCardModelItem>) : RecyclerView.Adapter<UHFCardListAdapter.UHFCardListViewHolder>() {


    class UHFCardListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UHFCardListViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.layout_uhf_card_items, parent, false)
        return UHFCardListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    override fun onBindViewHolder(holder: UHFCardListViewHolder, position: Int) {

        val cardTitle: String = locationList[position].app_usr_Vh_name
        val cardNumber: String = locationList[position].app_usr_uhf_tag
        val location: String = locationList[position].location


        holder.itemView.text_name.text = cardTitle
        holder.itemView.text_code.text = cardNumber
        holder.itemView.text_place.text = location


        holder.itemView.img_delete.setOnClickListener {
            deleteRFCard(locationList[position].app_usr_UHF_id, locationList[position].app_UHF_st_id)
        }

        holder.itemView.img_copy.setOnClickListener {
            copyUHFCard(locationList[position])
        }
    }

    abstract fun deleteRFCard(appUsrRfId: Int, appUhfStId: Int)

    abstract fun copyUHFCard(uhfCardModelItem: UHFCardModelItem)
}