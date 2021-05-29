package com.gt.giscollect.module.layer.func.tool

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.CursorLoader
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File


object UriPathTool {
//
//    fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
//        var res: String? = null
//        val proj = arrayOf<String>(MediaStore.Images.Media.DATA)
//        val cursor = contentUri?.let { context.contentResolver.query(it, proj, null, null, null) }
//        if (null != cursor && cursor.moveToFirst()) {
//            val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            res = cursor.getString(column_index)
//            cursor.close()
//        }
//        return res
//    }
//
//    fun getRealPath(context: Context, uri: Uri): String {
//
//    }
//
//    /**
//     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
//     */
//    @SuppressLint("NewApi")
//    fun getPath(context: Context, uri: Uri): String? {
//        val isKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
//        // DocumentProvider
//        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) { // ExternalStorageProvider
//            if (isExternalStorageDocument(uri)) {
//                val docId: String = DocumentsContract.getDocumentId(uri)
//                val split = docId.split(":").toTypedArray()
//                val type = split[0]
//                if ("primary".equals(type, ignoreCase = true)) {
//                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
//                }
//            } else if (isDownloadsDocument(uri)) {
//                val id: String = DocumentsContract.getDocumentId(uri)
//                val contentUri: Uri = ContentUris.withAppendedId(
//                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
//                )
//                return getDataColumn(context, contentUri, null, null)
//            } else if (isMediaDocument(uri)) {
//                val docId: String = DocumentsContract.getDocumentId(uri)
//                val split = docId.split(":").toTypedArray()
//                val type = split[0]
//                var contentUri: Uri? = null
//                if ("image" == type) {
//                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                } else if ("video" == type) {
//                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//                } else if ("audio" == type) {
//                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                }
//                val selection = "_id=?"
//                val selectionArgs =
//                    arrayOf(split[1])
//                return getDataColumn(context, contentUri, selection, selectionArgs)
//            }
//        } else if ("content".equals(uri.getScheme(), ignoreCase = true)) {
//            return getDataColumn(context, uri, null, null)
//        } else if ("file".equals(uri.getScheme(), ignoreCase = true)) {
//            return uri.getPath()
//        }
//        return null
//    }
//
//    /**
//     * Get the value of the data column for this Uri. This is useful for
//     * MediaStore Uris, and other file-based ContentProviders.
//     *
//     * @param context       The context.
//     * @param uri           The Uri to query.
//     * @param selection     (Optional) Filter used in the query.
//     * @param selectionArgs (Optional) Selection arguments used in the query.
//     * @return The value of the _data column, which is typically a file path.
//     */
//    fun getDataColumn(
//        context: Context, uri: Uri?, selection: String?,
//        selectionArgs: Array<String>?
//    ): String? {
//        var cursor: Cursor? = null
//        val column = "_data"
//        val projection = arrayOf(column)
//        try {
//            if (uri == null) return null
//            cursor = context.contentResolver.query(
//                uri, projection, selection, selectionArgs,
//                null
//            )
//            if (cursor != null && cursor.moveToFirst()) {
//                val column_index: Int = cursor.getColumnIndexOrThrow(column)
//                return cursor.getString(column_index)
//            }
//        } finally {
//            if (cursor != null) cursor.close()
//        }
//        return null
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is ExternalStorageProvider.
//     */
//    fun isExternalStorageDocument(uri: Uri): Boolean {
//        return "com.android.externalstorage.documents" == uri.getAuthority()
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is DownloadsProvider.
//     */
//    fun isDownloadsDocument(uri: Uri): Boolean {
//        return "com.android.providers.downloads.documents" == uri.getAuthority()
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is MediaProvider.
//     */
//    fun isMediaDocument(uri: Uri): Boolean {
//        return "com.android.providers.media.documents" == uri.getAuthority()
//    }

    fun getRealPath(
        context: Context,
        fileUri: Uri
    ): String? {
        val realPath: String?
        // SDK < API11
        realPath = if (Build.VERSION.SDK_INT < 11) {
            getRealPathFromURI_BelowAPI11(context, fileUri)
        } else if (Build.VERSION.SDK_INT < 19) {
            getRealPathFromURI_API11to18(context, fileUri)
        } else {
            getRealPathFromURI_API19(context, fileUri)
        }
        return realPath
    }

    @SuppressLint("NewApi")
    fun getRealPathFromURI_API11to18(
        context: Context?,
        contentUri: Uri?
    ): String? {
        val proj =
            arrayOf(MediaStore.Images.Media.DATA)
        var result: String? = null
        val cursorLoader =
            CursorLoader(context, contentUri, proj, null, null, null)
        val cursor: Cursor = cursorLoader.loadInBackground()
        if (cursor != null) {
            val column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(column_index)
            cursor.close()
        }
        return result
    }

    fun getRealPathFromURI_BelowAPI11(
        context: Context,
        contentUri: Uri?
    ): String {
        val proj =
            arrayOf(MediaStore.Images.Media.DATA)
        val cursor =
            context.contentResolver.query(contentUri!!, proj, null, null, null)
        var column_index = 0
        var result = ""
        if (cursor != null) {
            column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(column_index)
            cursor.close()
            return result
        }
        return result
    }

    @SuppressLint("NewApi")
    fun getRealPathFromURI_API19(
        context: Context,
        uri: Uri
    ): String? {
        val isKitKat =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(
                context,
                uri
            )
        ) { // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                // This is for checking Main Memory
                return if ("primary".equals(type, ignoreCase = true)) {
                    if (split.size > 1) {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else {
                        Environment.getExternalStorageDirectory().toString() + "/"
                    }
                    // This is for checking SD Card
                } else {
                    "storage" + "/" + docId.replace(":", "/")
                }
            } else if (isDownloadsDocument(uri)) {
                val fileName = getFilePath(context, uri)
                if (fileName != null) {
                    return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName
                }
                var id = DocumentsContract.getDocumentId(uri)
                if (id.startsWith("raw:")) {
                    id = id.replaceFirst("raw:".toRegex(), "")
                    val file = File(id)
                    if (file.exists()) return id
                }
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(
                uri.scheme,
                ignoreCase = true
            )
        ) { // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getFilePath(context: Context, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

}