package com.spit91.maskani.domain.model

data class Property(
    val id: String = "",
    val ownerId: String = "",
    val title: String = "",
    val description: String = "",
    val price: Long = 0,
    val location: String = "",
    val imageUrls: List<String> = emptyList(),
    val status: String = "draft",
    val unitType: String = "",
)

object UnitType {
    const val APARTMENT = "apartment"
    const val HOUSE = "house"
    const val VILLA = "villa"
    const val TOWN_HOUSE = "townhouse"
    const val STUDIO = "studio"
    const val BEDSITTER = "bedsitter"
    const val ONE_BEDROOM = "1_bedroom"
    const val TWO_BEDROOM = "2_bedroom"
    const val THREE_BEDROOM = "3_bedroom"
    const val FOUR_PLUS_BEDROOM = "4_plus_bedroom"
}

object PropertyStatus{
    const val DRAFT = "draft"
    const val PESNDING_REVIEW = "pending_review"
    const val PUBLISHED = "published"
    const val REJECTED = "rejected"
}