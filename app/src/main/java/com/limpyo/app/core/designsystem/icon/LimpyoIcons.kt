package com.limpyo.app.core.designsystem.icon

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.model.WasteCategory

/**
 * Every icon in the app, in one place. Material Symbols Rounded, outline, 24dp.
 * The active bottom-nav item uses the filled version (docs/DESIGN.md section 6).
 */
object LimpyoIcons {
    @DrawableRes val Home = R.drawable.ic_home
    @DrawableRes val HomeFilled = R.drawable.ic_home_fill
    @DrawableRes val Schedule = R.drawable.ic_calendar_month
    @DrawableRes val ScheduleFilled = R.drawable.ic_calendar_month_fill
    @DrawableRes val Report = R.drawable.ic_flag
    @DrawableRes val ReportFilled = R.drawable.ic_flag_fill
    @DrawableRes val Scan = R.drawable.ic_center_focus_strong
    @DrawableRes val ScanFilled = R.drawable.ic_center_focus_strong_fill
    @DrawableRes val You = R.drawable.ic_person
    @DrawableRes val YouFilled = R.drawable.ic_person_fill

    @DrawableRes val Notifications = R.drawable.ic_notifications
    @DrawableRes val LocateMe = R.drawable.ic_my_location
    @DrawableRes val ChevronRight = R.drawable.ic_chevron_right
    @DrawableRes val Place = R.drawable.ic_location_on
    @DrawableRes val Check = R.drawable.ic_check
    @DrawableRes val Truck = R.drawable.ic_local_shipping
    @DrawableRes val Unknown = R.drawable.ic_help

    @DrawableRes
    fun category(category: WasteCategory): Int = when (category) {
        WasteCategory.Biodegradable -> R.drawable.ic_eco
        WasteCategory.Recyclable -> R.drawable.ic_recycling
        WasteCategory.Residual -> R.drawable.ic_delete
        WasteCategory.Special -> R.drawable.ic_warning
        WasteCategory.EWaste -> R.drawable.ic_battery_charging_full
    }
}

@Composable
fun WasteCategory.color(): Color = with(LimpyoTheme.categories) {
    when (this@color) {
        WasteCategory.Biodegradable -> biodegradable
        WasteCategory.Recyclable -> recyclable
        WasteCategory.Residual -> residual
        WasteCategory.Special -> special
        WasteCategory.EWaste -> ewaste
    }
}

@Composable
fun WasteCategory.label(): String = stringResource(
    when (this) {
        WasteCategory.Biodegradable -> R.string.category_biodegradable
        WasteCategory.Recyclable -> R.string.category_recyclable
        WasteCategory.Residual -> R.string.category_residual
        WasteCategory.Special -> R.string.category_special
        WasteCategory.EWaste -> R.string.category_ewaste
    },
)
