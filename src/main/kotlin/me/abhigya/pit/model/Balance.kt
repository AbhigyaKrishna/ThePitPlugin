package me.abhigya.pit.model

/**
 * Represents a balance with decimal upto 1 digit.
 */
class Balance private constructor(
    private var whole: ULong = 0u,
    private var decimal: UByte = 0u
) : Number(), Comparable<Balance> {

    companion object {
        fun zero() = Balance()

        fun <T : Number> fromNumber(value: T): Balance {
            val v = value.toDouble()
            val whole = v.toULong()
            val decimal = ((v - v.toLong()) * 10).toInt().toUByte()
            return Balance(whole, decimal)
        }
    }

    fun set(value: Balance) {
        whole = value.whole
        decimal = value.decimal
    }

    fun <T: Number> set(value: T) {
        set(fromNumber(value))
    }

    fun has(value: Balance): Boolean {
        return this >= value
    }

    fun <T: Number> has(value: T): Boolean {
        return has(fromNumber(value))
    }

    // -------------------------------------------- //
    // Number
    // -------------------------------------------- //
    override fun toByte(): Byte {
        return whole.toByte()
    }

    override fun toChar(): Char {
        return toInt().toChar()
    }

    override fun toDouble(): Double {
        return whole.toDouble() + decimal.toDouble() / 10
    }

    override fun toFloat(): Float {
        return whole.toFloat() + decimal.toFloat() / 10
    }

    override fun toInt(): Int {
        return whole.toInt()
    }

    override fun toLong(): Long {
        return whole.toLong()
    }

    override fun toShort(): Short {
        return whole.toShort()
    }

    operator fun dec(): Balance {
        whole--
        return this
    }

    operator fun inc(): Balance {
        whole++
        return this
    }

    // -------------------------------------------- //
    // Number Operations
    // -------------------------------------------- //

    // Add
    operator fun plus(other: Balance): Balance {
        return Balance(whole + other.whole, (decimal + other.decimal).toUByte())
    }

    operator fun plus(other: Number): Balance {
        return plus(fromNumber(other))
    }

    operator fun plusAssign(other: Balance) {
        whole += other.whole
        decimal = (decimal + other.decimal).toUByte()
    }

    operator fun plusAssign(other: Number) {
        plusAssign(fromNumber(other))
    }

    // Subtract
    operator fun minus(other: Balance): Balance {
        return Balance(whole - other.whole, (decimal - other.decimal).toUByte())
    }

    operator fun minus(other: Number): Balance {
        return minus(fromNumber(other))
    }

    operator fun minusAssign(other: Balance) {
        whole -= other.whole
        decimal = (decimal - other.decimal).toUByte()
    }

    operator fun minusAssign(other: Number) {
        minusAssign(fromNumber(other))
    }

    // Multiply
    operator fun times(other: Balance): Balance {
        val whole = whole * other.whole
        val decimal = (decimal * other.decimal).toUByte()
        return Balance(whole, decimal)
    }

    operator fun times(other: Number): Balance {
        return times(fromNumber(other))
    }

    operator fun timesAssign(other: Balance) {
        whole *= other.whole
        decimal = (decimal * other.decimal).toUByte()
    }

    operator fun timesAssign(other: Number) {
        timesAssign(fromNumber(other))
    }

    // Divide
    operator fun div(other: Balance): Balance {
        val whole = whole / other.whole
        val decimal = (decimal / other.decimal).toUByte()
        return Balance(whole, decimal)
    }

    operator fun div(other: Number): Balance {
        return div(fromNumber(other))
    }

    operator fun divAssign(other: Balance) {
        whole /= other.whole
        decimal = (decimal / other.decimal).toUByte()
    }

    operator fun divAssign(other: Number) {
        divAssign(fromNumber(other))
    }

    // -------------------------------------------- //
    // Comparable
    // -------------------------------------------- //
    override operator fun compareTo(other: Balance): Int {
        return when {
            whole > other.whole -> 1
            whole < other.whole -> -1
            else -> decimal.compareTo(other.decimal)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Balance) return false

        if (whole != other.whole) return false
        return decimal == other.decimal
    }

    // -------------------------------------------- //
    // Object
    // -------------------------------------------- //
    override fun hashCode(): Int {
        var result = whole.hashCode()
        result = 31 * result + decimal.hashCode()
        return result
    }

    override fun toString(): String {
        return "$whole.$decimal"
    }

}

fun Number.toBalance(): Balance {
    return Balance.fromNumber(this)
}