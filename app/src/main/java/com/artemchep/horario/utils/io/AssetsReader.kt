package com.artemchep.horario.utils.io

import android.content.Context
import com.artemchep.basic.utils.IOUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Reads text from text file in assets directory.
 *
 * @return text from raw resource
 * @throws IOException
 */
@Throws(IOException::class)
fun readTextFromAssets(context: Context, path: String): String {
    val inputStream = context.assets.open(path)
    val inputStreamReader = InputStreamReader(inputStream)
    val bufferedReader = BufferedReader(inputStreamReader)
    return IOUtils.readTextFromBufferedReader(bufferedReader)
}