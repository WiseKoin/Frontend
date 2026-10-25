package com.wisekoin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisekoin.R
import com.wisekoin.data.model.FeedbackCategory
import com.wisekoin.ui.theme.BrandGreenDark
import com.wisekoin.ui.theme.CandyBg
import com.wisekoin.ui.theme.LemonDark
import com.wisekoin.ui.theme.MintDark
import com.wisekoin.ui.theme.PrimaryRed
import com.wisekoin.ui.theme.TextDark
import com.wisekoin.ui.theme.TextLight
import com.wisekoin.ui.theme.neomorphicBox
import com.wisekoin.viewmodel.FeedbackSubmissionState
import com.wisekoin.viewmodel.FeedbackValidationError
import com.wisekoin.viewmodel.FeedbackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    viewModel: FeedbackViewModel,
    defaultName: String?,
    defaultEmail: String?,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(defaultName, defaultEmail) {
        viewModel.prefillContact(defaultName, defaultEmail)
    }

    Scaffold(
        containerColor = CandyBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.help_center),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CandyBg)
                .padding(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { FeedbackHero() }

            if (state.submissionState == FeedbackSubmissionState.SENT) {
                item {
                    FeedbackSuccessCard(onSendAnother = viewModel::sendAnother)
                }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.feedback_topic),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FeedbackCategory.entries.forEach { category ->
                                FeedbackCategoryChip(
                                    category = category,
                                    selected = state.category == category,
                                    enabled = state.submissionState !=
                                        FeedbackSubmissionState.SUBMITTING,
                                    onClick = { viewModel.selectCategory(category) }
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neomorphicBox(cornerRadius = 28.dp, elevation = 4.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.82f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.tell_us_more),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = stringResource(R.string.feedback_form_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextLight
                            )

                            val messageError = when (state.validationError) {
                                FeedbackValidationError.MESSAGE_REQUIRED ->
                                    stringResource(R.string.feedback_message_required)
                                FeedbackValidationError.MESSAGE_TOO_SHORT ->
                                    stringResource(
                                        R.string.feedback_message_too_short,
                                        FeedbackViewModel.MIN_MESSAGE_LENGTH
                                    )
                                else -> null
                            }
                            OutlinedTextField(
                                value = state.message,
                                onValueChange = viewModel::updateMessage,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.feedback_message)) },
                                placeholder = {
                                    Text(stringResource(R.string.feedback_message_placeholder))
                                },
                                minLines = 5,
                                maxLines = 8,
                                enabled = state.submissionState != FeedbackSubmissionState.SUBMITTING,
                                isError = messageError != null,
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(messageError.orEmpty())
                                        Text(
                                            stringResource(
                                                R.string.feedback_character_count,
                                                state.message.length,
                                                FeedbackViewModel.MAX_MESSAGE_LENGTH
                                            )
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences
                                )
                            )

                            OutlinedTextField(
                                value = state.name,
                                onValueChange = viewModel::updateName,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.feedback_name_optional)) },
                                singleLine = true,
                                enabled = state.submissionState != FeedbackSubmissionState.SUBMITTING,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )

                            OutlinedTextField(
                                value = state.email,
                                onValueChange = viewModel::updateEmail,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.feedback_email_optional)) },
                                singleLine = true,
                                enabled = state.submissionState != FeedbackSubmissionState.SUBMITTING,
                                isError = state.validationError == FeedbackValidationError.INVALID_EMAIL,
                                supportingText = {
                                    if (state.validationError == FeedbackValidationError.INVALID_EMAIL) {
                                        Text(stringResource(R.string.invalid_email))
                                    } else {
                                        Text(stringResource(R.string.feedback_email_supporting_text))
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        viewModel.submit()
                                    }
                                )
                            )

                            AnimatedVisibility(
                                visible = state.submissionState == FeedbackSubmissionState.ERROR
                            ) {
                                Text(
                                    text = stringResource(R.string.feedback_submission_failed),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.submit()
                                },
                                enabled = state.submissionState != FeedbackSubmissionState.SUBMITTING,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                if (state.submissionState == FeedbackSubmissionState.SUBMITTING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.size(10.dp))
                                    Text(
                                        text = stringResource(R.string.send_feedback),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { FeedbackPrivacyCard() }
        }
    }
}

@Composable
private fun FeedbackHero() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = BrandGreenDark)
    ) {
        Row(
            modifier = Modifier.padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(MintDark.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.feedback_hero_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.feedback_hero_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun FeedbackCategoryChip(
    category: FeedbackCategory,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val label: String
    val icon: ImageVector
    val accent: Color
    when (category) {
        FeedbackCategory.BUG -> {
            label = stringResource(R.string.feedback_category_bug)
            icon = Icons.Default.BugReport
            accent = PrimaryRed
        }
        FeedbackCategory.IDEA -> {
            label = stringResource(R.string.feedback_category_idea)
            icon = Icons.Default.Lightbulb
            accent = LemonDark
        }
        FeedbackCategory.QUESTION -> {
            label = stringResource(R.string.feedback_category_question)
            icon = Icons.AutoMirrored.Filled.HelpOutline
            accent = BrandGreenDark
        }
    }

    FilterChip(
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accent.copy(alpha = 0.16f),
            selectedLabelColor = TextDark,
            selectedLeadingIconColor = accent
        )
    )
}

@Composable
private fun FeedbackSuccessCard(onSendAnother: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MintDark.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = BrandGreenDark,
                modifier = Modifier.size(54.dp)
            )
            Text(
                text = stringResource(R.string.feedback_thank_you),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = stringResource(R.string.feedback_submitted_description),
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight
            )
            Button(
                onClick = onSendAnother,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.send_another_feedback))
            }
        }
    }
}

@Composable
private fun FeedbackPrivacyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.62f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = BrandGreenDark
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.feedback_privacy_title),
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = stringResource(R.string.feedback_privacy_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
            }
        }
    }
}
