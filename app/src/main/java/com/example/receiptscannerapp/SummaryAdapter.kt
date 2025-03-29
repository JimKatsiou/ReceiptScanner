package com.example.receiptscannerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SummaryAdapter(private val summaryList: List<MonthSummary>) :
    RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    class SummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val monthTextView: TextView = view.findViewById(R.id.monthTextView)
        val totalTextView: TextView = view.findViewById(R.id.totalTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.summary_item, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val summary = summaryList[position]
        holder.monthTextView.text = summary.month
        holder.totalTextView.text = "Total: $${summary.total}"
    }

    override fun getItemCount() = summaryList.size
}
