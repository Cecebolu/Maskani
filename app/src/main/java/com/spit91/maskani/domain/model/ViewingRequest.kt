package com.spit91.maskani.domain.model

// A buyer's request to see a property. Maskani staff arrange the viewing with the landlord,
// so neither side ever gets the other's phone number.
data class ViewingRequest(
    val id: String = "",
    val propertyId: String = "",
    val propertyTitle: String = "",
    val propertyLocation: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerEmail: String = "",
    // buyer's own number, only visible to the buyer and Maskani staff, never to the landlord
    val buyerContact: String = "",
    val preferredDate: String = "", // yyyy-MM-dd
    val preferredTime: String = "",
    val note: String = "",
    val status: String = ViewingStatus.PENDING,
    val createdAt: Long = 0
)

object ViewingStatus {
    const val PENDING = "pending"
    const val CONFIRMED = "confirmed"
    const val COMPLETED = "completed"
    const val CANCELLED = "cancelled"
}

object ViewingTimeSlot {
    const val MORNING = "Morning (9am - 12pm)"
    const val AFTERNOON = "Afternoon (12pm - 4pm)"
    const val EVENING = "Evening (4pm - 6pm)"

    val all = listOf(MORNING, AFTERNOON, EVENING)
}
