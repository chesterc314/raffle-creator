package co.za.chester.rafflecreator.rafflecreator

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

class CustomRecyclerViewAdapter(private val values: ArrayList<String>,
                                private val removeAction: (ArrayList<String>, Int, RecyclerView.Adapter<CustomRecyclerViewAdapter.CustomViewHolder>) -> Unit,
                                private val editAction: (Int) -> Unit) : RecyclerView.Adapter<CustomRecyclerViewAdapter.CustomViewHolder>() {
    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById<View>(R.id.label) as TextView
        val removeButton: ImageButton = view.findViewById(R.id.imageButtonRemoveRaffle) as ImageButton
        val editButton: ImageButton = view.findViewById(R.id.imageButtonEditRaffle) as ImageButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_mobile, parent, false)
        return CustomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val value = this.values[position]
        holder.label.text = value
        holder.removeButton.setOnClickListener({ _ ->
            removeAction(this.values, position, this)
        })
        holder.editButton.setOnClickListener({ _ ->
            editAction(position)
        })
    }

    override fun getItemCount(): Int {
        return this.values.count()
    }
}
