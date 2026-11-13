package com.limpyo.app.feature.about

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.limpyo.app.BuildConfig
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.component.LimpyoCard
import com.limpyo.app.core.designsystem.component.ListRow
import com.limpyo.app.core.designsystem.component.SectionLabel
import com.limpyo.app.core.designsystem.component.TopBar
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme

private const val SOURCE_URL = "https://github.com/BRADDY-UI/limpyo"

/** Third-party code and data shipped in the app. Names and licenses are not translated. */
private val LICENSES = listOf(
    "Jetpack Compose, AndroidX" to "Apache 2.0",
    "Kotlin, kotlinx.serialization" to "Apache 2.0",
    "Hilt / Dagger" to "Apache 2.0",
    "Firebase Cloud Messaging" to "Apache 2.0",
    "Timber" to "Apache 2.0",
    "Inter, Space Grotesk" to "SIL Open Font License 1.1",
    "Material Symbols" to "Apache 2.0",
    "OpenStreetMap data" to "ODbL 1.0",
)

/** R-ABOUT: disclaimer, version, source, credits, licenses. Demo mode arrives with live tracking. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val c = LimpyoTheme.colors
    val t = LimpyoTheme.type
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(stringResource(R.string.about_title), onBack)

        LimpyoCard(Modifier.fillMaxWidth().padding(horizontal = LimpyoSpace.screen, vertical = LimpyoSpace.s)) {
            Text("limpyo", style = t.headline, color = c.ink)
            Text(stringResource(R.string.about_tagline), style = t.body, color = c.muted)
            Spacer(Modifier.height(LimpyoSpace.l))
            Text(stringResource(R.string.about_disclaimer_body), style = t.body, color = c.ink)
            Spacer(Modifier.height(LimpyoSpace.m))
            Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), style = t.caption, color = c.muted)
        }

        ListRow(
            title = stringResource(R.string.about_source),
            subtitle = stringResource(R.string.about_source_body),
            leadingIcon = LimpyoIcons.Code,
            trailingText = null,
            showChevron = false,
            onClick = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, SOURCE_URL.toUri()))
                } catch (_: ActivityNotFoundException) {
                    // No browser installed: nothing to open.
                }
            },
        )

        SectionLabel(stringResource(R.string.about_credits))
        Column(Modifier.padding(horizontal = LimpyoSpace.screen)) {
            Text(stringResource(R.string.about_credit_osm), style = t.body, color = c.ink)
            Spacer(Modifier.height(LimpyoSpace.xs))
            Text(stringResource(R.string.about_credit_concept), style = t.body, color = c.ink)
        }

        SectionLabel(stringResource(R.string.about_licenses))
        LICENSES.forEach { (name, license) -> ListRow(title = name, subtitle = license) }
        Spacer(Modifier.height(LimpyoSpace.x3))
    }
}
