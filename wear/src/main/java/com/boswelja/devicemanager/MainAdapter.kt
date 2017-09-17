package com.boswelja.devicemanager

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class MainAdapter(private val options: ArrayList<MainOption>): RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return options.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val option = options[position]
        holder?.label?.text = option.label
        holder?.icon?.setImageResource(option.iconRes)
        holder?.itemView?.setOnClickListener {
            when (option.type) {
                Config.TYPE_LOCK_PHONE -> {
                    val intent = Intent()
                    intent.putExtra("action", option.type)
                    intent.action = Config.INTENT_PERFORM_ACTION
                    holder.itemView.context.sendBroadcast(intent)
                }
                Config.TYPE_TOGGLE_WIFI -> {
                    val intent = Intent()
                    intent.putExtra("action", option.type)
                    intent.action = Config.INTENT_PERFORM_ACTION
                    holder.itemView.context.sendBroadcast(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.model_main_option, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
    }
}
