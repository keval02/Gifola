package com.gifola.adapter

import android.app.Activity
import android.util.Log

import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.gifola.R
import com.google.gson.Gson
import com.wickerlabs.logmanager.LogObject
import kotlinx.android.synthetic.main.layout_call_logs_items.view.*


abstract class CallLogsAdapter(var context: Activity, var checkInList: MutableList<LogObject>, var storedList: MutableList<LogObject>) : RecyclerView.Adapter<CallLogsAdapter.CallLogsListViewHolder>() {

    class CallLogsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogsListViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.layout_call_logs_items, parent, false)
        return CallLogsListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return checkInList.size
    }

    override fun onBindViewHolder(holder: CallLogsListViewHolder, position: Int) {
        holder.itemView.phoneNum.text = checkInList[position].number
        holder.itemView.phoneName.text = checkInList[position].contactName

        holder.itemView.callLayout.setOnClickListener {
            selectNumber(checkInList[position].number, checkInList[position].contactName)
        }
    }

    fun searchResult(searchText: String): ArrayList<LogObject> {
        val searchedList: ArrayList<LogObject> = ArrayList()
        storedList.forEach {
            if (it.number.contains(searchText) || it.contactName.toLowerCase().contains(searchText.toLowerCase())) {
                searchedList.add(it)
            }
        }

        return searchedList
    }

    abstract fun selectNumber(number: String, contactName: String)

}