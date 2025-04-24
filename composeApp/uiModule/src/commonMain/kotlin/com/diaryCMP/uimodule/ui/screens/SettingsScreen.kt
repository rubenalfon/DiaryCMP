package com.diaryCMP.uimodule.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.diaryCMP.uimodule.ui.composables.GenericAlertErrorSaving
import com.diaryCMP.uimodule.ui.composables.GenericDialog
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.composables.ResponsiveScreenHeader
import com.diaryCMP.uimodule.ui.theme.iconSize
import com.diaryCMP.uimodule.ui.theme.iconTextSeparation
import com.diaryCMP.uimodule.ui.theme.interiorCardPadding
import com.diaryCMP.uimodule.ui.theme.largePadding
import com.diaryCMP.uimodule.ui.theme.listSpacedByPadding
import com.diaryCMP.uimodule.ui.theme.mediumPadding
import com.diaryCMP.uimodule.ui.theme.noPadding
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.surfaceCornerRadius
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import com.diaryCMP.uimodule.ui.theme.widthGridCells
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.accept
import diary.composeapp.utilsmodule.generated.resources.account
import diary.composeapp.utilsmodule.generated.resources.are_you_sure
import diary.composeapp.utilsmodule.generated.resources.cancel
import diary.composeapp.utilsmodule.generated.resources.change_name
import diary.composeapp.utilsmodule.generated.resources.change_password
import diary.composeapp.utilsmodule.generated.resources.diary
import diary.composeapp.utilsmodule.generated.resources.email
import diary.composeapp.utilsmodule.generated.resources.end_day_hour
import diary.composeapp.utilsmodule.generated.resources.end_day_hour_body
import diary.composeapp.utilsmodule.generated.resources.end_day_hour_quick
import diary.composeapp.utilsmodule.generated.resources.force_sync
import diary.composeapp.utilsmodule.generated.resources.force_sync_body
import diary.composeapp.utilsmodule.generated.resources.force_sync_quick
import diary.composeapp.utilsmodule.generated.resources.logout
import diary.composeapp.utilsmodule.generated.resources.logout_alert_message
import diary.composeapp.utilsmodule.generated.resources.name
import diary.composeapp.utilsmodule.generated.resources.notifications
import diary.composeapp.utilsmodule.generated.resources.notifications_not_available
import diary.composeapp.utilsmodule.generated.resources.receive_notifications
import diary.composeapp.utilsmodule.generated.resources.return_
import diary.composeapp.utilsmodule.generated.resources.select_hour_of_notification
import diary.composeapp.utilsmodule.generated.resources.settings
import diary.composeapp.utilsmodule.generated.resources.sync_error
import diary.composeapp.utilsmodule.generated.resources.sync_in_progress
import es.diaryCMP.utilsModule.utils.KeyboardAware
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.TextFieldValueSaver
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.utilsModule.utils.keyboardDismissOnSwipeIOS
import es.diaryCMP.viewmodelsModule.viewmodels.SettingsScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsScreenViewModel,
    navigateToChangePassword: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.fakeInit()
    }
    val isLoading by viewModel.isLoading.collectAsState()

    val errorSaving by viewModel.errorSaving.collectAsState()
    val errorSync by viewModel.errorSync.collectAsState()
    val errorMessage = viewModel.errorMessage

    val canReceiveNotifications by viewModel.canReceiveNotifications.collectAsState()
    val receiveNotifications by viewModel.receiveNotifications.collectAsState()
    val notificationsTime by viewModel.notificationsTime.collectAsState()

    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()

    val endDayHour by viewModel.endDayHour.collectAsState()
    val isSyncInProgress by viewModel.isSyncInProgress.collectAsState()

    val lazyListState = rememberLazyStaggeredGridState()

    Column(modifier = modifier) {
        val animatedColor by animateColorAsState(if (lazyListState.firstVisibleItemScrollOffset > 5) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer)

        SettingsScreenHeader(
            containerColor = if (getScreenClass() == ScreenClass.Compact) {
                animatedColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
        )

        LoadingWrapper(isLoading = isLoading, content = {
            KeyboardAware(content = {
                SettingsScreenContent(
                    modifier = if (getScreenClass() != ScreenClass.Compact) {
                        Modifier
                    } else {
                        Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    },
                    lazyListState = lazyListState,

                    canReceiveNotifications = canReceiveNotifications,
                    receiveNotifications = receiveNotifications,
                    notificationsTime = notificationsTime,
                    onToggleReceiveNotifications = {
                        viewModel.toggleReceiveNotifications()
                    },
                    onSaveNotificationsTime = { notificationsTime ->
                        viewModel.updateNotificationsTime(notificationsTime)
                    },

                    endDayHour = endDayHour,
                    onChangeEndDayHour = { endDayHour ->
                        viewModel.onChangeEndDayHour(endDayHour)
                    },
                    isSyncInProgress = isSyncInProgress,
                    errorSync = errorSync,
                    onForceSync = {
                        viewModel.forceSync()
                    },

                    userName = currentUserName ?: "-",
                    userEmail = currentUserEmail ?: "-",
                    onChangeUserName = {
                        viewModel.changeUserName(it)
                    },
                    onChangePasswordButtonTapped = {
                        navigateToChangePassword()
                    },
                    onLogout = {
                        viewModel.logout()
                    }
                )
            })
        })
    }

    if (errorSaving) {
        GenericAlertErrorSaving(text = errorMessage, onDismiss = { viewModel.dismissErrorSaving() })
    }

    if (errorSync) {
        GenericDialog(
            title = stringResource(Res.string.sync_error),
            body = errorMessage,
            onDismiss = { viewModel.dismissErrorSync() },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissErrorSync()
                }) {
                    Text(stringResource(Res.string.return_))
                }
            },
        )
    }
}

