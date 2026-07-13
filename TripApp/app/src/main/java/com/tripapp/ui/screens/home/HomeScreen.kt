package com.tripapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripapp.data.local.entity.TripEntity

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    currentUid: String,
    onOpenTrip: (String) -> Unit
) {
    val trips by viewModel.trips.collectAsState()
    var showNewTripDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("我的行程") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewTripDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "新增行程")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(trips, key = { it.id }) { trip ->
                TripCard(trip = trip, onClick = { onOpenTrip(trip.id) })
            }
        }
    }

    if (showNewTripDialog) {
        NewTripDialog(
            onDismiss = { showNewTripDialog = false },
            onConfirm = { name, dest, dates ->
                viewModel.createTrip(name, dest, dates, "✈️", currentUid)
                showNewTripDialog = false
            }
        )
    }
}

@Composable
private fun TripCard(trip: TripEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(trip.cover, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(trip.name, fontWeight = FontWeight.Bold)
                Text(trip.dest, style = MaterialTheme.typography.bodySmall)
                Text("${trip.startDate} → ${trip.endDate}", style = MaterialTheme.typography.bodySmall)
            }
            StatusPill(status = trip.status)
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    val (label, bg) = if (status == "upcoming") "即將出發" to MaterialTheme.colorScheme.primary
                       else "已結束" to MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun NewTripDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var dest by remember { mutableStateOf("") }
    var dates by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增行程") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("行程名稱") })
                OutlinedTextField(value = dest, onValueChange = { dest = it }, label = { Text("目的地") })
                OutlinedTextField(value = dates, onValueChange = { dates = it }, label = { Text("日期") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, dest, dates) }) { Text("建立") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
