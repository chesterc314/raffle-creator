package co.za.chester.rafflecreator.rafflecreator.domain

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import org.funktionale.option.Option
import org.funktionale.option.toOption
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

data class Raffle(val name: String, private val participants: Set<Participant> = emptySet(), val id: UUID = UUID.randomUUID()) {
    fun determineWinner(): Option<Participant> = this.participants.shuffled().firstOrNull().toOption()
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
            raffles.forEach{ raffle -> jsonArray.put(raffle.toString()) }
            return jsonArray.toString()
        }

        fun toObjects(json: String): ArrayList<Raffle> {
            val jsonArray = JSONArray(json)
            val raffles : ArrayList<Raffle> = ArrayList()
            (0 until jsonArray.length()).forEach{ index -> raffles.add(toObject(jsonArray.getString(index))) }
            return raffles
        }
    }
}

data class Participant(val name: String, val raffleId: UUID) {
    override fun toString(): String = JSONObject()
            .put("name", this.name)
            .put("raffleId", this.raffleId.toString()).toString()

    companion object {
        private fun toObject(json: String): Participant {
            val jsonObject = JSONObject(json)
            val name: String = jsonObject.getString("name")
            val raffleId: String = jsonObject.getString("raffleId")
            return Participant(name, UUID.fromString(raffleId))
        }

        fun fromObjects(participants: ArrayList<Participant>): String {
            val jsonArray = JSONArray()
            participants.forEach{ participant -> jsonArray.put(participant.toString()) }
            return jsonArray.toString()
        }

        fun toObjects(json: String): ArrayList<Participant> {
            val jsonArray = JSONArray(json)
            val participants : ArrayList<Participant> = ArrayList()
            (0 until jsonArray.length()).forEach{ index -> participants.add(toObject(jsonArray.getString(index))) }
            return participants
        }
    }
}

class Repository(activity: Activity, keyRepo: String) {
    private var sharedPref: SharedPreferences = activity.getSharedPreferences(keyRepo, Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        with(this.sharedPref.edit()) {
            putString(key, value)
            commit()
        }
    }

    fun readString(key: String, value: String = ""): Option<String> {
        val result: String = sharedPref.getString(key, value)
        return (if(result.isEmpty()) null else result).toOption()
    }
}