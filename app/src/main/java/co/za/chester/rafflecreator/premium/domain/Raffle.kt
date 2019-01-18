package co.za.chester.rafflecreator.premium.domain

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import org.funktionale.option.Option
import org.funktionale.option.toOption
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

data class Raffle(val name: String, val participants: Set<Participant> = emptySet(), val id: UUID = UUID.randomUUID()) {
    fun determineWinner(): Option<Participant> {
        val raffleParticipants: List<Participant> = this.participants
                .filter { participant -> participant.raffleId == this.id }
                .flatMap { participant ->
                    val numberOfParticipantPerEntry: List<Participant> = (0 until participant.entryCount)
                            .toList()
                            .map { participant }
                    numberOfParticipantPerEntry
                }
        val maybeRandomIndex = if (raffleParticipants.isNotEmpty()) {
            Option.Some(Random().nextInt(raffleParticipants.size))
        } else {
            Option.None
        }

        return maybeRandomIndex.flatMap { randomIndex -> raffleParticipants.elementAtOrNull(randomIndex).toOption() }
    }

    override fun toString(): String = JSONObject()
            .put("name", this.name)
            .put("id", this.id.toString()).toString()

    companion object {
        private fun toObject(json: String): Raffle {
            val jsonObject = JSONObject(json)
            val name: String = jsonObject.getString("name")
            val id: String = jsonObject.getString("id")
            return Raffle(
                    name,
                    id = UUID.fromString(id)
            )
        }

        fun fromObjects(raffles: ArrayList<Raffle>): String {
            val jsonArray = JSONArray()
            raffles.forEach { raffle -> jsonArray.put(raffle.toString()) }
            return jsonArray.toString()
        }

        fun toObjects(json: String): ArrayList<Raffle> {
            val jsonArray = JSONArray(json)
            val raffles: ArrayList<Raffle> = ArrayList()
            (0 until jsonArray.length()).forEach { index -> raffles.add(toObject(jsonArray.getString(index))) }
            return raffles
        }

        fun toRaffle(raw: String): Option<Raffle> {
            return try {
                val lines = raw.lines()
                val raffleName = lines[0].split(":")[1].trim()
                val raffleId = UUID.randomUUID()
                val participants = lines.filter { it -> !it.contains("Raffle Name:") }.map { line ->
                    val cleanedLine =
                            line.replace("Name: ", "")
                                    .replace(" Entries", "")
                    val name = cleanedLine.split(":")[0]
                    val count = cleanedLine.split(":")[1].trim().toInt()
                    Participant(name, raffleId, count)
                }.toSet()
                Option.Some(Raffle(raffleName, participants, raffleId))
            }catch (e: Exception){
                Option.None
            }
        }
    }
}

data class Participant(val name: String, val raffleId: UUID, val entryCount: Int) {
    override fun toString(): String = JSONObject()
            .put("name", this.name)
            .put("entryCount", this.entryCount)
            .put("raffleId", this.raffleId.toString()).toString()

    companion object {
        private fun toObject(json: String): Participant {
            val jsonObject = JSONObject(json)
            val name: String = jsonObject.getString("name")
            val raffleId: String = jsonObject.getString("raffleId")
            val entryCount: Int = jsonObject.getInt("entryCount")
            return Participant(name, UUID.fromString(raffleId), entryCount)
        }

        fun fromObjects(participants: ArrayList<Participant>): String {
            val jsonArray = JSONArray()
            participants.forEach { participant -> jsonArray.put(participant.toString()) }
            return jsonArray.toString()
        }

        fun toObjects(json: String): ArrayList<Participant> {
            val jsonArray = JSONArray(json)
            val participants: ArrayList<Participant> = ArrayList()
            (0 until jsonArray.length()).forEach { index -> participants.add(toObject(jsonArray.getString(index))) }
            return participants
        }
    }
}

class Repository(activity: Activity, keyRepo: String) {
    private var sharedPref: SharedPreferences = activity.getSharedPreferences(keyRepo, Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        with(this.sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun readString(key: String, value: String = ""): Option<String> {
        val result: String = sharedPref.getString(key, value)
        return (if (result.isEmpty()) null else result).toOption()
    }
}
