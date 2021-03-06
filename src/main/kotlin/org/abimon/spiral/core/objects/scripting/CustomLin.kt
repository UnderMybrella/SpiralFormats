package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.LinTextScript
import org.abimon.spiral.core.objects.scripting.lin.TextCountEntry
import org.abimon.spiral.core.objects.scripting.lin.TextEntry
import org.abimon.spiral.core.utils.removeEscapes
import org.abimon.spiral.core.utils.writeInt32LE
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class CustomLin {
    var type = 2
    var header: ByteArray = ByteArray(0)
    val entries: ArrayList<LinScript> = ArrayList()

    fun add(entry: LinScript) {
        entries.add(entry)
    }

    fun addAll(entries: Array<LinScript>) {
        this.entries.addAll(entries)
    }

    fun addAll(entries: List<LinScript>) {
        this.entries.addAll(entries)
    }

    /**
     * WARNING: This will not work with UDG!!
     */
    fun add(text: String) {
        entries.add(TextEntry(text, 0))
    }

    fun compile(out: OutputStream) {
        out.writeInt32LE(type)
        out.writeInt32LE(header.size + (if (type == 1) 12 else 16))

        val entryData = ByteArrayOutputStream()
        val textData = ByteArrayOutputStream()
        val textText = ByteArrayOutputStream()

        textData.writeInt32LE(entries.count { entry -> entry is LinTextScript })

        var textID = 0

        if (entries[0] !is TextCountEntry)
            entries.add(0, TextCountEntry(entries.count { entry -> entry is LinTextScript }))
        val numText = entries.count { entry -> entry is LinTextScript }

        entries.forEach { entry ->
            entryData.write(0x70)
            entryData.write(entry.opCode)

            if (entry is LinTextScript) {
                val strData = (entry.text ?: "Hello, Null!").removeEscapes().toByteArray(Charsets.UTF_16LE)
                textData.writeInt32LE((numText * 4L) + 8 + textText.size())
                if(entry.writeBOM) {
                    textText.write(0xFF)
                    textText.write(0xFE)
                }

                textText.write(strData)
                if(strData[strData.size - 1] != 0x00.toByte() || strData[strData.size - 2] != 0x00.toByte()) {
                    textText.write(0x00)
                    textText.write(0x00)
                }

                entryData.write(textID / 256)
                entryData.write(textID % 256)

                textID++
            } else {
                entry.rawArguments.forEach { arg -> entryData.write(arg) }
            }
        }

        textData.writeInt32LE((numText * 4L) + 8 + textText.size())

        if (type == 1)
            out.writeInt32LE(12 + entryData.size() + textData.size() + textText.size())
        else {
            out.writeInt32LE(16 + entryData.size())
            out.writeInt32LE(16 + entryData.size() + textData.size() + textText.size())
        }

        out.write(entryData.toByteArray())
        out.write(textData.toByteArray())
        out.write(textText.toByteArray())
    }
}