@Composable
private fun SettingsScreenHeader(modifier: Modifier = Modifier, containerColor: Color) {
    ResponsiveScreenHeader(
        title = stringResource(Res.string.settings),
        modifier = modifier,
        containerColor = containerColor
    )
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    lazyListState: LazyStaggeredGridState,

    canReceiveNotifications: Boolean,
    receiveNotifications: Boolean,
    notificationsTime: LocalTime,
    onToggleReceiveNotifications: () -> Unit,
    onSaveNotificationsTime: (LocalTime) -> Unit,

    endDayHour: LocalTime,
    onChangeEndDayHour: (LocalTime) -> Unit,
    isSyncInProgress: Boolean,
    errorSync: Boolean,
    onForceSync: () -> Unit,

    userName: String,
    userEmail: String,
    onChangeUserName: (String) -> Unit,
    onChangePasswordButtonTapped: () -> Unit,
    onLogout: () -> Unit
) {
    val numberOfCards = 3
    val keyboardController = LocalSoftwareKeyboardController.current

    val columns =
        if (getScreenClass() != ScreenClass.Compact) StaggeredGridCells.Adaptive(widthGridCells)
        else StaggeredGridCells.Fixed(1)

    LazyVerticalStaggeredGrid(
        columns = columns,
        modifier = modifier.fillMaxWidth()
            .keyboardDismissOnSwipeIOS { keyboardController?.hide() },
        state = lazyListState,
        verticalItemSpacing = surfaceToWindowPadding,
        horizontalArrangement = Arrangement.spacedBy(surfaceToWindowPadding)
    ) {
        items(count = numberOfCards) { index ->
            Column {
                Cards(
                    index = index,

                    canReceiveNotifications = canReceiveNotifications,
                    receiveNotifications = receiveNotifications,
                    notificationsTime = notificationsTime,
                    onToggleReceiveNotifications = onToggleReceiveNotifications,
                    onSaveNotificationsTime = onSaveNotificationsTime,

                    endDayHour = endDayHour,
                    onChangeEndDayHour = onChangeEndDayHour,
                    isSyncInProgress = isSyncInProgress,
                    errorSync = errorSync,
                    onForceSync = onForceSync,

                    userName = userName,
                    userEmail = userEmail,
                    onChangeUserName = onChangeUserName,
                    onChangePasswordButtonTapped = onChangePasswordButtonTapped,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun Cards(
    index: Int,
    modifier: Modifier = Modifier,

    canReceiveNotifications: Boolean,
    receiveNotifications: Boolean,
    notificationsTime: LocalTime,
    onToggleReceiveNotifications: () -> Unit,
    onSaveNotificationsTime: (LocalTime) -> Unit,

    endDayHour: LocalTime,
    onChangeEndDayHour: (LocalTime) -> Unit,
    isSyncInProgress: Boolean,
    errorSync: Boolean,
    onForceSync: () -> Unit,

    userName: String,
    userEmail: String,
    onChangeUserName: (String) -> Unit,
    onChangePasswordButtonTapped: () -> Unit,
    onLogout: () -> Unit
) {
    when (index) {
        0 -> {
            NotificationCard(
                canReceiveNotifications = canReceiveNotifications,
                receiveNotifications = receiveNotifications,
                notificationsTime = notificationsTime,
                onReceiveNotificationsChanged = onToggleReceiveNotifications,
                onSaveNotificationsTime = onSaveNotificationsTime,
                modifier = modifier
            )
        }

        1 -> {
            DiaryCard(
                endDayHour = endDayHour,
                onChangeEndDayHour = onChangeEndDayHour,
                isSyncInProgressAlert = isSyncInProgress,
                errorSync = errorSync,
                onForceSync = onForceSync,
                modifier = modifier
            )
        }

        2 -> {
            AccountCard(
                userName = userName,
                userEmail = userEmail,
                onChangeUserName = onChangeUserName,
                onChangePasswordButtonTapped = onChangePasswordButtonTapped,
                onLogout = onLogout,
                modifier = modifier

            )
        }
    }
}

@Composable
private fun GenericSettingsCard(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(surfaceCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(interiorCardPadding)
            .padding(horizontal = smallPadding),
        verticalArrangement = Arrangement.spacedBy(largePadding)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box {
            content.invoke()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, FormatStringsInDatetimeFormats::class)
@Composable
private fun NotificationCard(
    canReceiveNotifications: Boolean,
    receiveNotifications: Boolean,
    notificationsTime: LocalTime,
    onReceiveNotificationsChanged: () -> Unit,
    onSaveNotificationsTime: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    GenericSettingsCard(modifier = modifier, title = stringResource(Res.string.notifications)) {
        Column(
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(largePadding)
        ) {

            Row(
                Modifier.fillMaxWidth().padding(start = mediumPadding)
                    .alpha(if (canReceiveNotifications) 1f else 0.38f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Notifications, contentDescription = null)
                Spacer(modifier = Modifier.width(iconTextSeparation))
                Text(
                    stringResource(Res.string.receive_notifications),
                    color = if (canReceiveNotifications) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    )
                )

                Spacer(Modifier.weight(1f))
                Switch(
                    checked = receiveNotifications,
                    onCheckedChange = { onReceiveNotificationsChanged.invoke() },
                    enabled = canReceiveNotifications,
                    modifier = Modifier.padding(end = mediumPadding)
                )
            }

            if (!canReceiveNotifications) {
                Row(
                    Modifier.padding(bottom = smallPadding).clip(RoundedCornerShape(smallPadding))
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.padding(start = mediumPadding)
                    )
                    Spacer(modifier = Modifier.width(iconTextSeparation))
                    Text(
                        text = stringResource(Res.string.notifications_not_available),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = smallPadding)
                            .padding(end = mediumPadding)
                    )
                }
                return@Column
            }

            if (!receiveNotifications) return@Column

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = mediumPadding)
            ) {
                var showTimePicker by rememberSaveable { mutableStateOf(false) }
                Icon(imageVector = Icons.Filled.Schedule, contentDescription = null)
                Spacer(modifier = Modifier.width(iconTextSeparation))
                Text(
                    text = stringResource(Res.string.select_hour_of_notification),
                    modifier = Modifier
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { showTimePicker = true }) {
                    Text(
                        "${notificationsTime.format(LocalTime.Format { byUnicodePattern("HH:mm") })} h",
                        modifier = Modifier
                    )
                }

                if (!showTimePicker) return@Row

                val timeInputState = rememberTimePickerState(
                    initialHour = notificationsTime.hour,
                    initialMinute = notificationsTime.minute
                )
                AlertDialog(modifier = modifier,
                    onDismissRequest = { showTimePicker = false },
                    title = { Text(stringResource(Res.string.select_hour_of_notification)) },
                    text = {
                        TimeInput(state = timeInputState)
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showTimePicker = false
                            onSaveNotificationsTime.invoke(
                                LocalTime(
                                    timeInputState.hour,
                                    timeInputState.minute
                                )
                            )
                        }) {
                            Text(stringResource(Res.string.accept))
                        }
                    }, dismissButton = {
                        TextButton(onClick = {
                            showTimePicker = false
                        }) {
                            Text(stringResource(Res.string.cancel))
                        }
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, FormatStringsInDatetimeFormats::class)
@Composable
private fun DiaryCard(
    endDayHour: LocalTime,
    onChangeEndDayHour: (LocalTime) -> Unit,
    isSyncInProgressAlert: Boolean,
    errorSync: Boolean,
    onForceSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    GenericSettingsCard(
        modifier = modifier,
        title = stringResource(Res.string.diary)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(largePadding)) {
            Column {
                Row( // End day hour
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = mediumPadding)
                ) {
                    Icon(imageVector = Icons.Filled.Bedtime, contentDescription = null)
                    Spacer(modifier = Modifier.width(iconTextSeparation))
                    var showTimePicker by rememberSaveable { mutableStateOf(false) }
                    Text(
                        stringResource(Res.string.end_day_hour),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showTimePicker = true }) {
                        Text(
                            "${endDayHour.format(LocalTime.Format { byUnicodePattern("HH:mm") })} h",
                            modifier = Modifier
                        )
                    }

                    if (showTimePicker) {
                        val timeInputState = rememberTimePickerState(
                            initialHour = endDayHour.hour,
                            initialMinute = endDayHour.minute
                        )
                        AlertDialog(modifier = modifier,
                            onDismissRequest = { showTimePicker = false },
                            title = { Text(stringResource(Res.string.end_day_hour)) },
                            text = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(
                                        listSpacedByPadding
                                    )
                                ) {
                                    Text(stringResource(Res.string.end_day_hour_body))
                                    TimeInput(state = timeInputState)
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showTimePicker = false
                                    onChangeEndDayHour.invoke(
                                        LocalTime(
                                            timeInputState.hour,
                                            timeInputState.minute
                                        )
                                    )
                                }) {
                                    Text(stringResource(Res.string.accept))
                                }
                            }, dismissButton = {
                                TextButton(onClick = {
                                    showTimePicker = false
                                }) {
                                    Text(stringResource(Res.string.cancel))
                                }
                            }
                        )
                    }
                }
                Row(Modifier.padding(start = mediumPadding)) {
                    Spacer(modifier = Modifier.width(iconSize))
                    Spacer(modifier = Modifier.width(iconTextSeparation))

                    Text(
                        stringResource(Res.string.end_day_hour_quick),
                        modifier = Modifier.padding(end = mediumPadding),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            var showForceSyncAlert by rememberSaveable { mutableStateOf(false) }
            var isSyncCompleted by remember { mutableStateOf(false) }
            var isPageStarting by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                isPageStarting = true
                delay(4_000)
                isPageStarting = false
            }

            LaunchedEffect(isSyncInProgressAlert) {
                if (!isPageStarting && !isSyncInProgressAlert && !errorSync) {
                    isSyncCompleted = true
                    delay(4_000)
                    isSyncCompleted = false
                }
            }

            TextButton(
                shape = RoundedCornerShape(smallPadding),
                contentPadding = PaddingValues(
                    horizontal = mediumPadding,
                    vertical = mediumPadding
                ),
                onClick = { showForceSyncAlert = true },
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = if (getScreenClass() != ScreenClass.Compact) smallPadding else noPadding)
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row( // End day hour
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Crossfade(targetState = isSyncCompleted) { targetState ->
                            when (targetState) {
                                true -> {
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = null
                                    )
                                }

                                false -> {
                                    Icon(
                                        imageVector = Icons.Filled.Sync,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(iconTextSeparation))
                        Text(stringResource(Res.string.force_sync))
                    }
                    Row(Modifier) {
                        Spacer(modifier = Modifier.width(iconSize))
                        Spacer(modifier = Modifier.width(iconTextSeparation))

                        Text(
                            stringResource(Res.string.force_sync_quick),
                            modifier = Modifier.padding(end = mediumPadding),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (showForceSyncAlert) {
                GenericDialog(
                    title = stringResource(Res.string.force_sync),
                    body = stringResource(Res.string.force_sync_body),
                    onConfirm = {
                        onForceSync()
                        showForceSyncAlert = false
                    }, onDismiss = {
                        showForceSyncAlert = false
                    }
                )
            }

            if (isSyncInProgressAlert) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(stringResource(Res.string.sync_in_progress)) },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(smallPadding)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    confirmButton = {},
                    dismissButton = {}
                )
            }
        }
    }
}


@Composable
private fun AccountCard(
    userName: String,
    userEmail: String,
    modifier: Modifier = Modifier,
    onChangeUserName: (String) -> Unit,
    onChangePasswordButtonTapped: () -> Unit,
    onLogout: () -> Unit
) {
    GenericSettingsCard(modifier = modifier, title = stringResource(Res.string.account)) {
        Column(verticalArrangement = Arrangement.spacedBy(largePadding)) {
            var showAlertChangeUserName by rememberSaveable { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(smallPadding))
                    .clickable { showAlertChangeUserName = true }
                    .padding(start = mediumPadding)
                    .padding(vertical = mediumPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(iconTextSeparation))

                Column {
                    Text(text = stringResource(Res.string.name))

                    Text(
                        text = userName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = stringResource(Res.string.change_name),
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            Row(
                modifier = Modifier.padding(horizontal = mediumPadding, vertical = mediumPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.AlternateEmail, contentDescription = null)
                Spacer(modifier = Modifier.width(iconTextSeparation))

                Column(Modifier.padding(end = mediumPadding)) {
                    Text(
                        stringResource(Res.string.email),
                        modifier = Modifier
                    )

                    Text(
                        userEmail,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            TextButton(contentPadding = PaddingValues(
                horizontal = mediumPadding,
                vertical = mediumPadding
            ),
                onClick = { onChangePasswordButtonTapped() }
            ) {
                Icon(imageVector = Icons.Filled.Key, contentDescription = null)
                Spacer(modifier = Modifier.width(iconTextSeparation))
                Text(stringResource(Res.string.change_password))
            }

            var showLogoutAlert by rememberSaveable { mutableStateOf(false) }
            TextButton(contentPadding = PaddingValues(
                horizontal = mediumPadding,
                vertical = mediumPadding
            ),
                onClick = { showLogoutAlert = true }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(iconTextSeparation))
                Text(stringResource(Res.string.logout), color = MaterialTheme.colorScheme.error)
            }

            if (showAlertChangeUserName) {
                ChangeUserNameAlert(
                    modifier = modifier,
                    oldUserName = userName,
                    onDismissRequest = { showAlertChangeUserName = false },
                    onChangeUserName = onChangeUserName
                )
            }

            if (showLogoutAlert) {
                GenericDialog(
                    title = stringResource(Res.string.are_you_sure),
                    body = stringResource(Res.string.logout_alert_message),
                    onConfirm = { onLogout.invoke() },
                    onDismiss = {
                        showLogoutAlert = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ChangeUserNameAlert(
    modifier: Modifier,
    oldUserName: String,
    onDismissRequest: () -> Unit,
    onChangeUserName: (String) -> Unit
) {
    var newUserName by rememberSaveable(stateSaver = TextFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(oldUserName)
        )
    }
    AlertDialog(modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.change_name)) },
        text = {

            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                newUserName = newUserName.copy(
                    selection = TextRange(
                        0,
                        newUserName.text.length
                    )
                )
            }

            TextField(
                value = newUserName,
                onValueChange = { newUserName = it },
                label = { Text(stringResource(Res.string.name)) },
                singleLine = true,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()
                    .onPreviewKeyEvent {
                        if (it.key == Key.Enter || it.key == Key.NumPadEnter && it.type == KeyEventType.KeyUp) {
                            onDismissRequest.invoke()
                            onChangeUserName.invoke(newUserName.text)
                            true
                        } else false
                    },
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest.invoke()
                onChangeUserName.invoke(newUserName.text)
            }) {
                Text(stringResource(Res.string.accept))
            }
        }, dismissButton = {
            TextButton(onClick = {
                onDismissRequest.invoke()
            }) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}