package com.clipboardreminder.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.clipboardreminder.domain.model.ReminderUi
import kotlinx.coroutines.delay

@Composable
fun PipOverlayContent(
    reminders: List<ReminderUi>,
    copySuccessId: Long?,
    onCopy: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD1A1A1A))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(reminders, key = { it.id }) { reminder ->
                PipReminderItem(
                    reminder = reminder,
                    isCopied = copySuccessId == reminder.id,
                    onCopy = { onCopy(reminder.id) }
                )
            }
        }
    }
}

@Composable
private fun PipReminderItem(
    reminder: ReminderUi,
    isCopied: Boolean,
    onCopy: () -> Unit
) {
    var showCopyFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            showCopyFeedback = true
            delay(1000L)
            showCopyFeedback = false
        }
    }

    val copyIconColor by animateColorAsState(
        targetValue = if (showCopyFeedback) Color(0xFF4CAF50) else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "pipCopyIconColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = reminder.title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onCopy,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copiar",
                tint = copyIconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
