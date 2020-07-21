package su.rbv.ecwid_sklad.utils

import android.app.Activity
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import su.rbv.ecwid_sklad.App
import su.rbv.ecwid_sklad.Const
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

/**
 *    Toast messages from anywhere by id or string
 */
fun toast(messageId: Int, duration: Int = Toast.LENGTH_SHORT) =
    toast(
        App.ctx.getString(messageId), duration
    )
fun toast(message: String?, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(App.ctx, message, duration).show()

/**
 *  Create alert Yes/No dialog
 *
 * @param activity calling Activity
 * @param messageId id of message
 * @param positiveTextId id of positive button text
 * @param negativeTextId id of negative button text
 * @param confirmHandler confirm action click callback
 * @param cancelHandler cancel action click callback (optional)
 *
 * @return AlertDialog
 */
fun confirmDialog(
        activity: Activity,
        messageId: Int,
        positiveTextId: Int,
        negativeTextId: Int,
        confirmHandler: () -> Unit,
        cancelHandler: (() -> Unit)? = null
    ) : AlertDialog =
    AlertDialog.Builder(activity)
        .setCancelable(false)
        .setMessage(activity.getString(messageId))
        .setPositiveButton(activity.getString(positiveTextId)) { _, _ -> confirmHandler() }
        .setNegativeButton(activity.getString(negativeTextId)) { _, _ -> cancelHandler?.invoke() }
        .create()

/**
 *   Loading from file, resizing and JPEG compressing image from pictureFileName input image name
 *   It returns JPEG compressed image in ByteArray or null if pictureFileName is null
 */
fun loadAndResizePicture(pictureFileName: String?): ByteArray? {

    if (pictureFileName != null) {
        val srcBitmap = BitmapFactory.decodeFile(pictureFileName)
        if (srcBitmap != null) {
            val srcWidth = srcBitmap.width
            val srcHeight = srcBitmap.height

            val scale = if (srcWidth > srcHeight) {
                Const.MAX_PICTURE_DIMENSION.toFloat() / srcHeight
            } else {
                Const.MAX_PICTURE_DIMENSION.toFloat() / srcWidth
            }
            val newWidth = (srcWidth * scale).roundToInt()
            val newHeight = (srcHeight * scale).roundToInt()
            val scaledBitmap = Bitmap.createScaledBitmap(srcBitmap, newWidth, newHeight, false)

            val outStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG,
                Const.JPEG_COMPRESS_RATIO, outStream)
            return outStream.toByteArray()
        } else {
            throw Exception()
        }
    }
    return null
}

/** Generate flow with field text changes */
fun SearchView.getQueryTextChangesAsFlow(): Flow<String?> {

    val query = MutableStateFlow<String?>(null)

    setOnQueryTextListener(
        object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                query.value = newText
                return false
            }
        }
    )

    return query
}

/** get 1st selected file or throw exception if any error */
fun getSelectedFile(contentResolver: ContentResolver, uri: Uri): String? {

    val filePathFirstColumn = 0

    val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)

    contentResolver
        .query(
            uri,
            filePathColumns,
            null,
            null,
            null
        )?.apply {
            moveToFirst()
            val columnIndex = getColumnIndex(filePathColumns[filePathFirstColumn])
            val photoPath = getString(columnIndex)
            close()
            return photoPath
        }
    throw Exception()
}
