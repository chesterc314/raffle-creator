package co.za.chester.rafflecreator.rafflecreator

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import co.za.chester.rafflecreator.rafflecreator.domain.Participant
import co.za.chester.rafflecreator.rafflecreator.domain.Raffle
import co.za.chester.rafflecreator.rafflecreator.domain.Repository
import org.funktionale.option.firstOption
import org.funktionale.option.getOrElse
import org.funktionale.option.toOption
import java.util.*
import kotlin.collections.ArrayList

class RaffleActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var participantRecyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<String>
    private lateinit var participants: ArrayList<Participant>
    private lateinit var customRecyclerViewAdapter: CustomRecyclerViewAdapter
    private lateinit var autoCompleteTextViewParticipant: AutoCompleteTextView
    private lateinit var raffleName: String
    private lateinit var raffleId: UUID
    private lateinit var raffleRepository: Repository
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_raffle)
        raffleName = intent.getStringExtra(RAFFLE_NAME)
        raffleId = UUID.fromString(intent.getStringExtra(RAFFLE_ID))
        this.title = "Raffle: $raffleName"
        participantRecyclerView = findViewById(R.id.participantRecyclerView)
        autoCompleteTextViewParticipant = findViewById(R.id.autoCompleteTextViewParticipant)
        raffleRepository = Repository(this, getString(R.string.raffle_key))
        arrayList = java.util.ArrayList()
        customRecyclerViewAdapter = CustomRecyclerViewAdapter(arrayList, removeParticipantAction(), { _ ->
        }, false)
        val layoutManager = LinearLayoutManager(applicationContext)
        participantRecyclerView.layoutManager = layoutManager
        participantRecyclerView.itemAnimator = DefaultItemAnimator()
        participantRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        participantRecyclerView.adapter = customRecyclerViewAdapter
        val maybeSupportActionBar = supportActionBar.toOption()
        maybeSupportActionBar.map { bar -> bar.setDisplayHomeAsUpEnabled(true) }
        textToSpeech = TextToSpeech(this, this)
        populateParticipantList()
    }

    override fun onInit(p0: Int) {

    }

    private fun removeParticipantAction(): (ArrayList<String>, Int, RecyclerView.Adapter<CustomRecyclerViewAdapter.CustomViewHolder>) -> Unit {
        return { values, position, adapter ->
            participants.removeAll { p -> p.name == values[position] && p.raffleId == raffleId }
            values.removeAt(position)
            raffleRepository.saveString(getString(R.string.participant_key), Participant.fromObjects(participants))
            adapter.notifyDataSetChanged()
        }
    }

    override fun onPause() {
        raffleRepository.saveString(getString(R.string.participant_key), Participant.fromObjects(participants))
        super.onPause()
    }

    override fun onDestroy() {
        val maybeTextToSpeech = textToSpeech.toOption()
        maybeTextToSpeech.map { textToSpeech ->
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }

    private fun populateParticipantList() {
        val maybeParticipantData = raffleRepository.readString(getString(R.string.participant_key))
        participants = maybeParticipantData.map { participant ->
            ArrayList(Participant.toObjects(participant))
        }.getOrElse {
            ArrayList()
        }
        arrayList.addAll(ArrayList(participants.filter { p -> p.raffleId == raffleId }.map { p -> p.name }))
        customRecyclerViewAdapter.notifyDataSetChanged()
    }

    fun addParticipant(view: View) {
        val participantName: String = this.autoCompleteTextViewParticipant.text.toString()
        if (participantName.isEmpty()) {
            Toast.makeText(this, "No Participant Name entered", Toast.LENGTH_LONG).show()
        } else {
            val maybeDuplicateName = this.arrayList.firstOption { a -> a == participantName }
            maybeDuplicateName.map { duplicateName ->
                Toast.makeText(this, "Participant Name: $duplicateName is already added", Toast.LENGTH_LONG).show()
            }.getOrElse {
                arrayList.add(participantName)
                participants.add(Participant(participantName, raffleId))
                raffleRepository.saveString(getString(R.string.participant_key), Participant.fromObjects(participants))
                customRecyclerViewAdapter.notifyDataSetChanged()
            }
            this.autoCompleteTextViewParticipant.setText("")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val maybeItem = item.toOption()
        return maybeItem.map { menuItem ->
            when (menuItem.itemId) {
                android.R.id.home -> {
                    this.finish()
                    true
                }
                else -> false
            }
        }.getOrElse { super.onOptionsItemSelected(item) }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun speak(text: String) {
        val maybeTextToSpeech = textToSpeech.toOption()
        maybeTextToSpeech.map { textToSpeech ->
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun selectWinner(view: View) {
        val maybeWinner = Raffle(raffleName, participants.toSet(), raffleId).determineWinner()
        maybeWinner.map { winner ->
            AudioPlayer.play(this, R.raw.winnerannouncement, {
                val layoutInflater = LayoutInflater.from(this)
                val promptView = layoutInflater.inflate(R.layout.winner_prompt, null)
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setView(promptView)
                val winnerTextView = promptView.findViewById(R.id.textViewWinner) as TextView
                winnerTextView.text = winner.name
                speak(winner.name)
                alertDialogBuilder.setPositiveButton("Done", { dialog, _ ->
                    dialog.dismiss()
                })
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            })
        }.getOrElse {
            Toast.makeText(this, "No Participants to pick from", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val RAFFLE_NAME: String = "co.za.chester.rafflecreator.rafflecreator.RAFFLE_NAME"
        const val RAFFLE_ID: String = "co.za.chester.rafflecreator.rafflecreator.RAFFLE_ID"
    }
}