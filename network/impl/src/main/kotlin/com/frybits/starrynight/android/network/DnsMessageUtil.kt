/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.frybits.starrynight.android.network

import com.frybits.starrynight.network.models.A
import com.frybits.starrynight.network.models.AAAA
import com.frybits.starrynight.network.models.CNAME
import com.frybits.starrynight.network.models.DnsHeader
import com.frybits.starrynight.network.models.DnsMessage
import com.frybits.starrynight.network.models.DnsQuestion
import com.frybits.starrynight.network.models.DnsResourceRecord
import com.frybits.starrynight.network.models.MX
import com.frybits.starrynight.network.models.NS
import com.frybits.starrynight.network.models.TXT
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal fun parseMessage(messageBytes: ByteArray): DnsMessage {
    val buf = ByteBuffer.wrap(messageBytes).order(ByteOrder.BIG_ENDIAN)
    val header = parseHeader(buf)
    val questions = parseQuestions(buf, messageBytes, header.qdCount)
    val answerRRs = parseResourceRecords(buf, messageBytes, header.anCount)
    val authorityRRs = parseResourceRecords(buf, messageBytes, header.nsCount)
    val additionalRRs = parseResourceRecords(buf, messageBytes, header.arCount)
    return DnsMessage(header, questions, answerRRs, authorityRRs, additionalRRs)
}

private fun parseHeader(buf: ByteBuffer): DnsHeader {
    val id = buf.getShort().toInt() and 0xFFFF
    val flags1 = buf.get().toInt() and 0xFF
    val flags2 = buf.get().toInt() and 0xFF
    val qr = (flags1 and 0x80) != 0
    val opCode = (flags1 and 0x78) ushr 3
    val aa = (flags1 and 0x04) != 0
    val tc = (flags1 and 0x02) != 0
    val rd = (flags1 and 0x01) != 0
    val ra = (flags2 and 0x80) != 0
    val rCode = flags2 and 0x0F
    val qdCount = buf.getShort().toInt() and 0xFFFF
    val anCount = buf.getShort().toInt() and 0xFFFF
    val nsCount = buf.getShort().toInt() and 0xFFFF
    val arCount = buf.getShort().toInt() and 0xFFFF

    return DnsHeader(
        id = id,
        qr = qr,
        opCode = opCode,
        aa = aa,
        tc = tc,
        rd = rd,
        ra = ra,
        rCode = rCode,
        qdCount = qdCount,
        anCount = anCount,
        nsCount = nsCount,
        arCount = arCount
    )
}

private fun parseQuestions(buf: ByteBuffer, fullMessage: ByteArray, numQuestions: Int): List<DnsQuestion> {
    return buildList {
        repeat(numQuestions) {
            val domainName = parseDomainName(buf, fullMessage)
            val type = buf.getShort().toInt() and 0xFFFF
            val qClass = buf.getShort().toInt() and 0xFFFF
            add(DnsQuestion(domainName, type, qClass))
        }
    }
}

private fun parseDomainName(buf: ByteBuffer, fullMessage: ByteArray): String {
    return buildString {
        while (true) {
            val length = buf.get().toInt() and 0xFF
            val isCompressed = (length and 0xC0) == 0xC0
            if (isCompressed) {
                val pointer = ((length and 0x3F) shl 8) or (buf.get().toInt() and 0xFF)
                val pointerBuf = ByteBuffer.wrap(fullMessage).order(ByteOrder.BIG_ENDIAN)
                pointerBuf.position(pointer)
                append(parseDomainName(pointerBuf, fullMessage))
                break
            }

            if (length == 0) {
                break
            }

            val label = ByteArray(length).also { buf.get(it) }
            append(label.toString(Charsets.UTF_8))
            append('.')
        }
    }
}

private const val TYPE_A = 1
private const val TYPE_NS = 2
private const val TYPE_CNAME = 5
private const val TYPE_MX = 15
private const val TYPE_TXT = 16
private const val TYPE_AAAA = 28

private fun parseResourceRecords(buf: ByteBuffer, fullMessage: ByteArray, numRecords: Int): List<DnsResourceRecord> {
    return buildList {
        repeat(numRecords) {
            val domainName = parseDomainName(buf, fullMessage)
            val type = buf.getShort().toInt() and 0xFFFF
            val rClass = buf.getShort().toInt() and 0xFFFF
            val ttl = buf.getInt().toLong() and 0xFFFFFFFFL
            val rdLength = buf.getShort().toInt() and 0xFFFF
            val rdEnd = buf.position() + rdLength

            val rData = when (type) {
                TYPE_A -> {
                    val ip = InetAddress.getByAddress(ByteArray(4).also { buf.get(it) })
                    A(ip.hostAddress)
                }
                TYPE_AAAA -> {
                    val ip = InetAddress.getByAddress(ByteArray(16).also { buf.get(it) })
                    AAAA(ip.hostAddress)
                }
                TYPE_CNAME -> {
                    CNAME(parseDomainName(buf, fullMessage))

                }
                TYPE_MX -> {
                    val preference = buf.getShort().toInt() and 0xFFFF
                    MX(preference, parseDomainName(buf, fullMessage))
                }
                TYPE_NS -> {
                    NS(parseDomainName(buf, fullMessage))
                }
                TYPE_TXT -> {
                    val strings = buildList {
                        while (buf.position() < rdEnd) {
                            val len = buf.get().toInt() and 0xFF
                            val strBytes = ByteArray(len).also { buf.get(it) }
                            add(strBytes.toString(Charsets.UTF_8).trim('"'))
                        }
                    }
                    TXT(strings)
                }
                else -> {
                    buf.position(rdEnd)
                    null
                }
            }

            buf.position(rdEnd)

            add(DnsResourceRecord(domainName, type, rClass, ttl, rdLength, rData))
        }
    }
}
