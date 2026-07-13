package com.tripapp.ui.screens.trip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

private data class TabItem(val label: String, val content: @Composable () -> Unit)

@Composable
fun TripDetailScreen(viewModel: TripDetailViewModel, tripName: String, onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        TabItem("行程") { ItineraryTab(viewModel) },
        TabItem("分帳") { ExpensesTab(viewModel) },
        TabItem("投票") { VoteTab(viewModel) },
        TabItem("清單") { ChecklistTab(viewModel) },
        TabItem("文件") { DocsTab(viewModel) },
        TabItem("相簿") { AlbumTab(viewModel) },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tripName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, tab ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(tab.label) })
                }
            }
            tabs[selectedTab].content()
        }
    }
}
