package com.spit91.maskani.presentation.auth.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spit91.maskani.domain.model.Property
import com.spit91.maskani.domain.model.UnitType
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage


@Composable
fun PropertyCard(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false,
    onToggleSaved: () -> Unit = {}
){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable (onClick = onClick),
        shape =RoundedCornerShape(16.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = property.imageUrls.firstOrNull(),
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)

                )
                IconButton (
                    onClick = onToggleSaved,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                ){
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite
                        else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isSaved) "Remove from saved" else "Save listing",
                        tint = if (isSaved) Color.Red else Color.DarkGray
                    )
                }
            }

            Column( modifier = Modifier.padding(16.dp)) {
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = property.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = unitTypeLabel(property.unitType),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${String.format("%,d", property.price)} / month",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            }
        }
    }
}
// turns the stored value(1_bedroom) into a human readable string (1 Bedroom)
fun unitTypeLabel(unitType: String): String = when (unitType) {
    UnitType.APARTMENT -> "Apartment"
    UnitType.HOUSE -> "House"
    UnitType.VILLA -> "Villa"
    UnitType.TOWN_HOUSE -> "Townhouse"
    UnitType.STUDIO -> "Studio"
    UnitType.BEDSITTER -> "Bedsitter"
    UnitType.ONE_BEDROOM -> "1 Bedroom"
    UnitType.TWO_BEDROOM -> "2 Bedrooms"
    UnitType.THREE_BEDROOM -> "3 Bedrooms"
    UnitType.FOUR_PLUS_BEDROOM -> "4+ Bedrooms"
    else -> unitType

}
