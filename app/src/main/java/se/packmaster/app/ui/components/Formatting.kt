package se.packmaster.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import se.packmaster.app.R
import se.packmaster.domain.model.QuantityRule
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

val SwedishLocale: Locale = Locale.forLanguageTag("sv-SE")

private val decimalFormat = DecimalFormat("0.#", DecimalFormatSymbols(SwedishLocale))
private val dateFormat = DateTimeFormatter.ofPattern("d MMM", SwedishLocale)

fun formatAmount(value: Double): String = decimalFormat.format(value)

/** Parsar tal skrivna med både komma och punkt. */
fun parseDecimal(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

@Composable
fun formatWeight(grams: Int): String =
    if (grams >= 1000) {
        stringResource(R.string.weight_kg, decimalFormat.format(grams / 1000.0))
    } else {
        stringResource(R.string.weight_g, grams)
    }

fun formatEpochDay(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dateFormat)

fun epochDayToUtcMillis(epochDay: Long): Long = epochDay * 86_400_000L

fun utcMillisToEpochDay(millis: Long): Long =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()

@Composable
fun ruleLabel(rule: QuantityRule): String = when (rule) {
    QuantityRule.FIXED -> stringResource(R.string.rule_fixed)
    QuantityRule.PER_DAY -> stringResource(R.string.rule_per_day)
    QuantityRule.PER_NIGHT -> stringResource(R.string.rule_per_night)
}

/** T.ex. "4 × per dag" eller "2 st (fast antal)". */
@Composable
fun ruleDescription(rule: QuantityRule, amount: Double): String = when (rule) {
    QuantityRule.FIXED -> stringResource(R.string.rule_desc_fixed, formatAmount(amount))
    QuantityRule.PER_DAY -> stringResource(R.string.rule_desc_per_day, formatAmount(amount))
    QuantityRule.PER_NIGHT -> stringResource(R.string.rule_desc_per_night, formatAmount(amount))
}

@Composable
fun daysLabel(days: Int): String = pluralStringResource(R.plurals.days, days, days)
