package com.clipboardreminder.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clipboardreminder.domain.model.SortOrder
import com.clipboardreminder.ui.theme.GoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterSheet(
    sortOrder: SortOrder,
    onSortChange: (SortOrder) -> Unit,
    filterColor: Int?,
    onFilterColorChange: (Int?) -> Unit,
    onDismissRequest: () -> Unit,
    showMostUsedOption: Boolean = false
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                "Filtrar por Cor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            ColorPickerRow(
                selectedColor = filterColor,
                onColorSelected = onFilterColorChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            Text(
                "Ordenar por",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            SortOption(
                label = "A → Z",
                selected = sortOrder == SortOrder.ALPHABETICAL,
                onClick = { onSortChange(SortOrder.ALPHABETICAL) }
            )
            SortOption(
                label = "Z → A",
                selected = sortOrder == SortOrder.ALPHABETICAL_DESC,
                onClick = { onSortChange(SortOrder.ALPHABETICAL_DESC) }
            )
            SortOption(
                label = "Última Modificação",
                selected = sortOrder == SortOrder.LAST_MODIFIED,
                onClick = { onSortChange(SortOrder.LAST_MODIFIED) }
            )
            if (showMostUsedOption) {
                SortOption(
                    label = "Mais Utilizados",
                    selected = sortOrder == SortOrder.MOST_USED,
                    onClick = { onSortChange(SortOrder.MOST_USED) }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SortOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
