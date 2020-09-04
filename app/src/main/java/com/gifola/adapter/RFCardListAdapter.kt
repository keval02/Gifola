package com.gifola.adapter

import android.app.Activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gifola.R
import com.gifola.model.RFCardModelItem
import kotlinx.android.synthetic.main.layout_rf_card_items.view.*


abstract class RFCardListAdapter(var context: Activity, var locationList: MutableList<RFCardModelItem>) : RecyclerView.Adapter<RFCardListAdapter.RFCardListViewHolder>() {


    class RFCardListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RFCardListViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.layout_rf_card_items, parent, false)
        return RFCardListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    override fun onBindViewHolder(holder: RFCardListViewHolder, position: Int) {

        val cardTitle: String = locationList[position].name
        val cardNumber: String = locationList[position].cardNo
        val location: String = locationList[position].location


        holder.itemView.text_name.text = cardTitle
        holder.itemView.text_mobileno.text = cardNumber
        holder.itemView.text_place.text = location


        holder.itemView.img_delete.setOnClickListener {
            deleteRFCard(locationList[position].app_usr_rf_id, locationList[position].app_rf_st_id)
        }

        holder.itemView.img_copy.setOnClickListener {
            copyRFCard(locationList[position])
        }
    }

    abstract fun deleteRFCard(appUsrRfId: Int, appRfStId: Int)

    abstract fun copyRFCard(rfCardModelItem: RFCardModelItem)
}