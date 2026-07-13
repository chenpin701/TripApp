package com.tripapp.ui.screens.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChecklistTab(viewModel: TripDetailViewModel) {
    val items by viewModel.checklist.collectAsState()

    LazyColumn(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(items, key = { it.id }) { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.isDone, onCheckedChange = { viewModel.toggleChecklist(item.id) })
                Text(item.name)
                Spacer(Modifier.width(6.dp))
                Text(if (item.owner == "p") "個人" else "團體", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
