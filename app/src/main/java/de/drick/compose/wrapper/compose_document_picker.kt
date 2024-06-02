package de.drick.compose.wrapper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import de.drick.compose.sample.ui.log

@Composable
fun rememberDirectoryPicker(onDirectorySelected: (Uri?) -> Unit): DirectoryPickerState {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            log("directory selected: $uri")
            if (uri != null) {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                ctx.contentResolver.takePersistableUriPermission(uri, flags)
            }
            onDirectorySelected(uri)
        }
    )
    return remember { MutableDirectoryPickerState(launcher) }
}

interface DirectoryPickerState {
    fun launchDirectoryPickerIntent(uri: Uri? = null)
}

private class MutableDirectoryPickerState(
    var launcher: ManagedActivityResultLauncher<Uri?, Uri?>
) : DirectoryPickerState {
    override fun launchDirectoryPickerIntent(uri: Uri?) {
        launcher.launch(uri)
    }
}

@Composable
fun rememberFilePicker(): FilePickerState {
    val state = remember { MutableFilePickerState() }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            state.selectedUri = uri
        }
    )
    state.launcher = launcher
    return state
}

interface FilePickerState {
    val selectedUri: Uri?
    fun launchFilePickerIntent(filter: Array<String>)
}

private class MutableFilePickerState() : FilePickerState {
    var launcher: ManagedActivityResultLauncher<Array<String>, Uri?>? = null
    override var selectedUri: Uri? by mutableStateOf(null)
    override fun launchFilePickerIntent(filter: Array<String>) {
        checkNotNull(launcher).launch(filter)
    }
}


@Composable
fun rememberMediaPicker(): MediaPickerState {
    val state = remember { MutableMediaPickerState() }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            state.selectedUri = uri
        }
    )
    state.launcher = launcher
    return state
}

interface MediaPickerState {
    val selectedUri: Uri?
    fun launchFilePickerIntent(input: PickVisualMediaRequest)
}

private class MutableMediaPickerState() : MediaPickerState {
    var launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>? = null
    override var selectedUri: Uri? by mutableStateOf(null)
    override fun launchFilePickerIntent(input: PickVisualMediaRequest) {
        checkNotNull(launcher).launch(input)
    }
}

data class UriFile(
    val id: String,
    val name: String,
    val size: Long,
    val mimeType: String,
    val lastModified: Long,
    val flags: Int
) {
    companion object {
        private val projectionDocument = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS
        )
        fun fromUri(ctx: Context, uri: Uri): UriFile? {
            return ctx.contentResolver.query(uri, projectionDocument, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                UriFile(
                    id = cursor.getString(0),
                    name = cursor.getString(1),
                    size = cursor.getLong(2),
                    mimeType = cursor.getString(3),
                    lastModified = cursor.getLong(4),
                    flags = cursor.getInt(5)
                )
            }
        }
    }
}