package com.example.nook

import com.example.nook.R
import androidx.annotation.DrawableRes

data class NookItem(
    val name: String, val time: String, @DrawableRes val image: Int,
    val zone: String, val description: String = "", val tags: List<String> = emptyList()
)
data class Zone(val name: String, val meta: String, @DrawableRes val image: Int, val items: List<NookItem> = emptyList())
data class ActivityEntry(val action: String, val itemName: String, val time: String, val zone: String, @DrawableRes val image: Int, val day: String)

val passportItem = NookItem("Passport", "3 weeks ago", R.drawable.ic_launcher_background, "Hallway Drawer",
    "Hidden in the Hallway Drawer, for out-of-country trips someday.", listOf("document", "personal"))

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

val zones = listOf(
    hallwayDrawer,
    Zone("Garage Shelf", "3 items • Yesterday", R.drawable.ic_launcher_background),
    Zone("Bedroom Desk", "1 item • 2 days ago", R.drawable.ic_launcher_background)
)

val activities = listOf(
    ActivityEntry("Added", "Journal", "10:30 PM", "Bedroom Desk", R.drawable.ic_launcher_background, "Today"),
    ActivityEntry("Checked", "Spare Keys", "10:17 PM", "Hallway Drawer", R.drawable.ic_launcher_background, "Today"),
    ActivityEntry("Removed", "Old Wallet", "8:49 AM", "Garage Shelf", R.drawable.ic_launcher_background, "Yesterday"),
    ActivityEntry("Removed", "Lipstick", "8:37 AM", "Garage Shelf", R.drawable.ic_launcher_background, "Yesterday")
)