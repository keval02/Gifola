package com.gifola.adapter

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gifola.R
import com.gifola.apis.ApiURLs
import com.gifola.constans.Global
import com.gifola.model.DashboardListData
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.dialoge_info.*
import kotlinx.android.synthetic.main.layout_dashboard_main_list_items.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


abstract class DashboardMainListAdapter(var context: Activity, var dashboardList: MutableList<DashboardListData>) : RecyclerView.Adapter<DashboardMainListAdapter.DashboardMainListViewHolder>() {


    class DashboardMainListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardMainListViewHolder {
        val itemView = context.layoutInflater.inflate(R.layout.layout_dashboard_main_list_items, parent, false)
        return DashboardMainListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dashboardList.size
    }

    override fun onBindViewHolder(holder: DashboardMainListViewHolder, position: Int) {
        val onlyDateFormat = SimpleDateFormat("dd/MM/YYYY")
        var visitDate: String = dashboardList[position].VisitDate.toString()
        var requestDate = dashboardList[position].RequestDate.toString()
        var visitFromTime = dashboardList[position].VisitTimeFrom.toString()
        var visitToTime = dashboardList[position].VisitTimeTo.toString()

        if (requestDate == null || requestDate == "") {
            requestDate = "-"
        } else {
            try {
                val parseDate: Date = Date(visitDate)
                val parseInDateFormatOnly = onlyDateFormat.format(parseDate)
                requestDate = parseInDateFormatOnly.toString()
            } catch (e: Exception) {
                Log.e("exception", e.message)
            }

        }


        if (visitDate == null || visitDate == "") {
            visitDate = "-"
        } else {
            val parseDate = Date(visitDate)
            val parseInDateFormatOnly = onlyDateFormat.format(parseDate)
            visitDate = parseInDateFormatOnly.toString()
        }

        if (visitFromTime != null || visitFromTime != "") {
            val splitFromTime = visitFromTime?.split(".")
            visitFromTime = splitFromTime?.get(0)
        }

        if (visitToTime != null || visitToTime != "") {
            val splitToTime = visitToTime?.split(".")
            visitToTime = splitToTime?.get(0)
        }


        Log.e("parseDateTime", visitFromTime)

        holder.itemView.text_date.text = requestDate ?: "-"
        holder.itemView.text_name.text = dashboardList[position].Name ?: "-"
        holder.itemView.text_mobileno.text = dashboardList[position].MobileNo ?: "-"
        holder.itemView.text_home.text = dashboardList[position].Location ?: "-"
        holder.itemView.text_time.text = "\n$visitDate\n$visitFromTime To $visitToTime"

        if (dashboardList[position].Purpose == null || dashboardList[position].Purpose == "") {
            holder.itemView.purposeTV.text = "-"
        } else {
            holder.itemView.purposeTV.text = dashboardList[position].Purpose
        }




        if (dashboardList[position].Mode == 0) {
            holder.itemView.requestTV.text = "Now"
        } else {
            holder.itemView.requestTV.text = "Schedule"
        }

        if (dashboardList[position].isReceieveRequest == false) {
            when (dashboardList[position].AcceptStatus) {
                0 -> {
                    holder.itemView.card_accept.visibility = View.GONE
                    holder.itemView.card_reject.visibility = View.VISIBLE
                }
                1 -> {
                    holder.itemView.card_accept.visibility = View.VISIBLE
                    holder.itemView.card_reject.visibility = View.GONE
                }
                2 -> {
                    holder.itemView.rejectedTV.text = "Pending"
                    holder.itemView.card_accept.visibility = View.GONE
                    holder.itemView.card_reject.visibility = View.VISIBLE
                }
            }
        } else {
            when (dashboardList[position].AcceptStatus) {
                0 -> {
                    holder.itemView.card_accept.visibility = View.GONE
                    holder.itemView.card_reject.visibility = View.VISIBLE
                }
                1 -> {
                    holder.itemView.card_accept.visibility = View.VISIBLE
                    holder.itemView.card_reject.visibility = View.GONE
                }
                2 -> {
                    holder.itemView.card_reject.visibility = View.VISIBLE
                    holder.itemView.card_accept.visibility = View.VISIBLE
                    holder.itemView.rejectedTV.text = "Reject"
                    holder.itemView.acceptTV.text = "Accept"
                }
            }

        }


        if(dashboardList[position].isReceieveRequest){
            holder.itemView.card_reject.setOnClickListener {
                dashboardList[position].isReceieveRequest = false
                dashboardList[position].AcceptStatus = 0
                holder.itemView.rejectedTV.text = "Rejected"
                notifyItemChanged(position)
                confirmationDialog(dashboardList[position].RequestID, 0)
            }
            holder.itemView.card_accept.setOnClickListener {
                dashboardList[position].isReceieveRequest = false
                dashboardList[position].AcceptStatus = 1
                holder.itemView.acceptTV.text = "Accepted"
                notifyItemChanged(position)
                confirmationDialog(dashboardList[position].RequestID, 1)
            }
        }

        Glide.with(context)
                .load(ApiURLs.IMAGE_URL + dashboardList[position].ProPic)
                .apply(RequestOptions().placeholder(R.drawable.user1).error(R.drawable.user1))
                .into(holder.itemView.profile_image)

        holder.itemView.infoclick.setOnClickListener {
            infoDialoge(dashboardList[position])
        }
        holder.itemView.menuimg.setOnClickListener(View.OnClickListener {
            if (holder.itemView.sublayout.visibility == View.VISIBLE) {
                holder.itemView.sublayout.visibility = View.GONE
            } else {
                holder.itemView.sublayout.visibility = View.VISIBLE
            }
        })
    }


    fun infoDialoge(dashboardListData: DashboardListData) {
        val dialog2 = Dialog(ContextThemeWrapper(context, R.style.AppTheme))
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog2.setCancelable(true)
        dialog2.setContentView(R.layout.dialoge_info)
        dialog2.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog2.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)



        if (!dashboardListData.Organization.isNullOrEmpty()) {
            dialog2.text_organization.text = dashboardListData.Organization
        } else {
            dialog2.text_organization.text = "-"
        }

        if (!dashboardListData.Designation.isNullOrEmpty()) {
            dialog2.text_designation.text = dashboardListData.Designation
        } else {
            dialog2.text_designation.text = "-"
        }

        if (!dashboardListData.Email.isNullOrEmpty()) {
            dialog2.text_emailid.text = dashboardListData.Email
        } else {
            dialog2.text_emailid.text = "-"
        }

        if (!dashboardListData.WorkAddress.isNullOrEmpty()) {
            dialog2.text_address.text = dashboardListData.WorkAddress
        } else {
            dialog2.text_address.text = "-"
        }
        dialog2.submit.setOnClickListener { dialog2.dismiss() }
        dialog2.img_no.setOnClickListener { dialog2.dismiss() }
        dialog2.show()
    }

    fun confirmationDialog(reqId: Int, status: Int){
        val alert: AlertDialog.Builder = AlertDialog.Builder(context)

        alert.setTitle("Update Status")
        alert.setMessage("Are you sure you want to update the status?")
        alert.setPositiveButton(android.R.string.yes) { dialog, which -> updateRequestStatus(reqId, status) }
        alert.setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialog, which -> // close dialog
            dialog.cancel()
        })
        alert.show()
    }

    abstract fun updateRequestStatus(reqId: Int, status: Int)

}