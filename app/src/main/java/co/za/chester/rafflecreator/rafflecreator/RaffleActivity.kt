package co.za.chester.rafflecreator.rafflecreator

import android.annotation.TargetApi
import android.content.Intent
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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import co.za.chester.rafflecreator.rafflecreator.domain.Participant
import co.za.chester.rafflecreator.rafflecreator.domain.Raffle
import co.za.chester.rafflecreator.rafflecreator.domain.Repository
import org.funktionale.option.Option
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
    private lateinit var entryCountEditText: EditText
    private lateinit var raffleName: String
    private lateinit var raffleId: UUID
    private lateinit var raffleRepository: Repository
    private lateinit var textToSpeech: TextToSpeech
    private var maybeParticipant: Option<Participant> = Option.empty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_raffle)
        raffleName = intent.getStringExtra(RAFFLE_NAME)
        raffleId = UUID.fromString(intent.getStringExtra(RAFFLE_ID))
        this.title = "Raffle: $raffleName"
        participantRecyclerView = findViewById(R.id.participantRecyclerView)
        autoCompleteTextViewParticipant = findViewById(R.id.autoCompleteTextViewParticipant)
        entryCountEditText = findViewById(R.id.entryCountEditText)
        raffleRepository = Repository(this, getString(R.string.raffle_key))
        arrayList = java.util.ArrayList()
        customRecyclerViewAdapter = CustomRecyclerViewAdapter(arrayList, removeParticipantAction(), { position ->
            maybeParticipant = Option.Some(participants.filter { p -> p.raffleId == raffleId }[position])
            maybeParticipant.map { participant ->
                this.autoCompleteTextViewParticipant.setText(participant.name)
                this.entryCountEditText.setText(participant.entryCount.toString())
            }
        })
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
        val entryCountText = this.entryCountEditText.text.toString().trim()
        val entryCount: Int = if(entryCountText.isEmpty()){1}else{entryCountText.toInt()}
        if (participantName.isEmpty()) {
            Toast.makeText(this, "No Participant Name entered", Toast.LENGTH_LONG).show()
        } else {
            maybeParticipant.map { participant ->
                arrayList.remove(participant.name)
                participants.remove(participant)
                addParticipantToList(participantName, entryCount)
                maybeParticipant = Option.empty()
            }.getOrElse {
                checkForDuplicate(participantName, entryCount)
            }
        }
    }

    private fun checkForDuplicate(participantName: String, entryCount: Int) {
        val maybeDuplicateName = this.arrayList.firstOption { a -> a.trim() == participantName.trim() }
        maybeDuplicateName.map { duplicateName ->
            Toast.makeText(this, "Participant Name: $duplicateName is already added", Toast.LENGTH_LONG).show()
        }.getOrElse {
            addParticipantToList(participantName, entryCount)
        }
    }

    private fun addParticipantToList(participantName: String, entryCount: Int) {
        arrayList.add(participantName)
        participants.add(Participant(participantName, raffleId, entryCount))
        raffleRepository.saveString(getString(R.string.participant_key), Participant.fromObjects(participants))
        customRecyclerViewAdapter.notifyDataSetChanged()
        clearFields()
    }

    private fun clearFields() {
        this.autoCompleteTextViewParticipant.setText("")
        this.entryCountEditText.setText("")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val maybeItem = item.toOption()
        return maybeItem.map { menuItem ->
            when (menuItem.itemId) {
                android.R.id.home -> {
                    this.finish()
                    true
                }
                R.id.menu_item_share -> {
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    val participantNamesForSharing: String = participants
                            .filter { p -> p.raffleId == raffleId }
                            .sortedBy { p -> p.name }
                            .fold("", { acc, p -> acc + "Name: ${p.name} Entries: ${p.entryCount}\n"})
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Raffle Name: $raffleName\n$participantNamesForSharing")
                    shareIntent.type = "text/plain"
                    startActivity(Intent.createChooser(shareIntent, "Share with"))
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
        clearFields()
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