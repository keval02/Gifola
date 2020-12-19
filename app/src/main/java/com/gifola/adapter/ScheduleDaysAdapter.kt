package com.gifola.adapter

import android.app.Activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gifola.R
import com.gifola.model.ScheduleDaysModel
import kotlinx.android.synthetic.main.layout_schedule_days_items.view.*


class ScheduleDaysAdapter(var context: Activity, var daysList: MutableList<ScheduleDaysModel>) : RecyclerView.Adapter<ScheduleDaysAdapter.ScheduleDaysViewHolder>() {


    class ScheduleDaysViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleDaysViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.layout_schedule_days_items, parent, false)
        return ScheduleDaysViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return daysList.size
    }

    override fun onBindViewHolder(holder: ScheduleDaysViewHolder, position: Int) {
        holder.itemView.circleTV.text = daysList[position].days

        holder.itemView.circleTV.setOnClickListener {
            if(daysList[position].isSelected){
                daysList[position].isSelected = false
                holder.itemView.circleTV.setBackgroundDrawable(context.getDrawable(R.drawable.rounded_border))
                holder.itemView.circleTV.setTextColor(context.resources.getColor(android.R.color.black))
            }else {
                daysList[position].isSelected = true
                holder.itemView.circleTV.setBackgroundDrawable(context.getDrawable(R.drawable.rounded_solid))
                holder.itemView.circleTV.setTextColor(context.resources.getColor(android.R.color.white))
            }
        }
    }

    fun getSelectedDays(): ArrayList<String> {
        val selectedList : ArrayList<String> = ArrayList()
        daysList.forEach {
            if(it.isSelected)
            selectedList.add(it.days)
        }
        return selectedList
    }

}