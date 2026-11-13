package com.limpyo.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.limpyo.app.core.designsystem.theme.LimpyoRadius
import com.limpyo.app.core.designsystem.theme.LimpyoSize
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme

/**
 * Text field (docs/DESIGN.md): 56dp, raised background, 12 radius, label above, helper or error below.
 * Focus shows a 2dp accent ring. [error] replaces [helper] and turns the ring danger-colored.
 */
@Composable
fun LimpyoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helper: String? = null,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val c = LimpyoTheme.colors
    val t = LimpyoTheme.type
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(LimpyoRadius.medium)
    val ring = when {
        error != null -> c.danger
        focused -> c.accentStrong
        else -> c.border
    }
    Column(modifier.fillMaxWidth()) {
        Text(label, style = t.label, color = c.ink)
        Spacer(Modifier.height(LimpyoSpace.s))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = t.body.copy(color = c.ink),
            cursorBrush = SolidColor(c.accentStrong),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .semantics { contentDescription = label },
            decorationBox = { inner ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(LimpyoSize.textField)
                        .background(c.raised, shape)
                        .border(if (focused || error != null) LimpyoSize.focusRing else LimpyoSize.border, ring, shape)
                        .padding(horizontal = LimpyoSpace.l),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder != null) Text(placeholder, style = t.body, color = c.muted)
                    inner()
                }
            },
        )
        val below = error ?: helper
        if (below != null) {
            Spacer(Modifier.height(LimpyoSpace.xs))
            Text(below, style = t.caption, color = if (error != null) c.danger else c.muted)
        }
    }
}
