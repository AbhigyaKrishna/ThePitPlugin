package me.abhigya.pit.configuration

import space.arim.dazzleconf.serialiser.Decomposer
import space.arim.dazzleconf.serialiser.FlexibleType
import space.arim.dazzleconf.serialiser.ValueSerialiser
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object DurationSerializer : ValueSerialiser<Duration> {

    private val YEARS = Regex("(?<years>\\d+)(years?|yrs?|y)", RegexOption.IGNORE_CASE)
    private val MONTHS = Regex("(?<months>\\d+)(months?|mo)", RegexOption.IGNORE_CASE)
    private val WEEKS = Regex("(?<weeks>\\d+)(weeks?|w)", RegexOption.IGNORE_CASE)
    private val DAYS = Regex("(?<days>\\d+)(days?|d)", RegexOption.IGNORE_CASE)
    private val HOURS = Regex("(?<hours>\\d+)(hours?|hr|h)", RegexOption.IGNORE_CASE)
    private val MINUTES = Regex("(?<minutes>\\d+)(mins?|m)", RegexOption.IGNORE_CASE)
    private val SECONDS = Regex("(?<seconds>\\d+)(seconds?|sec|s)", RegexOption.IGNORE_CASE)
    private val TICKS = Regex("(?<ticks>\\d+)(ticks?|t)", RegexOption.IGNORE_CASE)
    private val MILLISECONDS = Regex("(?<milliseconds>\\d+)(milliseconds?|ms)", RegexOption.IGNORE_CASE)

    override fun getTargetClass(): Class<Duration> = Duration::class.java

    override fun deserialise(flexibleType: FlexibleType): Duration {
        val time = flexibleType.string
        val years = YEARS.findAll(time).sumOf { it.groupValues[1].toLong() }
        val months = MONTHS.findAll(time).sumOf { it.groupValues[1].toLong() }
        val weeks = WEEKS.findAll(time).sumOf { it.groupValues[1].toLong() }
        val days = DAYS.findAll(time).sumOf { it.groupValues[1].toLong() }
        val hours = HOURS.findAll(time).sumOf { it.groupValues[1].toLong() }
        val minutes = MINUTES.findAll(time).sumOf { it.groupValues[1].toLong() }
        val seconds = SECONDS.findAll(time).sumOf { it.groupValues[1].toLong() }
        val ticks = TICKS.findAll(time).sumOf { it.groupValues[1].toLong() }
        val millis = MILLISECONDS.findAll(time).sumOf { it.groupValues[1].toLong() }
        return (years * 31536000000L +
                months * 2628000000L +
                weeks * 604800000L +
                days * 86400000L +
                hours * 3600000L +
                minutes * 60000L +
                seconds * 1000L +
                ticks * 50L +
                millis).milliseconds
    }

    override fun serialise(value: Duration, decomposer: Decomposer): Any {
        return "${value.inWholeMilliseconds}ms"
    }

}