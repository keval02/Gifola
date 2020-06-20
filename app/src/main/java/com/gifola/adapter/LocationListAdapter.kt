package com.gifola.adapter

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.gifola.R
import com.gifola.customfonts.MyTextViewRegular
import com.gifola.model.LocationDataModelItem
import kotlinx.android.synthetic.main.layout_my_location_items.view.*

class LocationListAdapter(var context: Activity, var locationList: MutableList<LocationDataModelItem>) : RecyclerView.Adapter<LocationListAdapter.LocationViewHolder>() {


    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.layout_my_location_items, parent, false)
        return LocationViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {

        val locationTitle: String = locationList[position].site_title.toString()

        if (locationTitle == null || locationTitle == "null" || locationTitle == "") {
            holder.itemView.text_officename.text = "No Title"
        } else {
            holder.itemView.text_officename.text = locationTitle
        }

        holder.itemView.editimg.setOnClickListener {
            addressDialoge()
        }

    }

    private fun addressDialoge() {
        val dialog = Dialog(ContextThemeWrapper(context, R.style.AppTheme))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialoge_add_address)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val submit = dialog.findViewById(R.id.submit) as MyTextViewRegular
        val img_no = dialog.findViewById(R.id.img_no) as ImageView
        submit.setOnClickListener { dialog.dismiss() }
        img_no.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}