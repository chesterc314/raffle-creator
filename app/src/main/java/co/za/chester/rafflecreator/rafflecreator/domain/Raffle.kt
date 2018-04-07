package co.za.chester.rafflecreator.rafflecreator.domain

import java.util.*

data class Raffle(val name: String, private val participants: Set<Participant> = emptySet(), val id: UUID = UUID.randomUUID()) {
    fun addParticipant(part: Participant) = participants.plus(part)
}

data class Participant(val name: String, val raffleId: UUID)

object RaffleRepository {

}

object ParticipantRepository{

}