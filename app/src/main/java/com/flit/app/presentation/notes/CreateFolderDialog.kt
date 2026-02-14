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
 * 폴더 생성 다이얼로그
 * 폴더 이름 입력 + 생성/취소 버튼
 */
@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    val colors = FlitTheme.colors
    var folderName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 폴더", color = colors.text) },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { if (it.length <= 30) folderName = it },
                label = { Text("폴더 이름") },
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
                onClick = { onCreate(folderName) },
                enabled = folderName.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.accent
                )
            ) {
                Text("생성")
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
