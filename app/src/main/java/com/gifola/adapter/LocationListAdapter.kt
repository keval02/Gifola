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
import com.gifola.customfonts.MyEditText
import com.gifola.customfonts.MyTextViewRegular
import com.gifola.model.LocationDataModelItem
import kotlinx.android.synthetic.main.layout_my_location_items.view.*

abstract class LocationListAdapter(var context: Activity, var locationList: MutableList<LocationDataModelItem>) : RecyclerView.Adapter<LocationListAdapter.LocationViewHolder>() {


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

        var locationTitle: String = locationList[position].site_title.toString()
        var locationAddress: String = locationList[position].site_address.toString()
        val locationID: String = locationList[position].mem_site_id.toString()

        if (locationTitle == null || locationTitle == "null" || locationTitle == "") {
            locationTitle = "No Title"
        }

        if (locationAddress == null || locationAddress == "null" || locationAddress == "") {
            locationAddress = ""
        }

        holder.itemView.text_officename.text = locationTitle
        holder.itemView.editimg.setOnClickListener {
            addressDialoge(locationTitle, locationAddress, locationID, position)
        }

    }

    private fun addressDialoge(locationTitle: String, locationAddress: String, locationID: String, position: Int) {
        val dialog = Dialog(ContextThemeWrapper(context, R.style.AppTheme))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialoge_add_address)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val submit = dialog.findViewById(R.id.submit) as MyTextViewRegular
        val img_no = dialog.findViewById(R.id.img_no) as ImageView
        val edit_alias = dialog.findViewById(R.id.edit_alias) as MyEditText
        val edit_address = dialog.findViewById(R.id.edit_address) as MyEditText

        edit_alias.setText(locationTitle)
        edit_address.setText(locationAddress)


        submit.setOnClickListener {
            var title = edit_alias.text.toString()
            var address = edit_address.text.toString()

            if(title == locationTitle && address == locationAddress){
                dialog.dismiss()
            }else {
                locationList[position].site_title = title
                locationList[position].site_address = address
                dialog.dismiss()
                notifyItemChanged(position)
                updateLocationDetails(title, address, locationID)
            }


        }
        img_no.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    abstract fun updateLocationDetails(locationTitle: String, locationAddress: String, locationID: String)
}