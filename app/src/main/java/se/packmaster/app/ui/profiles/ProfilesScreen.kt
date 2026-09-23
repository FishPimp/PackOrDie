package se.packmaster.app.ui.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import se.packmaster.app.R
import se.packmaster.app.data.prefs.ThemeMode
import se.packmaster.app.ui.components.ConfirmDialog
import se.packmaster.app.ui.components.EmptyState
import se.packmaster.app.ui.components.ItemAvatar
import se.packmaster.app.ui.components.NameEmojiDialog
import se.packmaster.domain.model.Profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(viewModel: ProfilesViewModel = viewModel(factory = ProfilesViewModel.Factory)) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Profile?>(null) }
    var toDelete by remember { mutableStateOf<Profile?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.profiles_title)) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = Profile(name = "", emoji = "🧑") },
                icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                text = { Text(stringResource(R.string.profiles_new)) },
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.profiles_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val list = profiles
            if (list != null && list.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "👥",
                        title = stringResource(R.string.profiles_empty_title),
                        message = stringResource(R.string.profiles_empty_message),
                    )
                }
            }
            items(list.orEmpty(), key = { it.id }) { profile ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editing = profile },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp),
                    ) {
                        ItemAvatar(
                            emoji = profile.emoji,
                            size = 48.dp,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(profile.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { toDelete = profile }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                }
            }

            item {
                Column(Modifier.padding(top = 24.dp)) {
                    Text(stringResource(R.string.settings_appearance), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.padding(4.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                            ) {
                                Text(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                        ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { profile ->
        NameEmojiDialog(
            title = stringResource(if (profile.id == 0L) R.string.profiles_new else R.string.profiles_edit),
            initialName = profile.name,
            initialEmoji = profile.emoji,
            onConfirm = { name, emoji -> viewModel.saveProfile(profile.copy(name = name, emoji = emoji)) },
            onDismiss = { editing = null },
        )
    }

    toDelete?.let { profile ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_profile_title),
            message = stringResource(R.string.confirm_delete_profile_message, profile.name),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = { viewModel.deleteProfile(profile) },
            onDismiss = { toDelete = null },
        )
    }
}
