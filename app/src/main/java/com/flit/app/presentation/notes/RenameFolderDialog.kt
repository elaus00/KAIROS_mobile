package com.flit.app.presentation.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.flit.app.ui.theme.FlitTheme

/**
 * 폴더 이름 변경 다이얼로그
 */
@Composable
fun RenameFolderDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    val colors = FlitTheme.colors
    var newName by remember { mutableStateOf(currentName) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("폴더 이름 변경", color = colors.text) },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { if (it.length <= 30) newName = it },
                label = { Text("새 이름") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.accent,
                    unfocusedLabelColor = colors.textMuted,
                    cursorColor = colors.accent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onRename(newName) },
                enabled = newName.isNotBlank() && newName != currentName,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.accent
                )
            ) {
                Text("변경")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.textMuted
                )
            ) {
                Text("취소")
            }
        },
        containerColor = colors.card,
        textContentColor = colors.text
    )
}
