package me.abhigya.pit.util.ext

import java.util.*

fun UUID.toByteArray(array: ByteArray = ByteArray(16), offset: Int = 0): ByteArray {
    var msb = mostSignificantBits
    var lsb = leastSignificantBits

    for (i in 7 downTo 0) {
        array[offset + i] = (msb and 0xffL).toByte()
        msb = msb shr 8
    }
    for (i in 15 downTo 8) {
        array[offset + i] = (lsb and 0xffL).toByte()
        lsb = lsb shr 8
    }

    return array
}

fun fromByteArray(array: ByteArray, offset: Int = 0): UUID {
    return UUID(longFromBytes(
            array[offset], array[offset + 1],
            array[offset + 2], array[offset + 3],
            array[offset + 4], array[offset + 5],
            array[offset + 6], array[offset + 7]),
            longFromBytes(
                    array[offset + 8], array[offset + 9],
                    array[offset + 10], array[offset + 11],
                    array[offset + 12], array[offset + 13],
                    array[offset + 14], array[offset + 15]))
}

private fun longFromBytes(b1: Byte, b2: Byte, b3: Byte, b4: Byte, b5: Byte, b6: Byte, b7: Byte, b8: Byte): Long {
    return b1.toLong() and 0xffL shl 56 or (b2.toLong() and 0xffL shl 48) or (b3.toLong() and 0xffL shl 40) or (b4.toLong() and 0xffL shl 32) or (b5.toLong() and 0xffL shl 24
            ) or (b6.toLong() and 0xffL shl 16) or (b7.toLong() and 0xffL shl 8) or (b8.toLong() and 0xffL)
}