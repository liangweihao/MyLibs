package com.nothing.commonutils.utils

import okhttp3.internal.and

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/5/22
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */

/**
 * Hexadecimal encoding where each byte is represented by two hexadecimal digits.
 * @hide
 */

object HexEncoding {
    private val LOWER_CASE_DIGITS =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    private val UPPER_CASE_DIGITS =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    /**
     * Encodes the provided byte as a two-digit hexadecimal String value.
     *
     * @param  b byte to encode
     * @param  upperCase `true` to use uppercase letters, `false`
     * for lowercase
     * @return the encoded string
     *
     * @hide
     */
    fun encodeToString(b:Byte, upperCase:Boolean):String {
        val digits = if (upperCase) UPPER_CASE_DIGITS else LOWER_CASE_DIGITS
        val buf = CharArray(2) // We always want two digits.

        buf[0] = digits[b.toInt() shr 4 and 0xf]
        buf[1] = digits[b and 0xf]
        return String(buf, 0, 2)
    }

    /**
     * Encodes the provided data as a sequence of hexadecimal characters.
     *
     * @param  data byte array to encode
     * @return the encoded data, using uppercase letters
     *
     * @hide
     */
    fun encode(data:ByteArray):CharArray {
        return encode(data, 0, data.size, true /* upperCase */)
    }

    /**
     * Encodes the provided data as a sequence of hexadecimal characters.
     *
     * @param  data byte array to encode
     * @param  upperCase `true` to use uppercase letters, `false`
     * for lowercase
     * @return the encoded data
     *
     * @hide
     */
    fun encode(data:ByteArray, upperCase:Boolean):CharArray {
        return encode(data, 0, data.size, upperCase)
    }

    /**
     * Encodes the provided data as a sequence of hexadecimal characters.
     *
     * @param  data byte array containing the data to encode
     * @param  offset offset of the data to encode in the `data` array
     * @param  len length of the data to encode in the `data` array
     * @return the encoded data, using uppercase letters
     *
     * @hide
     */

    fun encode(data:ByteArray, offset:Int, len:Int):CharArray {
        return encode(data, offset, len, true /* upperCase */)
    }

    /**
     * Encodes the provided data as a sequence of hexadecimal characters.
     */
    private fun encode(data:ByteArray, offset:Int, len:Int, upperCase:Boolean):CharArray {
        val digits = if (upperCase) UPPER_CASE_DIGITS else LOWER_CASE_DIGITS
        val result = CharArray(len * 2)
        for (i in 0 until len) {
            val b = data[offset + i]
            val resultIndex = 2 * i
            result[resultIndex] = digits[b.toInt() shr 4 and 0x0f]
            result[resultIndex + 1] = digits[b and 0x0f]
        }
        return result
    }

    /**
     * Encodes the provided data as a sequence of hexadecimal characters.
     *
     * @param  data byte array to encode
     * @return the encoded data, using uppercase letters
     *
     * @hide
     */
    fun encodeToString(data:ByteArray):String {
        return encodeToString(data, true /* upperCase */)
    }

    /**
     * Encodes the provided data as a sequence of hexadecimal characters.
     *
     * @param  data byte array to encode.
     * @param  upperCase `true` to use uppercase letters, `false`
     * for lowercase
     * @return the encoded data
     *
     * @hide
     */

    fun encodeToString(data:ByteArray, upperCase:Boolean):String {
        return String(encode(data, upperCase))
    }

    /**
     * Decodes the provided hexadecimal sequence. Odd-length inputs are not
     * allowed.
     *
     * @param  encoded string of hexadecimal characters to decode. Letters
     * can be either uppercase or lowercase.
     * @return the decoded data
     * @throws IllegalArgumentException if the input is malformed
     *
     * @hide
     */
    @Throws(IllegalArgumentException::class) fun decode(encoded:String):ByteArray {
        return decode(encoded.toCharArray())
    }

    /**
     * Decodes the provided hexadecimal sequence.
     *
     * @param  encoded string of hexadecimal characters to decode. Letters
     * can be either uppercase or lowercase.
     * @param  allowSingleChar If `true` odd-length inputs are allowed and
     * the first character is interpreted as the lower bits of the first
     * result byte. If `false` odd-length inputs are not allowed.
     * @return the decoded data
     * @throws IllegalArgumentException if the input is malformed
     *
     * @hide
     */
    @Throws(IllegalArgumentException::class)
    fun decode(encoded:String, allowSingleChar:Boolean):ByteArray {
        return decode(encoded.toCharArray(), allowSingleChar)
    }

    /**
     * Decodes the provided hexadecimal sequence. Odd-length inputs are not
     * allowed.
     *
     * @param  encoded char array of hexadecimal characters to decode. Letters
     * can be either uppercase or lowercase.
     * @return the decoded data
     * @throws IllegalArgumentException if the input is malformed
     *
     * @hide
     */
    @Throws(IllegalArgumentException::class) fun decode(encoded:CharArray):ByteArray {
        return decode(encoded, false)
    }

    /**
     * Decodes the provided hexadecimal sequence.
     *
     * @param  encoded char array of hexadecimal characters to decode. Letters
     * can be either uppercase or lowercase.
     * @param  allowSingleChar If `true` odd-length inputs are allowed and
     * the first character is interpreted as the lower bits of the first
     * result byte. If `false` odd-length inputs are not allowed.
     * @return the decoded data
     * @throws IllegalArgumentException if the input is malformed
     *
     * @hide
     */
    @Throws(IllegalArgumentException::class)
    fun decode(encoded:CharArray, allowSingleChar:Boolean):ByteArray {
        val encodedLength = encoded.size
        val resultLengthBytes = (encodedLength + 1) / 2
        val result = ByteArray(resultLengthBytes)
        var resultOffset = 0
        var i = 0
        if (allowSingleChar) {
            if (encodedLength % 2 != 0) { // Odd number of digits -- the first digit is the lower 4 bits of the first result
                // byte.
                result[resultOffset++] = toDigit(encoded, i).toByte()
                i++
            }
        } else {
            require(encodedLength % 2 == 0) { "Invalid input length: $encodedLength" }
        }
        while (i < encodedLength) {
            result[resultOffset++] = (toDigit(encoded, i) shl 4 or toDigit(encoded, i + 1)).toByte()
            i += 2
        }
        return result
    }

    @Throws(IllegalArgumentException::class)
    private fun toDigit(str:CharArray, offset:Int):Int { // NOTE: that this isn't really a code point in the traditional sense, since we're
        // just rejecting surrogate pairs outright.
        val pseudoCodePoint = str[offset].toInt()
        if ('0'.toInt() <= pseudoCodePoint && pseudoCodePoint <= '9'.toInt()) {
            return pseudoCodePoint - '0'.toInt()
        } else if ('a'.toInt() <= pseudoCodePoint && pseudoCodePoint <= 'f'.toInt()) {
            return 10 + (pseudoCodePoint - 'a'.toInt())
        } else if ('A'.toInt() <= pseudoCodePoint && pseudoCodePoint <= 'F'.toInt()) {
            return 10 + (pseudoCodePoint - 'A'.toInt())
        }
        throw IllegalArgumentException("Illegal char: " + str[offset] + " at offset " + offset)
    }
}