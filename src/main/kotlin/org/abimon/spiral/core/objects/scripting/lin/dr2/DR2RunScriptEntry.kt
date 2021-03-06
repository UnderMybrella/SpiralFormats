package org.abimon.spiral.core.objects.scripting.lin.dr2

import org.abimon.spiral.core.objects.scripting.lin.LinScript

class DR2RunScriptEntry(val chapter: Int, val scene: Int, val room: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], (args[1] shl 8) or args[2], (args[3] shl 8) or args[4])

    override val opCode: Int = 0x1B
    override val rawArguments: IntArray = intArrayOf(chapter, scene shr 8, scene % 256, room shr 8, room % 256)

    override fun format(): String = "Run Script|$chapter, ${scene shr 8}, ${scene % 256}, ${room shr 8}, ${room % 256}"
}