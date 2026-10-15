package com.example.nook.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.example.nook.receivers.ReminderBroadcastReceiver
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object NookRepository {
    private const val PREFS_NAME = "nook_prefs"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Users Credentials ---
    fun loadUsers(context: Context): MutableList<Pair<String, String>> {
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString("all_users_credentials", null)
        val list = mutableListOf<Pair<String, String>>()
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(obj.getString("email") to obj.getString("password"))
                }
            } catch (_: Exception) {}
        }
        if (list.none { it.first.equals("guest101@gmail.com", ignoreCase = true) }) {
            list.add(0, "guest101@gmail.com" to "password")
        }
        return list
    }

    fun saveUsers(context: Context, users: List<Pair<String, String>>) {
        val array = JSONArray()
        for ((email, pass) in users) {
            val obj = JSONObject()
            obj.put("email", email)
            obj.put("password", pass)
            array.put(obj)
        }
        getPrefs(context).edit().putString("all_users_credentials", array.toString()).apply()
    }

    // --- User Profile ---
    fun getUserProfile(context: Context, email: String): Pair<String, String?> {
        val prefs = getPrefs(context)
        val safeKey = email.lowercase().trim()
        val defaultName = if (safeKey == "guest101@gmail.com") "Guest" else safeKey.substringBefore("@").replaceFirstChar { it.uppercase() }

        var name = prefs.getString("user_${safeKey}_name", null)
        var photoUri = prefs.getString("user_${safeKey}_photo", null)

        if (name == null && safeKey == "guest101@gmail.com") {
            name = prefs.getString("user_name", defaultName)
            photoUri = prefs.getString("user_profile_image", null)
            if (name != null) {
                saveUserProfile(context, email, name, photoUri)
            }
        }

        return (name ?: defaultName) to photoUri
    }

    fun saveUserProfile(context: Context, email: String, name: String, photoUri: String?) {
        val safeKey = email.lowercase().trim()
        val editor = getPrefs(context).edit()
        editor.putString("user_${safeKey}_name", name)
        if (photoUri != null) {
            editor.putString("user_${safeKey}_photo", photoUri)
        } else {
            editor.remove("user_${safeKey}_photo")
        }
        editor.apply()
    }

    // --- Notification Preferences ---
    fun loadNotificationPrefs(context: Context, email: String): Triple<Boolean, Boolean, Boolean> {
        val prefs = getPrefs(context)
        val safeKey = email.lowercase().trim()
        val allow = prefs.getBoolean("user_${safeKey}_notif_allow", true)
        val reminders = prefs.getBoolean("user_${safeKey}_notif_reminders", true)
        val activities = prefs.getBoolean("user_${safeKey}_notif_activities", true)
        return Triple(allow, reminders, activities)
    }

    fun saveNotificationPrefs(context: Context, email: String, allow: Boolean, reminders: Boolean, activities: Boolean) {
        val safeKey = email.lowercase().trim()
        val editor = getPrefs(context).edit()
        editor.putBoolean("user_${safeKey}_notif_allow", allow)
        editor.putBoolean("user_${safeKey}_notif_reminders", reminders)
        editor.putBoolean("user_${safeKey}_notif_activities", activities)
        editor.apply()
    }

    // --- Zones Persistence ---
    fun loadZones(context: Context, email: String): List<Zone> {
        val prefs = getPrefs(context)
        val safeKey = email.lowercase().trim()
        var jsonStr = prefs.getString("user_${safeKey}_zones", null)

        if (jsonStr.isNullOrEmpty() && safeKey == "guest101@gmail.com") {
            jsonStr = prefs.getString("zones", null)
        }

        if (jsonStr.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<Zone>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Zone(
                        name = obj.optString("name", ""),
                        meta = obj.optString("meta", "0 items • Just now"),
                        image = obj.optInt("image", 0),
                        imageUri = if (obj.has("imageUri") && !obj.isNull("imageUri")) obj.getString("imageUri") else null
                    )
                )
            }
            if (prefs.getString("user_${safeKey}_zones", null).isNullOrEmpty()) {
                saveZones(context, email, list)
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveZones(context: Context, email: String, zones: List<Zone>) {
        val safeKey = email.lowercase().trim()
        val array = JSONArray()
        for (z in zones) {
            val obj = JSONObject()
            obj.put("name", z.name)
            obj.put("meta", z.meta)
            obj.put("image", z.image)
            if (z.imageUri != null) obj.put("imageUri", z.imageUri)
            array.put(obj)
        }
        getPrefs(context).edit().putString("user_${safeKey}_zones", array.toString()).apply()
    }

    // --- Items Persistence ---
    fun loadItems(context: Context, email: String): List<NookItem> {
        val prefs = getPrefs(context)
        val safeKey = email.lowercase().trim()
        var jsonStr = prefs.getString("user_${safeKey}_items", null)

        if (jsonStr.isNullOrEmpty() && safeKey == "guest101@gmail.com") {
            jsonStr = prefs.getString("items", null)
        }

        if (jsonStr.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<NookItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val tagsList = mutableListOf<String>()
                if (obj.has("tags")) {
                    val tagsArr = obj.getJSONArray("tags")
                    for (t in 0 until tagsArr.length()) {
                        tagsList.add(tagsArr.getString(t))
                    }
                }
                list.add(
                    NookItem(
                        name = obj.optString("name", ""),
                        time = obj.optString("time", "Just now"),
                        image = obj.optInt("image", 0),
                        zone = obj.optString("zone", ""),
                        description = obj.optString("description", ""),
                        category = obj.optString("category", ""),
                        imageUri = if (obj.has("imageUri") && !obj.isNull("imageUri")) obj.getString("imageUri") else null,
                        tags = tagsList
                    )
                )
            }
            if (prefs.getString("user_${safeKey}_items", null).isNullOrEmpty()) {
                saveItems(context, email, list)
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveItems(context: Context, email: String, items: List<NookItem>) {
        val safeKey = email.lowercase().trim()
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("time", item.time)
            obj.put("image", item.image)
            obj.put("zone", item.zone)
            obj.put("description", item.description)
            obj.put("category", item.category)
            if (item.imageUri != null) obj.put("imageUri", item.imageUri)
            val tagsArr = JSONArray()
            item.tags.forEach { tagsArr.put(it) }
            obj.put("tags", tagsArr)
            array.put(obj)
        }
        getPrefs(context).edit().putString("user_${safeKey}_items", array.toString()).apply()
    }

    // --- Activities Persistence ---
    fun loadActivities(context: Context, email: String): List<ActivityEntry> {
        val prefs = getPrefs(context)
        val safeKey = email.lowercase().trim()
        var jsonStr = prefs.getString("user_${safeKey}_activities", null)

        if (jsonStr.isNullOrEmpty() && safeKey == "guest101@gmail.com") {
            jsonStr = prefs.getString("activities", null)
        }

        if (jsonStr.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<ActivityEntry>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ActivityEntry(
                        action = obj.optString("action", ""),
                        itemName = obj.optString("itemName", ""),
                        time = obj.optString("time", "Just now"),
                        zone = obj.optString("zone", ""),
                        image = obj.optInt("image", 0),
                        day = obj.optString("day", "Today"),
                        imageUri = if (obj.has("imageUri") && !obj.isNull("imageUri")) obj.getString("imageUri") else null
                    )
                )
            }
            if (prefs.getString("user_${safeKey}_activities", null).isNullOrEmpty()) {
                saveActivities(context, email, list)
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveActivities(context: Context, email: String, activities: List<ActivityEntry>) {
        val safeKey = email.lowercase().trim()
        val array = JSONArray()
        for (act in activities) {
            val obj = JSONObject()
            obj.put("action", act.action)
            obj.put("itemName", act.itemName)
            obj.put("time", act.time)
            obj.put("zone", act.zone)
            obj.put("image", act.image)
            obj.put("day", act.day)
            if (act.imageUri != null) obj.put("imageUri", act.imageUri)
            array.put(obj)
        }
        getPrefs(context).edit().putString("user_${safeKey}_activities", array.toString()).apply()
    }

    // --- Reminders Persistence & Alarm Scheduling ---
    fun loadReminders(context: Context, email: String): List<Reminder> {
        val prefs = getPrefs(context)
        val safeKey = email.lowercase().trim()
        val jsonStr = prefs.getString("user_${safeKey}_reminders", null)
        if (jsonStr.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<Reminder>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Reminder(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        note = obj.optString("note", ""),
                        timeMillis = obj.optLong("timeMillis", System.currentTimeMillis()),
                        timeText = obj.optString("timeText", "12:00 PM"),
                        dateText = obj.optString("dateText", "Today"),
                        relatedItemName = obj.optString("relatedItemName", ""),
                        relatedZoneName = obj.optString("relatedZoneName", ""),
                        isEnabled = obj.optBoolean("isEnabled", true)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveReminders(context: Context, email: String, reminders: List<Reminder>) {
        val safeKey = email.lowercase().trim()
        val array = JSONArray()
        for (r in reminders) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("title", r.title)
            obj.put("note", r.note)
            obj.put("timeMillis", r.timeMillis)
            obj.put("timeText", r.timeText)
            obj.put("dateText", r.dateText)
            obj.put("relatedItemName", r.relatedItemName)
            obj.put("relatedZoneName", r.relatedZoneName)
            obj.put("isEnabled", r.isEnabled)
            array.put(obj)
        }
        getPrefs(context).edit().putString("user_${safeKey}_reminders", array.toString()).apply()

        // Sync alarms for this user
        reminders.forEach { reminder ->
            if (reminder.isEnabled && reminder.timeMillis > System.currentTimeMillis()) {
                scheduleReminderAlarm(context, email, reminder)
            } else {
                cancelReminderAlarm(context, reminder)
            }
        }
    }

    fun scheduleReminderAlarm(context: Context, email: String, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_note", reminder.note)
            putExtra("reminder_item", reminder.relatedItemName)
            putExtra("reminder_zone", reminder.relatedZoneName)
            putExtra("user_email", email)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.timeMillis, pendingIntent)
        } catch (_: Exception) {}
    }

    fun cancelReminderAlarm(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }

    fun rescheduleAllRemindersOnBoot(context: Context) {
        val users = loadUsers(context)
        for ((email, _) in users) {
            val reminders = loadReminders(context, email)
            reminders.forEach { r ->
                if (r.isEnabled && r.timeMillis > System.currentTimeMillis()) {
                    scheduleReminderAlarm(context, email, r)
                }
            }
        }
    }

    // --- ZIP Backup & Restore Service ---
    fun exportBackupZip(context: Context, email: String, outputStream: OutputStream): Boolean {
        return try {
            val safeKey = email.lowercase().trim()
            val zones = loadZones(context, safeKey)
            val items = loadItems(context, safeKey)
            val activities = loadActivities(context, safeKey)
            val reminders = loadReminders(context, safeKey)
            val profile = getUserProfile(context, safeKey)

            // Gather all image URIs to package into ZIP
            val imageUrisToExport = mutableSetOf<String>()
            profile.second?.let { imageUrisToExport.add(it) }
            zones.forEach { z -> z.imageUri?.let { imageUrisToExport.add(it) } }
            items.forEach { itm -> itm.imageUri?.let { imageUrisToExport.add(it) } }

            val zipOut = ZipOutputStream(BufferedOutputStream(outputStream))

            // 1. Write manifest.json
            val manifestObj = JSONObject()
            manifestObj.put("backup_version", 1)
            manifestObj.put("app_name", "Nook")
            manifestObj.put("created_at", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
            manifestObj.put("owner_email", email)

            val countsObj = JSONObject()
            countsObj.put("zones", zones.size)
            countsObj.put("items", items.size)
            countsObj.put("activities", activities.size)
            countsObj.put("reminders", reminders.size)
            countsObj.put("images", imageUrisToExport.size)
            manifestObj.put("counts", countsObj)

            zipOut.putNextEntry(ZipEntry("manifest.json"))
            zipOut.write(manifestObj.toString(2).toByteArray())
            zipOut.closeEntry()

            // 2. Write data.json
            val dataObj = JSONObject()

            val profileObj = JSONObject()
            profileObj.put("name", profile.first)
            profile.second?.let { profileObj.put("photoUri", it) }
            dataObj.put("profile", profileObj)

            val zonesArr = JSONArray()
            zones.forEach { z ->
                val zObj = JSONObject()
                zObj.put("name", z.name)
                zObj.put("meta", z.meta)
                zObj.put("image", z.image)
                if (z.imageUri != null) zObj.put("imageUri", z.imageUri)
                zonesArr.put(zObj)
            }
            dataObj.put("zones", zonesArr)

            val itemsArr = JSONArray()
            items.forEach { itm ->
                val iObj = JSONObject()
                iObj.put("name", itm.name)
                iObj.put("time", itm.time)
                iObj.put("image", itm.image)
                iObj.put("zone", itm.zone)
                iObj.put("description", itm.description)
                iObj.put("category", itm.category)
                if (itm.imageUri != null) iObj.put("imageUri", itm.imageUri)
                val tagsArr = JSONArray()
                itm.tags.forEach { tagsArr.put(it) }
                iObj.put("tags", tagsArr)
                itemsArr.put(iObj)
            }
            dataObj.put("items", itemsArr)

            val activitiesArr = JSONArray()
            activities.forEach { act ->
                val aObj = JSONObject()
                aObj.put("action", act.action)
                aObj.put("itemName", act.itemName)
                aObj.put("time", act.time)
                aObj.put("zone", act.zone)
                aObj.put("image", act.image)
                aObj.put("day", act.day)
                if (act.imageUri != null) aObj.put("imageUri", act.imageUri)
                activitiesArr.put(aObj)
            }
            dataObj.put("activities", activitiesArr)

            val remindersArr = JSONArray()
            reminders.forEach { r ->
                val rObj = JSONObject()
                rObj.put("id", r.id)
                rObj.put("title", r.title)
                rObj.put("note", r.note)
                rObj.put("timeMillis", r.timeMillis)
                rObj.put("timeText", r.timeText)
                rObj.put("dateText", r.dateText)
                rObj.put("relatedItemName", r.relatedItemName)
                rObj.put("relatedZoneName", r.relatedZoneName)
                rObj.put("isEnabled", r.isEnabled)
                remindersArr.put(rObj)
            }
            dataObj.put("reminders", remindersArr)

            zipOut.putNextEntry(ZipEntry("data.json"))
            zipOut.write(dataObj.toString(2).toByteArray())
            zipOut.closeEntry()

            // 3. Export Image Files into images/ directory inside ZIP
            var imgCounter = 0
            for (uriStr in imageUrisToExport) {
                try {
                    val uri = Uri.parse(uriStr)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        imgCounter++
                        val entryName = "images/img_$imgCounter.jpg"
                        zipOut.putNextEntry(ZipEntry(entryName))
                        inputStream.copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                } catch (_: Exception) {}
            }

            zipOut.flush()
            zipOut.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun inspectBackupZip(context: Context, inputStream: InputStream): Pair<Boolean, Map<String, Any>> {
        val result = mutableMapOf<String, Any>()
        val tempDir = File(context.cacheDir, "backup_inspect_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        return try {
            val zipIn = ZipInputStream(BufferedInputStream(inputStream))
            var entry: ZipEntry? = zipIn.nextEntry
            var foundManifest = false
            var foundData = false

            while (entry != null) {
                val entryName = entry.name
                // Zip Slip Path Traversal Protection
                if (entryName.contains("..") || entryName.startsWith("/")) {
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                    continue
                }

                val outFile = File(tempDir, entryName)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { zipIn.copyTo(it) }
                    if (entryName == "manifest.json") foundManifest = true
                    if (entryName == "data.json") foundData = true
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            zipIn.close()

            if (!foundManifest || !foundData) {
                tempDir.deleteRecursively()
                return false to mapOf("error" to "Invalid backup format: missing manifest.json or data.json")
            }

            val manifestFile = File(tempDir, "manifest.json")
            val manifestJson = JSONObject(manifestFile.readText())

            if (manifestJson.optString("app_name") != "Nook" || manifestJson.optInt("backup_version", 0) != 1) {
                tempDir.deleteRecursively()
                return false to mapOf("error" to "Incompatible backup format version")
            }

            result["created_at"] = manifestJson.optString("created_at", "Unknown")
            result["owner_email"] = manifestJson.optString("owner_email", "Unknown")

            val counts = manifestJson.optJSONObject("counts")
            if (counts != null) {
                result["zones"] = counts.optInt("zones", 0)
                result["items"] = counts.optInt("items", 0)
                result["activities"] = counts.optInt("activities", 0)
                result["reminders"] = counts.optInt("reminders", 0)
                result["images"] = counts.optInt("images", 0)
            }
            result["temp_dir"] = tempDir.absolutePath

            true to result
        } catch (e: Exception) {
            tempDir.deleteRecursively()
            false to mapOf("error" to "Failed to read backup zip: ${e.localizedMessage}")
        }
    }

    fun restoreBackupFromInspect(context: Context, email: String, tempDirPath: String, isMergeMode: Boolean): Boolean {
        return try {
            val tempDir = File(tempDirPath)
            val dataFile = File(tempDir, "data.json")
            if (!dataFile.exists()) return false

            val dataObj = JSONObject(dataFile.readText())

            // Restore Images to App Storage
            val imagesDir = File(context.filesDir, "restored_images")
            imagesDir.mkdirs()
            val imageMap = mutableMapOf<String, String>() // oldUriOrIndex -> newUri

            val zipImagesDir = File(tempDir, "images")
            if (zipImagesDir.exists() && zipImagesDir.isDirectory) {
                zipImagesDir.listFiles()?.forEach { imgFile ->
                    val destFile = File(imagesDir, "restored_${System.currentTimeMillis()}_${imgFile.name}")
                    imgFile.copyTo(destFile, overwrite = true)
                    val newUriStr = Uri.fromFile(destFile).toString()
                    imageMap[imgFile.name] = newUriStr
                }
            }

            // Restore Profile
            if (dataObj.has("profile")) {
                val pObj = dataObj.getJSONObject("profile")
                val name = pObj.optString("name", "Guest")
                val photo = if (pObj.has("photoUri")) pObj.optString("photoUri") else null
                saveUserProfile(context, email, name, photo)
            }

            // Restore Zones
            val existingZones = loadZones(context, email).toMutableList()
            val restoredZones = mutableListOf<Zone>()
            if (dataObj.has("zones")) {
                val zArr = dataObj.getJSONArray("zones")
                for (i in 0 until zArr.length()) {
                    val obj = zArr.getJSONObject(i)
                    restoredZones.add(
                        Zone(
                            name = obj.optString("name", ""),
                            meta = obj.optString("meta", "0 items"),
                            image = obj.optInt("image", 0),
                            imageUri = if (obj.has("imageUri")) obj.optString("imageUri") else null
                        )
                    )
                }
            }

            // Restore Items
            val existingItems = loadItems(context, email).toMutableList()
            val restoredItems = mutableListOf<NookItem>()
            if (dataObj.has("items")) {
                val iArr = dataObj.getJSONArray("items")
                for (i in 0 until iArr.length()) {
                    val obj = iArr.getJSONObject(i)
                    val tagsList = mutableListOf<String>()
                    if (obj.has("tags")) {
                        val tArr = obj.getJSONArray("tags")
                        for (t in 0 until tArr.length()) tagsList.add(tArr.getString(t))
                    }
                    restoredItems.add(
                        NookItem(
                            name = obj.optString("name", ""),
                            time = obj.optString("time", "Just now"),
                            image = obj.optInt("image", 0),
                            zone = obj.optString("zone", ""),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", ""),
                            imageUri = if (obj.has("imageUri")) obj.optString("imageUri") else null,
                            tags = tagsList
                        )
                    )
                }
            }

            // Restore Activities
            val existingActivities = loadActivities(context, email).toMutableList()
            val restoredActivities = mutableListOf<ActivityEntry>()
            if (dataObj.has("activities")) {
                val aArr = dataObj.getJSONArray("activities")
                for (i in 0 until aArr.length()) {
                    val obj = aArr.getJSONObject(i)
                    restoredActivities.add(
                        ActivityEntry(
                            action = obj.optString("action", ""),
                            itemName = obj.optString("itemName", ""),
                            time = obj.optString("time", "Just now"),
                            zone = obj.optString("zone", ""),
                            image = obj.optInt("image", 0),
                            day = obj.optString("day", "Today"),
                            imageUri = if (obj.has("imageUri")) obj.optString("imageUri") else null
                        )
                    )
                }
            }

            // Restore Reminders
            val existingReminders = loadReminders(context, email).toMutableList()
            val restoredReminders = mutableListOf<Reminder>()
            if (dataObj.has("reminders")) {
                val rArr = dataObj.getJSONArray("reminders")
                for (i in 0 until rArr.length()) {
                    val obj = rArr.getJSONObject(i)
                    restoredReminders.add(
                        Reminder(
                            id = UUID.randomUUID().toString(), // Safe new UUID
                            title = obj.optString("title", ""),
                            note = obj.optString("note", ""),
                            timeMillis = obj.optLong("timeMillis", System.currentTimeMillis()),
                            timeText = obj.optString("timeText", "12:00 PM"),
                            dateText = obj.optString("dateText", "Today"),
                            relatedItemName = obj.optString("relatedItemName", ""),
                            relatedZoneName = obj.optString("relatedZoneName", ""),
                            isEnabled = obj.optBoolean("isEnabled", true)
                        )
                    )
                }
            }

            if (isMergeMode) {
                // Merge Mode: Append unique non-duplicate entries
                val finalZones = existingZones.toMutableList()
                restoredZones.forEach { rz ->
                    if (finalZones.none { it.name.equals(rz.name, ignoreCase = true) }) {
                        finalZones.add(rz)
                    }
                }
                val finalItems = existingItems.toMutableList()
                restoredItems.forEach { ri ->
                    if (finalItems.none { it.name.equals(ri.name, ignoreCase = true) && it.zone.equals(ri.zone, ignoreCase = true) }) {
                        finalItems.add(ri)
                    }
                }
                val finalActivities = (restoredActivities + existingActivities).distinctBy { "${it.action}_${it.itemName}_${it.time}" }
                val finalReminders = (existingReminders + restoredReminders).distinctBy { it.title }

                saveZones(context, email, finalZones)
                saveItems(context, email, finalItems)
                saveActivities(context, email, finalActivities)
                saveReminders(context, email, finalReminders)
            } else {
                // Replace Mode: Replace active user's data completely
                saveZones(context, email, restoredZones)
                saveItems(context, email, restoredItems)
                saveActivities(context, email, restoredActivities)
                saveReminders(context, email, restoredReminders)
            }

            tempDir.deleteRecursively()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
