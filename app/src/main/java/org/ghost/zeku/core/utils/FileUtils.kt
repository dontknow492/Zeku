package org.ghost.zeku.core.utils


import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

import org.ghost.zeku.MyApplication
import java.io.File
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import org.ghost.zeku.repository.PreferenceRepository

object FileUtil {
    fun deleteFile(path: String){
        runCatching {
            if (!File(path).delete()){
                DocumentFile.fromSingleUri(MyApplication.instance, path.toUri())?.delete()
            }
            deleteFileFromMediaStore(path)
        }
    }


    fun getCachePath(context: Context) : String {
//        PreferenceRepository
//        val preference = PreferenceManager.getDefaultSharedPreferences(context).getString("cache_path", "")
//        if (preference.isNullOrBlank() || !File(formatPath(preference)).canWrite()) {
//            val externalPath = context.getExternalFilesDir(null)
//            return if (externalPath == null){
//                context.cacheDir.absolutePath + "/downloads/"
//            }else{
//                externalPath.absolutePath + "/downloads/"
//            }
//        }else {
//            return formatPath(preference)
//        }
        return context.cacheDir.absolutePath + "/downloads/"
    }

    private fun deleteFileFromMediaStore(path: String) {
        val contentResolver = MyApplication.instance.contentResolver
        val file = File(path)
        val uri = MediaStore.Files.getContentUri("external")

        val selection: String
        val selectionArgs: Array<String>

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val parentPath = file.parentFile?.absolutePath.orEmpty()
            val primaryRoot = Environment.getExternalStorageDirectory().absolutePath
            if (parentPath.startsWith(primaryRoot)) {
                val trimmed = parentPath
                    .removePrefix(primaryRoot)
                    .removePrefix(File.separator)
                val relativePath = if (trimmed.isEmpty()) "" else "$trimmed${File.separator}"
                selection = MediaStore.MediaColumns.RELATIVE_PATH + " =? AND " +
                        MediaStore.MediaColumns.DISPLAY_NAME + " =?"
                selectionArgs = arrayOf(relativePath, file.name)
            } else {
                // Non-primary storage: fall back to DATA query
                selection = MediaStore.MediaColumns.DATA + " =?"
                selectionArgs = arrayOf(file.absolutePath)
            }
        } else {
            selection = MediaStore.MediaColumns.DATA + " =?"
            selectionArgs = arrayOf(file.absolutePath)
        }
        contentResolver.delete(uri, selection, selectionArgs)
    }

    fun exists(path: String) : Boolean {
        val file = File(path)
        if (path.isEmpty()) return false
        return file.exists()
    }
}