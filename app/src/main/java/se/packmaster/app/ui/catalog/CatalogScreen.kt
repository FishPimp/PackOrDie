package se.packmaster.app.ui.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import se.packmaster.app.R
import se.packmaster.app.ui.components.EmptyState
import se.packmaster.app.ui.components.ItemAvatar
import se.packmaster.app.ui.components.ItemEditorDialog
import se.packmaster.app.ui.components.ItemFields
import se.packmaster.app.ui.components.formatWeight
import se.packmaster.app.ui.components.ruleDescription
import se.packmaster.app.ui.components.toFields
import se.packmaster.app.ui.components.withFields
import se.packmaster.domain.model.CatalogItem
import se.packmaster.domain.model.Profile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CatalogScreen(viewModel: CatalogViewModel = viewModel(factory = CatalogViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Ett nytt objekt representeras av ett CatalogItem med id 0.
    var editing by remember { mutableStateOf<CatalogItem?>(null) }
    var showCategories by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.catalog_title))
                        Text(
                            stringResource(R.string.catalog_subtitle, state.totalItems),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCategories = true }) {
                        Icon(Icons.Filled.Category, contentDescription = stringResource(R.string.categories_title))
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editing = CatalogItem(
                        name = "",
                        categoryId = state.selectedCategoryId,
                        profileId = null,
                        emoji = "📦",
                    )
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.catalog_new_item)) },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                },
                placeholder = { Text(stringResource(R.string.search_catalog)) },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategoryId == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text(stringResource(R.string.filter_all_categories)) },
                    )
                }
                items(state.categories, key = { it.id }) { category ->
                    FilterChip(
                        selected = state.selectedCategoryId == category.id,
                        onClick = { viewModel.selectCategory(category.id) },
                        label = { Text("${category.emoji} ${category.name}") },
                    )
                }
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                state.sections.isEmpty() -> EmptyState(
                    emoji = "🔍",
                    title = stringResource(R.string.catalog_empty_title),
                    message = stringResource(R.string.catalog_empty_message),
                )

                else -> {
                    val profilesById = state.profiles.associateBy { it.id }
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        state.sections.forEach { section ->
                            stickyHeader(key = "section_${section.category?.id}") {
                                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        section.category?.let { "${it.emoji}  ${it.name}" }
                                            ?: stringResource(R.string.none_category),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
                                    )
                                }
                            }
                            items(section.items, key = { it.id }) { item ->
                                CatalogRow(
                                    item = item,
                                    profile = item.profileId?.let(profilesById::get),
                                    onClick = { editing = item },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { item ->
        val isNew = item.id == 0L
        ItemEditorDialog(
            title = stringResource(if (isNew) R.string.editor_new_item else R.string.editor_edit_item),
            initial = if (isNew) ItemFields(categoryId = item.categoryId) else item.toFields(),
            categories = state.categories,
            profiles = state.profiles,
            onImportImage = viewModel::importImage,
            onCreateCategory = viewModel::createCategory,
            onSave = { fields ->
                viewModel.saveItem(item.withFields(fields))
                editing = null
            },
            onDelete = if (isNew) {
                null
            } else {
                fun() {
                    viewModel.deleteItem(item)
                    editing = null
                }
            },
            onDismiss = { editing = null },
        )
    }

    if (showCategories) {
        CategoriesDialog(
            categories = state.categories,
            onSave = viewModel::saveCategory,
            onDelete = viewModel::deleteCategory,
            onDismiss = { showCategories = false },
        )
    }
}

@Composable
private fun CatalogRow(item: CatalogItem, profile: Profile?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            ItemAvatar(emoji = item.emoji, imagePath = item.imagePath)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val details = buildList {
                    add(ruleDescription(item.rule, item.amount))
                    if (profile != null) add("${profile.emoji} ${profile.name}")
                    if (item.weightGrams > 0) add(formatWeight(item.weightGrams))
                    if (item.isCustom) add(stringResource(R.string.catalog_custom_badge))
                }
                Text(
                    details.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
