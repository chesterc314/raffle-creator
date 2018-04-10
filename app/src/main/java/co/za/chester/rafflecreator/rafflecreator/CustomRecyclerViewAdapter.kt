package co.za.chester.rafflecreator.rafflecreator

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val label: TextView = view.findViewById<View>(R.id.label) as TextView
    val removeButton: ImageButton = view.findViewById(R.id.imageButtonRemoveRaffle) as ImageButton
    val editButton: ImageButton = view.findViewById(R.id.imageButtonEditRaffle) as ImageButton
}

class CustomRecyclerViewAdapter<T>(
        private val values: ArrayList<T>,
        private val viewAction: (T, CustomViewHolder) -> Unit,
        private val removeAction: (ArrayList<T>, Int, RecyclerView.Adapter<CustomViewHolder>) -> Unit,
        private val editAction: (Int) -> Unit) : RecyclerView.Adapter<CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_mobile, parent, false)
        return CustomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val value = this.values[position]
        viewAction(value, holder)

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
