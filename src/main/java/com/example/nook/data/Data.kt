package com.example.nook.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.example.nook.R
import androidx.annotation.DrawableRes

fun loadOrientedBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return null

        val rotationDegrees = context.contentResolver.openInputStream(uri)?.use { stream ->
            try {
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } catch (_: Exception) {
                0f
            }
        } ?: 0f

        if (rotationDegrees != 0f) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    } catch (_: Exception) {
        null
    }
}

data class NookItem(
    val name: String,
    val time: String = "Just now",
    @DrawableRes val image: Int = R.drawable.ic_launcher_background,
    val zone: String,
    val description: String = "",
    val category: String = "",
    val imageUri: String? = null,
    val tags: List<String> = emptyList()
)

data class Zone(
    val name: String,
    val meta: String = "0 items • Just now",
    @DrawableRes val image: Int = R.drawable.ic_launcher_background,
    val items: List<NookItem> = emptyList(),
    val imageUri: String? = null
)

const val SUPPORT_EMAIL = "support@nookapp.com"

data class Reminder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val note: String = "",
    val timeMillis: Long,
    val timeText: String,
    val dateText: String,
    val relatedItemName: String = "",
    val relatedZoneName: String = "",
    val isEnabled: Boolean = true
)

data class ActivityEntry(
    val action: String,
    val itemName: String,
    val time: String = "Just now",
    val zone: String = "",
    @DrawableRes val image: Int = 0,
    val day: String = "Today",
    val imageUri: String? = null
)

val passportItem = NookItem(
    name = "Passport",
    time = "3 weeks ago",
    image = R.drawable.ic_launcher_background,
    zone = "Hallway Drawer",
    description = "Hidden in the Hallway Drawer, for out-of-country trips someday.",
    category = "Documents",
    tags = listOf("document", "personal")
)

val allItems = listOf(
    passportItem,
    NookItem("Remote", "5 hours ago", R.drawable.ic_launcher_background, "Garage Shelf"),
    NookItem("Wallet", "This morning", R.drawable.ic_launcher_background, "Bedroom Desk"),
    NookItem("Spare Keys", "2 days ago", R.drawable.ic_launcher_background, "Hallway Drawer")
)

val hallwayDrawer = Zone("Hallway Drawer", "6 items • 3 min ago", R.drawable.ic_launcher_background, listOf(
    passportItem,
    NookItem("Flashlight", "9 hours ago", R.drawable.ic_launcher_background, "Hallway Drawer"),
    NookItem("Spare Keys", "2 days ago", R.drawable.ic_launcher_background, "Hallway Drawer"),
    NookItem("Airpods", "3 min ago", R.drawable.ic_launcher_background, "Hallway Drawer"),
    NookItem("Power Bank", "Yesterday", R.drawable.ic_launcher_background, "Hallway Drawer"),
    NookItem("Utility Bills", "1 hour ago", R.drawable.ic_launcher_background, "Hallway Drawer")
))

val zones = emptyList<Zone>()

val activities = listOf(
    ActivityEntry("Added", "Journal", "10:30 PM", "Bedroom Desk", R.drawable.ic_launcher_background, "Today"),
    ActivityEntry("Checked", "Spare Keys", "10:17 PM", "Hallway Drawer", R.drawable.ic_launcher_background, "Today"),
    ActivityEntry("Removed", "Old Wallet", "8:49 AM", "Garage Shelf", R.drawable.ic_launcher_background, "Yesterday"),
    ActivityEntry("Removed", "Lipstick", "8:37 AM", "Garage Shelf", R.drawable.ic_launcher_background, "Yesterday")
)
