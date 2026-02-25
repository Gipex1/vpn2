package com.example.axiomvpn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator

class ServerAdapter(
    private var servers: List<Server>,
    private val onItemClick: (Server) -> Unit,
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    class ServerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView as MaterialCardView
        val tvFlag: TextView = itemView.findViewById(R.id.tv_flag)
        val tvCountry: TextView = itemView.findViewById(R.id.tv_country)
        val tvIp: TextView = itemView.findViewById(R.id.tv_ip)
        val tvPing: TextView = itemView.findViewById(R.id.tv_ping)
        val progressLoad: LinearProgressIndicator = itemView.findViewById(R.id.progress_load)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = servers[position]

        holder.tvFlag.text = server.flagEmoji
        holder.tvCountry.text = server.country
        holder.tvIp.text = server.ip
        holder.tvPing.text = "${server.ping} ms"
        holder.progressLoad.progress = server.load
        holder.card.setOnClickListener { onItemClick(server) }
    }

    override fun getItemCount(): Int = servers.size

    fun updateData(newServers: List<Server>) {
        servers = newServers
        notifyDataSetChanged()
    }
}