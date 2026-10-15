package com.wisekoin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wisekoin.R
import com.wisekoin.data.model.CategoryResponse
import com.wisekoin.ui.theme.BrandGreenDark
import com.wisekoin.ui.theme.CandyBg
import com.wisekoin.ui.theme.PrimaryRed
import com.wisekoin.ui.theme.TextDark

@Composable
internal fun CategoryManagementDialog(
    category: CategoryResponse,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember(category.id) { mutableStateOf(category.name) }
    var icon by remember(category.id) { mutableStateOf(category.icon ?: "category") }
    var confirmDelete by remember(category.id) { mutableStateOf(false) }
    val icons = listOf(
        "restaurant", "transport", "shopping", "coffee", "education",
        "health", "home", "work", "money", "savings"
    )

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(stringResource(R.string.manage_category), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.category_name)) },
                    singleLine = true,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.choose_icon), fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.forEach { option ->
                        val selected = option == icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) BrandGreenDark else CandyBg)
                                .clickable(enabled = !isSubmitting) { icon = option },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                getIconForCategory(option),
                                contentDescription = option,
                                tint = if (selected) Color.White else TextDark
                            )
                        }
                    }
                }
                errorMessage?.let { Text(it, color = PrimaryRed) }
                if (confirmDelete) {
                    Text(
                        stringResource(R.string.delete_category_confirmation, category.name),
                        color = PrimaryRed
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { if (confirmDelete) onDelete() else confirmDelete = true },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(
                            if (confirmDelete) R.string.confirm_delete else R.string.delete
                        )
                    )
                }
                Button(
                    onClick = { onSave(name.trim(), icon) },
                    enabled = !isSubmitting && name.isNotBlank()
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.save_changes))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
