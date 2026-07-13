package com.tripapp.ui.screens.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripapp.data.local.entity.ItineraryItemEntity

@Composable
fun ItineraryTab(viewModel: TripDetailViewModel) {
    val items by viewModel.itinerary.collectAsState()
    val grouped = items.groupBy { it.day }.toSortedMap()

    LazyColumn(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        grouped.forEach { (day, dayItems) ->
            item {
                Text("Day $day · ${dayItems.firstOrNull()?.dayTitle.orEmpty()}", fontWeight = FontWeight.Bold)
            }
            items(dayItems, key = { it.id }) { item -> ItineraryRow(item, onVote = { viewModel.voteItinerary(item.id) }) }
        }
    }
}

@Composable
private fun ItineraryRow(item: ItineraryItemEntity, onVote: () -> Unit) {
    Card {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${item.time}　${item.name}", fontWeight = FontWeight.SemiBold)
                Text(item.note, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onVote) { Text("👍 ${item.votes}") }
        }
    }
}
