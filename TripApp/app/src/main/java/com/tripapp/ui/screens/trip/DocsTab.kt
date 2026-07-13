package com.tripapp.ui.screens.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DocsTab(viewModel: TripDetailViewModel) {
    val docs by viewModel.documents.collectAsState()

    Column(modifier = Modifier.padding(14.dp)) {
        Text("文件會先加密再上傳，雲端只存密文", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(docs, key = { it.id }) { doc ->
                Card {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Text("📄 ")
                        Text(doc.name)
                        Spacer(Modifier.weight(1f))
                        Text(doc.date, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        // 實際專案：搭配 ActivityResultContracts.GetContent() 選檔後
        // 呼叫 viewModel.uploadDocument(name, mime, bytes, date)
    }
}
