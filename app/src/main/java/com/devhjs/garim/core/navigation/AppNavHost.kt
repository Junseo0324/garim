package com.devhjs.garim.core.navigation
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.devhjs.garim.presentation.home.HomeScreenRoot
import com.devhjs.garim.presentation.editor.EditorScreenRoot
import com.devhjs.garim.presentation.result.ResultScreenRoot

@Composable
fun AppNavHost() {
    val stack = rememberNavBackStack(HomeRoute)
    NavDisplay(
        backStack = stack,
        onBack = { if (stack.size > 1) stack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreenRoot(onNavigateToEditor = { stack.add(EditorRoute(it)) })
            }
            entry<EditorRoute> { route ->
                EditorScreenRoot(route.photoId,
                    onNavigateToResult = { stack.add(ResultRoute(it)) },
                    onNavigateBack = { stack.removeLastOrNull() })
            }
            entry<ResultRoute> { route ->
                ResultScreenRoot(route.outputId,
                    onNavigateBack = { stack.removeLastOrNull() },
                    onNavigateToNewEditor = {
                        stack.clear()
                        stack.add(HomeRoute)
                        stack.add(EditorRoute(it))
                    })
            }
        }
    )
}
