package net.irext.ircontrol.compose.utils

import android.content.Context
import java.io.File
import java.io.InputStream

private const val BinDir = "bin"
private const val FileNamePrefix = "irext_"
private const val FileNameExt = ".ir"

fun Context.remoteBinFile(remoteMap: String): File {
    val dir = File(requireNotNull(getExternalFilesDir(null)), BinDir).also { it.mkdirs() }
    return dir.resolve("$FileNamePrefix$remoteMap$FileNameExt")
}

fun File.writeFrom(inputStream: InputStream) {
    inputStream.use { input ->
        outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

fun File.readBytesOrNull(): ByteArray? = takeIf { isFile && canRead() }?.readBytes()
