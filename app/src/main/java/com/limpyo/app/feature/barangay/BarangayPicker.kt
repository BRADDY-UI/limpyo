package com.limpyo.app.feature.barangay

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.component.LimpyoTextField
import com.limpyo.app.core.designsystem.component.ListRow
import com.limpyo.app.core.designsystem.component.PrimaryButton
import com.limpyo.app.core.designsystem.component.RadioRow
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme

/**
 * ONB-04 / barangay switcher: "Use my location" + searchable list of the 26 barangays.
 * [header] is the screen's title area (onboarding text or a TopBar). [onDone] runs after saving.
 */
@Composable
fun BarangayPicker(
    header: @Composable () -> Unit,
    onDone: () -> Unit,
    viewModel: BarangayPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = LimpyoTheme.colors
    val t = LimpyoTheme.type
    // Asks for rough location only; locate() reports Failed if it's denied.
    val focus = LocalFocusManager.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { viewModel.locate() }

    Column(Modifier.fillMaxSize()) {
        header()
        LazyColumn(Modifier.weight(1f)) {
            item {
                LimpyoTextField(
                    value = state.query,
                    onValueChange = viewModel::onQuery,
                    label = stringResource(R.string.barangay_search),
                    imeAction = ImeAction.Search,
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                    modifier = Modifier.padding(horizontal = LimpyoSpace.screen, vertical = LimpyoSpace.s),
                )
            }
            item {
                ListRow(
                    title = stringResource(
                        if (state.locate == LocateStatus.Locating) R.string.barangay_locating else R.string.barangay_use_location,
                    ),
                    leadingIcon = LimpyoIcons.LocateMe,
                    onClick = {
                        focus.clearFocus()
                        permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    },
                )
                val note = when (state.locate) {
                    LocateStatus.Failed -> R.string.barangay_location_failed
                    LocateStatus.Outside -> R.string.barangay_outside
                    else -> null
                }
                if (note != null) {
                    Text(
                        stringResource(note),
                        style = t.caption,
                        color = c.warning,
                        modifier = Modifier.padding(horizontal = LimpyoSpace.screen, vertical = LimpyoSpace.xs),
                    )
                }
            }
            items(state.shown, key = { it.id }) { b ->
                RadioRow(title = b.name, selected = state.selectedId == b.id, onClick = {
                    focus.clearFocus() // close the keyboard so Continue is visible
                    viewModel.select(b.id)
                })
            }
            if (state.shown.isEmpty() && state.all.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.barangay_none_found, state.query),
                        style = t.body,
                        color = c.muted,
                        modifier = Modifier.padding(LimpyoSpace.screen),
                    )
                }
            }
            item {
                Text(
                    stringResource(R.string.barangay_approximate_note),
                    style = t.caption,
                    color = c.muted,
                    modifier = Modifier.padding(horizontal = LimpyoSpace.screen, vertical = LimpyoSpace.l),
                )
            }
        }
        // imePadding keeps Continue above the keyboard (the app draws edge to edge).
        Box(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(LimpyoSpace.screen)) {
            PrimaryButton(
                text = stringResource(R.string.onb_continue),
                onClick = { viewModel.save(onDone) },
                enabled = state.selectedId != null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(LimpyoSpace.xs))
    }
}
