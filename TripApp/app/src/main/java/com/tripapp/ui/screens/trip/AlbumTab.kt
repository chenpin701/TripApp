package com.tripapp.ui.screens.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AlbumTab(viewModel: TripDetailViewModel) {
    val photos by viewModel.photos.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(photos, key = { it.id }) { photo ->
            Card {
                Column {
                    AsyncImage(
                        model = photo.localCachePath ?: photo.storagePath,
                        contentDescription = photo.caption,
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                    Row(modifier = Modifier.padding(6.dp)) {
                        Text(photo.caption, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.toggleLike(photo.id) }) {
                            Text(if (photo.liked) "❤️" else "🤍")
                        }
                    }
                }
            }
        }
    }
}
