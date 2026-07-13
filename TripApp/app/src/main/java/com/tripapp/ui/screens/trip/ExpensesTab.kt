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
import com.tripapp.data.local.entity.ExpenseEntity
import com.tripapp.data.repository.SplitCalculator

@Composable
fun ExpensesTab(viewModel: TripDetailViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val members by viewModel.members.collectAsState()
    val balances by viewModel.balances.collectAsState()

    Column(modifier = Modifier.padding(14.dp)) {
        Text("結算總覽", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        balances.forEach { balance ->
            val member = members.find { it.id == balance.memberId }
            val label = if (balance.netTwd >= 0) "應收 NT$${balance.netTwd}" else "應付 NT$${-balance.netTwd}"
            Text("${member?.name ?: balance.memberId}：$label")
        }
        Spacer(Modifier.height(6.dp))
        Button(onClick = { viewModel.settleAll() }) { Text("標記全部已結清") }

        Spacer(Modifier.height(14.dp))
        Text("明細", fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses, key = { it.id }) { exp -> ExpenseRow(exp) }
        }
    }
}

@Composable
private fun ExpenseRow(exp: ExpenseEntity) {
    Card {
        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(exp.name, fontWeight = FontWeight.SemiBold)
                Text("${exp.category} · ${exp.date}", style = MaterialTheme.typography.bodySmall)
            }
            Text("${exp.currency} ${exp.amount}")
            if (exp.isSettled) {
                Spacer(Modifier.width(6.dp))
                Text("✓ 已結清", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
