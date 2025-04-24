package com.diaryCMP.uimodule.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.diaryCMP.uimodule.ui.theme.extraSmallPadding
import com.diaryCMP.uimodule.ui.theme.largePadding
import com.diaryCMP.uimodule.ui.theme.noPadding
import com.diaryCMP.uimodule.ui.theme.pageListHorizontalPadding
import com.diaryCMP.uimodule.ui.theme.permanentDrawerItemHeight
import com.diaryCMP.uimodule.ui.theme.permanentDrawerSheet
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.diary_cmp
import es.diaryCMP.modelsModule.models.NavigationItem
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.utilsModule.utils.keyboardAsState
import es.diaryCMP.viewmodelsModule.viewmodels.GlobalViewModel
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun Navigation(
    globalViewModel: GlobalViewModel = koinViewModel<GlobalViewModel>(),
    items: List<NavigationItem>,
    navigator: Navigator,
    containerColor: Color,
    content: @Composable (PaddingValues) -> Unit
) {
    val density = LocalDensity.current.density
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    val screenClass = getScreenClass()

    val enterAnimationDuration = 150
    val exitAnimationDuration = 100

    AnimatedVisibility(
        screenClass == ScreenClass.Compact,
        enter = slideInVertically(animationSpec = tween(durationMillis = enterAnimationDuration)) + expandVertically(
            animationSpec = tween(durationMillis = enterAnimationDuration),
            expandFrom = Alignment.Top
        ),
        exit = slideOutVertically(animationSpec = tween(durationMillis = exitAnimationDuration)) + shrinkVertically(
            animationSpec = tween(durationMillis = exitAnimationDuration)
        ) + fadeOut(animationSpec = tween(durationMillis = exitAnimationDuration))
    ) {
        val isKeyboardOpen by keyboardAsState()

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AnimatedVisibility(
                    visible = !isKeyboardOpen,
                    enter = slideInVertically(animationSpec = tween(durationMillis = enterAnimationDuration)) + expandVertically(
                        animationSpec = tween(durationMillis = enterAnimationDuration),
                        expandFrom = Alignment.Top
                    ),
                    exit = slideOutVertically(animationSpec = tween(durationMillis = exitAnimationDuration)) + shrinkVertically(
                        animationSpec = tween(durationMillis = exitAnimationDuration)
                    ) + fadeOut(animationSpec = tween(durationMillis = exitAnimationDuration))
                ) {
                    NavigationBarImpl(
                        items = items,
                        selectedItemIndex = selectedItemIndex,
                        onSelectedItemChange = { index ->
                            selectedItemIndex = index
                        },
                        navigator = navigator,
                        containerColor = containerColor,
                        modifier = Modifier.onGloballyPositioned {
                            globalViewModel.updateNavigationComposableSize(
                                DpSize(
                                    width = Dp(it.size.width / density),
                                    height = Dp(it.size.height / density)
                                )
                            )
                        }
                    )
                }
            }) { paddingValues ->
            content(paddingValues)
        }
    }
    AnimatedVisibility(
        screenClass == ScreenClass.Medium,
        enter = slideInHorizontally(animationSpec = tween(durationMillis = enterAnimationDuration)) + expandHorizontally(
            animationSpec = tween(durationMillis = enterAnimationDuration),
            expandFrom = Alignment.End
        ),
        exit = slideOutHorizontally(animationSpec = tween(durationMillis = exitAnimationDuration)) + shrinkHorizontally(
            animationSpec = tween(durationMillis = exitAnimationDuration)
        ) + fadeOut(animationSpec = tween(durationMillis = exitAnimationDuration))
    ) {
        var navigationRailWidth by remember { mutableStateOf(noPadding) }

        Box {
            content(PaddingValues(start = navigationRailWidth))
            Row {
                NavigationRailImpl(
                    items = items,
                    selectedItemIndex = selectedItemIndex,
                    onSelectedItemChange = { index ->
                        selectedItemIndex = index
                    },
                    navigator = navigator,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        navigationRailWidth = Dp(coordinates.size.width / density)

                        globalViewModel.updateNavigationComposableSize(
                            DpSize(
                                width = Dp(coordinates.size.width / density),
                                height = Dp(coordinates.size.height / density)
                            )
                        )
                    },
                    containerColor = containerColor

                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
    AnimatedVisibility(
        screenClass == ScreenClass.Large,
        enter = slideInHorizontally(animationSpec = tween(durationMillis = enterAnimationDuration)) + expandHorizontally(
            animationSpec = tween(durationMillis = enterAnimationDuration),
            expandFrom = Alignment.End
        ),
        exit = slideOutHorizontally(animationSpec = tween(durationMillis = exitAnimationDuration)) + shrinkHorizontally(
            animationSpec = tween(durationMillis = exitAnimationDuration)
        ) + fadeOut(animationSpec = tween(durationMillis = exitAnimationDuration))
    ) {
        NavigationDrawerImpl(
            items = items,
            selectedItemIndex = selectedItemIndex,
            onSelectedItemChange = { index ->
                selectedItemIndex = index
            },
            navigator = navigator,
            containerColor = containerColor,
            content = { content.invoke(PaddingValues()) },
            updateNavigationComposableSize = {
                globalViewModel.updateNavigationComposableSize(it)
            },
            density = density
        )
    }
}

@Composable
private fun NavigationBarImpl(
    items: List<NavigationItem>,
    selectedItemIndex: Int,
    onSelectedItemChange: (Int) -> Unit,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    containerColor: Color
) {
    NavigationBar(
        containerColor = containerColor, modifier = modifier
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(colors = NavigationBarItemDefaults.colors(

            ), selected = selectedItemIndex == index, onClick = {
                navigator.navigate(
                    item.itemRoute,
                    options = NavOptions(popUpTo = PopUpTo.First())
                )
                onSelectedItemChange.invoke(index)
            }, label = {
                Text(
                    text = item.title, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }, icon = {
                Icon(
                    imageVector = if (index == selectedItemIndex) {
                        item.selectedIcon
                    } else item.unselectedIcon, contentDescription = null
                )
            })
        }
    }
}

@Composable
private fun NavigationRailImpl(
    items: List<NavigationItem>,
    selectedItemIndex: Int,
    onSelectedItemChange: (Int) -> Unit,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    containerColor: Color
) {
    NavigationRail(containerColor = containerColor, modifier = modifier) {
        Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxHeight()) {
            items.forEachIndexed { index, item ->
                NavigationRailItem(colors = NavigationRailItemDefaults.colors(

                ), selected = selectedItemIndex == index, onClick = {
                    navigator.navigate(
                        item.itemRoute,
                        options = NavOptions(popUpTo = PopUpTo.First())
                    )
                    onSelectedItemChange.invoke(index)
                }, label = {
                    Text(
                        text = item.title, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }, icon = {
                    Icon(
                        imageVector = if (index == selectedItemIndex) {
                            item.selectedIcon
                        } else item.unselectedIcon, contentDescription = null
                    )
                })
            }
        }
    }
}

@Composable
private fun NavigationDrawerImpl(
    items: List<NavigationItem>,
    selectedItemIndex: Int,
    onSelectedItemChange: (Int) -> Unit,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    containerColor: Color,
    content: @Composable () -> Unit,
    updateNavigationComposableSize: (DpSize) -> Unit,
    density: Float = LocalDensity.current.density
) {
    PermanentNavigationDrawer(modifier = modifier, drawerContent = {
        PermanentDrawerSheet(
            drawerContainerColor = containerColor,
            modifier = Modifier.width(permanentDrawerSheet).onGloballyPositioned {
                updateNavigationComposableSize(
                    DpSize(
                        width = Dp(it.size.width / density),
                        height = Dp(it.size.height / density)
                    )
                )

            }
        ) {
            Text(
                stringResource(Res.string.diary_cmp),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(pageListHorizontalPadding)
                    .padding(vertical = largePadding),
            )
            items.forEachIndexed { index, item ->
                Spacer(modifier = Modifier.height(extraSmallPadding))
                NavigationDrawerItem(modifier = Modifier.padding(pageListHorizontalPadding).height(
                    permanentDrawerItemHeight
                ), colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent
                ), selected = selectedItemIndex == index, onClick = {
                    navigator.navigate(
                        item.itemRoute,
                        options = NavOptions(popUpTo = PopUpTo.First())
                    )
                    onSelectedItemChange.invoke(index)
                }, label = {
                    Text(
                        text = item.title, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }, icon = {
                    Icon(
                        imageVector = if (index == selectedItemIndex) {
                            item.selectedIcon
                        } else item.unselectedIcon, contentDescription = null
                    )
                })
            }

        }
    }, content = {
        content.invoke()
    })
}