package com.example.asc_mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.asc_mobile.R
import com.example.asc_mobile.model.SkippingRequest
import com.example.asc_mobile.model.Confirmation
import com.example.asc_mobile.AbsencesActivity

class SkippingRequestsAdapter(
    private var skippingRequests: List<SkippingRequest>,
    private val onItemClick: (SkippingRequest) -> Unit
    ) : RecyclerView.Adapter<SkippingRequestsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentName: TextView = view.findViewById(R.id.studentName)
        val startDate: TextView = view.findViewById(R.id.startDate)
        val endDate: TextView = view.findViewById(R.id.endDate)
        val reason: TextView = view.findViewById(R.id.reason)
        val lessons: TextView = view.findViewById(R.id.lessons)
        val status: TextView = view.findViewById(R.id.status)
        val confirmations: TextView = view.findViewById(R.id.confirmations)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skipping_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = skippingRequests[position]
        holder.studentName.text = "Студент: ${request.student.name}"
        holder.startDate.text = "Дата начала: ${request.startDate}"
        holder.endDate.text = "Дата окончания: ${request.endDate}"
        holder.reason.text = "Причина: ${request.reason}"
        holder.lessons.text = "Пары: ${request.lessons?.joinToString() ?: "Нет данных"}"
        holder.status.text = "Статус: ${request.status}"

        val documentsText = request.confirmations?.takeIf { it.isNotEmpty() }
            ?.joinToString(", ") { it.filename }
            ?: "Нет"

        holder.confirmations.text = "Подтверждающие документы: $documentsText"

        if (request.status == "REJECTED") {
            holder.itemView.isClickable = false
            holder.itemView.isFocusable = false
            holder.itemView.alpha = 0.5f
        } else {
            holder.itemView.isClickable = true
            holder.itemView.isFocusable = true
            holder.itemView.alpha = 1.0f

            holder.itemView.setOnClickListener {
                onItemClick(request)
            }
        }
    }

    override fun getItemCount() = skippingRequests.size

    fun updateData(newList: List<SkippingRequest>) {
        skippingRequests = newList
        notifyDataSetChanged()
    }
}