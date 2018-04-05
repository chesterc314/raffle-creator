package co.za.chester.rafflecreator.rafflecreator

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import android.support.v7.app.AlertDialog
import android.widget.EditText
import android.view.LayoutInflater


class MainActivity : AppCompatActivity() {

    private var exitCounter: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resetExitCounter()
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

        val raffleNameInput = promptView
                .findViewById(R.id.editTextDialogRaffleNameInput) as EditText
        alertDialogBuilder
                .setPositiveButton("Add",
                        { dialog, _ ->
                            val raffleName = raffleNameInput.text
                            if (raffleName.isNullOrEmpty()) {
                                Toast.makeText(this, "No Raffle Name entered", Toast.LENGTH_LONG).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this, "$raffleName added to list of raffles", Toast.LENGTH_LONG).show()
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
