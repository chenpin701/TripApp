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

@Composable
fun VoteTab(viewModel: TripDetailViewModel) {
    val proposals by viewModel.votes.collectAsState()
    val members by viewModel.members.collectAsState()
    val myMemberId = members.firstOrNull()?.id ?: return // 示範用第一位成員代表「我」，實際專案綁定登入帳號對應的 memberId

    LazyColumn(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(proposals, key = { it.id }) { p ->
            val voterIds = p.voterMemberIds.split(",").filter { it.isNotBlank() }
            Card {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(p.emoji, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.name, fontWeight = FontWeight.SemiBold)
                        Text(p.note, style = MaterialTheme.typography.bodySmall)
                    }
                    val voted = myMemberId in voterIds
                    TextButton(onClick = { viewModel.toggleVote(p, myMemberId) }) {
                        Text(if (voted) "已投 ${voterIds.size}" else "投票 ${voterIds.size}")
                    }
                }
            }
        }
    }
}
