package co.za.chester.rafflecreator.rafflecreator

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private var exitCounter: Int = 0
    private lateinit var raffleRecyclerView: RecyclerView
    private lateinit var raffles: ArrayList<String>
    private lateinit var customRecyclerViewAdapter: CustomRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resetExitCounter()
        raffleRecyclerView = findViewById(R.id.raffleRecyclerView)
        raffles = ArrayList()
        customRecyclerViewAdapter = CustomRecyclerViewAdapter(raffles, { values, position, adapter ->
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder
                    .setCancelable(false)
                    .setMessage("Are you sure you want to remove this Raffle: ${values[position]}")
                    .setPositiveButton("Yes", { dialog, _ ->
                        values.removeAt(position)
                        adapter.notifyDataSetChanged()
                        dialog.dismiss()
                    })
                    .setNegativeButton("No",
                            { dialog, _ ->
                                dialog.cancel()
                            })

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        })
        val layoutManager = LinearLayoutManager(applicationContext)
        raffleRecyclerView.layoutManager = layoutManager
        raffleRecyclerView.itemAnimator = DefaultItemAnimator()
        raffleRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        raffleRecyclerView.adapter = customRecyclerViewAdapter
    }

    private fun resetExitCounter() {
        exitCounter = 2
    }

    override fun onBackPressed() {
        --exitCounter
        when (exitCounter) {
            1 -> Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_LONG).show()
            0 -> this.finish()
        }
    }

    fun addRaffle(view: View) {
        resetExitCounter()
        openRaffleNameDialog()
    }

    private fun openRaffleNameDialog() {
        val layoutInflater = LayoutInflater.from(this)
        val promptView = layoutInflater.inflate(R.layout.prompt, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(promptView)

        val raffleNameInput = promptView.findViewById(R.id.editTextDialogRaffleNameInput) as EditText
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Add", { dialog, _ ->
                    val raffleName = raffleNameInput.text.toString()
                    if (!raffleName.isEmpty()) {
                        Toast.makeText(this, "$raffleName added to list of raffles", Toast.LENGTH_LONG).show()
                        raffles.add(raffleName)
                        customRecyclerViewAdapter.notifyDataSetChanged()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "No Raffle Name entered", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    }
                })
                .setNegativeButton("Cancel",
                        { dialog, _ ->
                            dialog.cancel()
                        })

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
