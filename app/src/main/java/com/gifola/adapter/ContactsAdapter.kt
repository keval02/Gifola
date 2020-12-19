package com.gifola.adapter

import android.app.Activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gifola.R
import com.gifola.model.ContactsModel
import com.wickerlabs.logmanager.LogObject
import kotlinx.android.synthetic.main.layout_call_logs_items.view.*


abstract class ContactsAdapter(var context: Activity, var checkInList: MutableList<ContactsModel>) : RecyclerView.Adapter<ContactsAdapter.ContactsListViewHolder>() {


    class ContactsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsListViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.layout_call_logs_items, parent, false)
        return ContactsListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return checkInList.size
    }

    override fun onBindViewHolder(holder: ContactsListViewHolder, position: Int) {
        holder.itemView.phoneNum.text = checkInList[position].contactNum
        holder.itemView.phoneName.text = checkInList[position].contactName
        holder.itemView.callImage.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_person_24))

        holder.itemView.callLayout.setOnClickListener {
            selectNumber(checkInList[position].contactNum , checkInList[position].contactName)
        }
    }

    fun searchResult(searchText : String): ArrayList<ContactsModel> {
        val searchedList : ArrayList<ContactsModel> = ArrayList()
        checkInList.forEach {
            if(it.contactNum.contains(searchText) || it.contactName.toLowerCase().contains(searchText.toLowerCase())){
                searchedList.add(it)
            }
        }
        return searchedList
    }

    abstract fun selectNumber(number: String, contactName: String)

}