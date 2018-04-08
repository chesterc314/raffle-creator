package co.za.chester.rafflecreator.rafflecreator

import android.content.Intent
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
import co.za.chester.rafflecreator.rafflecreator.domain.Raffle
import co.za.chester.rafflecreator.rafflecreator.domain.Repository


class MainActivity : AppCompatActivity() {

    private var exitCounter: Int = 0
    private lateinit var raffleRecyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<String>
    private lateinit var raffles: ArrayList<Raffle>
    private lateinit var customRecyclerViewAdapter: CustomRecyclerViewAdapter
    private lateinit var raffleRepository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resetExitCounter()
        raffleRecyclerView = findViewById(R.id.raffleRecyclerView)
        raffleRepository = Repository(this, getString(R.string.raffle_key))
        populateRaffleList()
        customRecyclerViewAdapter = CustomRecyclerViewAdapter(arrayList, { values, position, adapter ->
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder
                    .setCancelable(false)
                    .setMessage("Are you sure you want to remove this Raffle: ${values[position]}")
                    .setPositiveButton("Yes", { dialog, _ ->
                        values.removeAt(position)
                        raffles.removeAt(position)
                        raffleRepository.saveString(getString(R.string.raffle_name_key), Raffle.fromObjects(raffles))
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

    override fun onPause() {
        super.onPause()
        raffleRepository.saveString(getString(R.string.raffle_name_key), Raffle.fromObjects(raffles))
    }

    override fun onStart() {
        super.onStart()
        populateRaffleList()
    }

    private fun populateRaffleList() {
        val raffleData = raffleRepository.readString(getString(R.string.raffle_name_key))
        raffles = Raffle.toObjects(raffleData)
        arrayList = if (raffleData.isNullOrEmpty()) {
            ArrayList()
        } else {
            ArrayList(Raffle.toObjects(raffleData).map { raffle -> raffle.name }.toList())
        }
        customRecyclerViewAdapter.notifyDataSetChanged()
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
                        Toast.makeText(this, "$raffleName added to list of arrayList", Toast.LENGTH_LONG).show()
                        arrayList.add(raffleName)
                        customRecyclerViewAdapter.notifyDataSetChanged()
                        val raffle = Raffle(raffleName)
                        raffles.add(raffle)
                        raffleRepository.saveString(getString(R.string.raffle_name_key), Raffle.fromObjects(raffles))
                        openParticipantActivity(raffle)
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

    private fun openParticipantActivity(raffle: Raffle) {
        val raffleIntent = Intent(this, RaffleActivity::class.java)
        raffleIntent.putExtra(RaffleActivity.RAFFLE_NAME, raffle.name)
        raffleIntent.putExtra(RaffleActivity.RAFFLE_ID, raffle.id.toString())
        startActivity(raffleIntent)
    }
}
