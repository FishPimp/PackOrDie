package se.packmaster.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.packmaster.app.R

/** Utvalda emojis som passar som ikoner för objekt, kategorier och väskor. */
val PickerEmojis = listOf(
    "🧳", "🎒", "👜", "💼", "🛍️", "🏋️", "🏊", "🏖️", "🏡", "✈️", "🚗", "⛺", "🏕️", "⛷️", "🚲",
    "👕", "👖", "🧥", "🧦", "🩲", "🩱", "👗", "👟", "🥾", "🧢", "🧤", "🧣", "🕶️", "🛌", "🌙",
    "🧴", "🪥", "🧼", "🪒", "💇", "🧻", "💊", "🩹", "🌡️", "💉",
    "🔌", "🔋", "📱", "💻", "🎧", "📷", "⌚", "🎮", "📚", "✏️",
    "🍼", "🧷", "🧸", "👶", "🧒", "🧑", "👩", "👨", "👵", "👴", "👨‍👩‍👧", "🐶", "🐱",
    "📄", "🛂", "🪪", "🎫", "💳", "👛", "🔑", "🔒", "📋",
    "🍎", "🍪", "🥪", "🍱", "☕", "🚰", "🥤",
    "☂️", "☀️", "❄️", "⭐", "❤️", "🎉", "🌈", "🦆", "⚽", "🎾", "🧩", "🎨", "🎸", "🌸",
)

@Composable
fun EmojiPickerDialog(
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var custom by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.emoji_picker_title)) },
        text = {
            Column {
                // Vanligt rutnät (inte lazy) eftersom dialoger mäter sitt innehåll.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    PickerEmojis.chunked(6).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { emoji ->
                                Surface(
                                    shape = CircleShape,
                                    color = if (emoji == current) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { onPick(emoji); onDismiss() },
                                ) {
                                    Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 22.sp) }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = custom,
                    onValueChange = { custom = it.take(8) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.emoji_picker_custom)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = custom.isNotBlank(),
                onClick = { onPick(custom.trim()); onDismiss() },
            ) { Text(stringResource(R.string.action_use)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

/** Klickbar emoji-knapp som öppnar väljaren. */
@Composable
fun EmojiButton(emoji: String, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .height(56.dp)
            .aspectRatio(1f)
            .clickable { open = true },
    ) {
        Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 28.sp) }
    }
    if (open) EmojiPickerDialog(current = emoji, onPick = onPick, onDismiss = { open = false })
}
