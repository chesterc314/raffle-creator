package co.za.chester.rafflecreator.rafflecreator

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private var exitCounter: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resetExitCounter()
    }

    private fun resetExitCounter(){
        exitCounter = 2
    }

    override fun onBackPressed() {
        --exitCounter
        when (exitCounter){
            1 -> Toast.makeText(this, "Press back again and app will be exited!", Toast.LENGTH_LONG).show()
            0 -> this.finish()
        }
    }

    fun addRaffle(view: View){
        resetExitCounter()
        Toast.makeText(this, "TODO: Popup to enter Raffle name", Toast.LENGTH_LONG).show()
    }
}
