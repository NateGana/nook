package com.example.nook

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.nook.data.*
import com.example.nook.ui.components.*
import com.example.nook.ui.screens.*
import com.example.nook.ui.theme.NookCream
import com.example.nook.ui.theme.NookTheme

enum class Screen {
    GetStarted, LoginLanding, SignIn, SignUp, Home,
    Zones, ZoneDetail, AddCamera, AddForm, ItemSaved, Activity, Settings,
    NotificationSettings, Reminders, BackupRestore, HelpCenter, ContactUs
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NookApp() }
    }
}

@Composable
fun NookApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("nook_prefs", android.content.Context.MODE_PRIVATE) }
    val systemIsDark = isSystemInDarkTheme()
    var isDarkMode by remember {
        mutableStateOf(prefs.getBoolean("is_dark_mode", systemIsDark))
    }
    var isFirstInstall by remember { mutableStateOf(prefs.getBoolean("is_first_install", true)) }
    var isLoggedIn by remember { mutableStateOf(prefs.getBoolean("is_logged_in", false)) }
    var capturedImageUri by remember { mutableStateOf<String?>(null) }

    var userEmail by remember { mutableStateOf(prefs.getString("user_email", "guest101@gmail.com") ?: "guest101@gmail.com") }

    val initialProfile = remember(userEmail) { NookRepository.getUserProfile(context, userEmail) }
    var userName by remember { mutableStateOf(initialProfile.first) }
    var userProfileImageUri by remember { mutableStateOf(initialProfile.second) }

    val zonesList = remember { mutableStateListOf<Zone>() }
    val itemsList = remember { mutableStateListOf<NookItem>() }
    val activitiesList = remember { mutableStateListOf<ActivityEntry>() }
    var selectedZone by remember { mutableStateOf<Zone?>(null) }

    val users = remember {
        mutableStateListOf<Pair<String, String>>().apply {
            addAll(NookRepository.loadUsers(context))
        }
    }

    LaunchedEffect(userEmail) {
        zonesList.clear()
        zonesList.addAll(NookRepository.loadZones(context, userEmail))

        itemsList.clear()
        itemsList.addAll(NookRepository.loadItems(context, userEmail))

        activitiesList.clear()
        activitiesList.addAll(NookRepository.loadActivities(context, userEmail))

        val (name, photo) = NookRepository.getUserProfile(context, userEmail)
        userName = name
        userProfileImageUri = photo
    }

    val initialScreen = when {
        isFirstInstall -> Screen.GetStarted
        isLoggedIn -> Screen.Home
        else -> Screen.LoginLanding
    }

    var screen by remember { mutableStateOf(initialScreen) }

    androidx.activity.compose.BackHandler(enabled = screen != Screen.Home && screen != Screen.LoginLanding && screen != Screen.SignIn && screen != Screen.SignUp && screen != Screen.GetStarted) {
        screen = when (screen) {
            Screen.NotificationSettings, Screen.Reminders, Screen.BackupRestore, Screen.HelpCenter, Screen.ContactUs, Screen.Settings -> Screen.Settings
            Screen.ZoneDetail -> Screen.Zones
            Screen.AddForm -> Screen.AddCamera
            Screen.AddCamera -> Screen.Home
            Screen.Zones, Screen.Activity -> Screen.Home
            else -> Screen.Home
        }
    }

    var showNotificationDialog by remember { mutableStateOf(false) }
    var lastReadActivityCount by remember { mutableStateOf(0) }
    val hasUnreadNotifications = activitiesList.size > lastReadActivityCount
    val handleNotificationClick = {
        showNotificationDialog = true
        lastReadActivityCount = activitiesList.size
    }

    val withBottomBar = setOf(Screen.Home, Screen.Zones, Screen.Activity, Screen.Settings)
    val currentTab = when (screen) {
        Screen.Zones -> Tab.Zones
        Screen.ZoneDetail -> Tab.Zones
        Screen.Activity -> Tab.Activity
        Screen.Settings -> Tab.Settings
        else -> Tab.Home
    }

    NookTheme(darkTheme = isDarkMode) {
        if (showNotificationDialog) {
            RecentNotificationsDialog(
                activities = activitiesList,
                onDismiss = { showNotificationDialog = false },
                onViewAllActivity = {
                    screen = Screen.Activity
                },
                onClearAll = {
                    activitiesList.clear()
                    NookRepository.saveActivities(context, userEmail, activitiesList)
                },
                onDeleteNotification = { entry ->
                    activitiesList.remove(entry)
                    NookRepository.saveActivities(context, userEmail, activitiesList)
                }
            )
        }

        Surface(modifier = Modifier.fillMaxSize(), color = NookCream) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when (screen) {
                        Screen.GetStarted -> GetStartedScreen {
                            val editor = prefs.edit()
                            editor.putBoolean("is_first_install", false)
                            editor.apply()
                            isFirstInstall = false
                            screen = Screen.LoginLanding
                        }
                        Screen.LoginLanding -> LoginLandingScreen(
                            onLogin = { screen = Screen.SignIn },
                            onSignUp = { screen = Screen.SignUp }
                        )
                        Screen.SignIn -> SignInScreen(
                            onLogin = { email, password ->
                                val foundUser = users.find { it.first.equals(email, ignoreCase = true) && it.second == password }
                                if (foundUser != null) {
                                    val safeEmail = foundUser.first
                                    if (isLoggedIn) {
                                        NookRepository.saveZones(context, userEmail, zonesList)
                                        NookRepository.saveItems(context, userEmail, itemsList)
                                        NookRepository.saveActivities(context, userEmail, activitiesList)
                                    }

                                    val editor = prefs.edit()
                                    editor.putBoolean("is_logged_in", true)
                                    editor.putString("user_email", safeEmail)
                                    editor.apply()

                                    userEmail = safeEmail
                                    val (loadedName, loadedPhoto) = NookRepository.getUserProfile(context, safeEmail)
                                    userName = loadedName
                                    userProfileImageUri = loadedPhoto

                                    zonesList.clear()
                                    zonesList.addAll(NookRepository.loadZones(context, safeEmail))

                                    itemsList.clear()
                                    itemsList.addAll(NookRepository.loadItems(context, safeEmail))

                                    activitiesList.clear()
                                    activitiesList.addAll(NookRepository.loadActivities(context, safeEmail))

                                    isLoggedIn = true
                                    screen = Screen.Home
                                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onSignUp = { screen = Screen.SignUp }
                        )
                        Screen.SignUp -> SignUpScreen(
                            onSignUp = { email, password ->
                                if (email.isNotEmpty() && password.isNotEmpty()) {
                                    if (users.any { it.first.equals(email, ignoreCase = true) }) {
                                        Toast.makeText(context, "Account already exists!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        if (isLoggedIn) {
                                            NookRepository.saveZones(context, userEmail, zonesList)
                                            NookRepository.saveItems(context, userEmail, itemsList)
                                            NookRepository.saveActivities(context, userEmail, activitiesList)
                                        }

                                        users.add(email to password)
                                        NookRepository.saveUsers(context, users)

                                        val editor = prefs.edit()
                                        editor.putBoolean("is_logged_in", true)
                                        editor.putString("user_email", email)
                                        editor.apply()

                                        userEmail = email
                                        val defaultName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                                        userName = defaultName
                                        userProfileImageUri = null
                                        NookRepository.saveUserProfile(context, email, defaultName, null)

                                        zonesList.clear()
                                        itemsList.clear()
                                        activitiesList.clear()

                                        NookRepository.saveZones(context, email, zonesList)
                                        NookRepository.saveItems(context, email, itemsList)
                                        NookRepository.saveActivities(context, email, activitiesList)

                                        isLoggedIn = true
                                        screen = Screen.Home
                                        Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onLogin = { screen = Screen.SignIn }
                        )
                        Screen.Home -> HomeScreen(
                            items = itemsList,
                            zones = zonesList,
                            userName = userName,
                            userProfileImageUri = userProfileImageUri,
                            hasUnreadNotifications = hasUnreadNotifications,
                            onProfileClick = { screen = Screen.Settings },
                            onNotificationClick = handleNotificationClick,
                            onUpdateItem = { oldItem, updatedItem ->
                                val index = itemsList.indexOf(oldItem)
                                if (index != -1) {
                                    itemsList[index] = updatedItem
                                    activitiesList.add(0, ActivityEntry("Checked", updatedItem.name, "Just now", updatedItem.zone))
                                    NookRepository.saveItems(context, userEmail, itemsList)
                                    NookRepository.saveActivities(context, userEmail, activitiesList)
                                }
                            },
                            onDeleteItem = { itemToDelete ->
                                itemsList.remove(itemToDelete)
                                activitiesList.add(0, ActivityEntry("Removed", itemToDelete.name, "Just now", itemToDelete.zone))
                                NookRepository.saveItems(context, userEmail, itemsList)
                                NookRepository.saveActivities(context, userEmail, activitiesList)
                            }
                        )
                        Screen.Zones -> ZonesScreen(
                            zones = zonesList.map { zone ->
                                val count = itemsList.count { it.zone == zone.name }
                                zone.copy(meta = "$count ${if (count == 1) "item" else "items"} • Just now")
                            },
                            hasUnreadNotifications = hasUnreadNotifications,
                            onNotificationClick = handleNotificationClick,
                            onAddZone = { name, imageUri ->
                                if (zonesList.none { it.name.equals(name, ignoreCase = true) }) {
                                    zonesList.add(
                                        Zone(
                                            name = name,
                                            meta = "0 items • Just now",
                                            image = 0,
                                            imageUri = imageUri
                                        )
                                    )
                                    activitiesList.add(0, ActivityEntry("Added", "Zone '$name'", "Just now", name))
                                    NookRepository.saveZones(context, userEmail, zonesList)
                                    NookRepository.saveActivities(context, userEmail, activitiesList)
                                }
                            },
                            onSelectZone = { zone ->
                                selectedZone = zone
                                screen = Screen.ZoneDetail
                            }
                        )
                        Screen.ZoneDetail -> ZoneDetailScreen(
                            zone = selectedZone,
                            items = itemsList.filter { it.zone == selectedZone?.name },
                            allZones = zonesList,
                            onUpdateZone = { updatedZone ->
                                val oldName = selectedZone?.name
                                val index = zonesList.indexOfFirst { it.name == oldName }
                                if (index != -1) {
                                    zonesList[index] = updatedZone
                                    selectedZone = updatedZone
                                    if (oldName != null && oldName != updatedZone.name) {
                                        itemsList.indices.filter { itemsList[it].zone == oldName }.forEach { i ->
                                            itemsList[i] = itemsList[i].copy(zone = updatedZone.name)
                                        }
                                    }
                                    activitiesList.add(0, ActivityEntry("Checked", "Zone '${updatedZone.name}'", "Just now", updatedZone.name))
                                    NookRepository.saveZones(context, userEmail, zonesList)
                                    NookRepository.saveItems(context, userEmail, itemsList)
                                    NookRepository.saveActivities(context, userEmail, activitiesList)
                                }
                            },
                            onDeleteZone = { zoneToDelete ->
                                zonesList.removeIf { it.name == zoneToDelete.name }
                                itemsList.removeIf { it.zone == zoneToDelete.name }
                                activitiesList.add(0, ActivityEntry("Removed", "Zone '${zoneToDelete.name}'", "Just now", zoneToDelete.name))
                                NookRepository.saveZones(context, userEmail, zonesList)
                                NookRepository.saveItems(context, userEmail, itemsList)
                                NookRepository.saveActivities(context, userEmail, activitiesList)
                                selectedZone = null
                                screen = Screen.Zones
                            },
                            onUpdateItem = { oldItem, updatedItem ->
                                val index = itemsList.indexOf(oldItem)
                                if (index != -1) {
                                    itemsList[index] = updatedItem
                                    activitiesList.add(0, ActivityEntry("Checked", updatedItem.name, "Just now", updatedItem.zone))
                                    NookRepository.saveItems(context, userEmail, itemsList)
                                    NookRepository.saveActivities(context, userEmail, activitiesList)
                                }
                            },
                            onDeleteItem = { itemToDelete ->
                                itemsList.remove(itemToDelete)
                                activitiesList.add(0, ActivityEntry("Removed", itemToDelete.name, "Just now", itemToDelete.zone))
                                NookRepository.saveItems(context, userEmail, itemsList)
                                NookRepository.saveActivities(context, userEmail, activitiesList)
                            },
                            onBack = { screen = Screen.Zones }
                        )
                        Screen.AddCamera -> AddItemCameraScreen(
                            onClose = { screen = Screen.Home },
                            onImageCaptured = { uri ->
                                capturedImageUri = uri.toString()
                                screen = Screen.AddForm
                            }
                        )
                        Screen.AddForm -> AddItemFormScreen(
                            onBack = { screen = Screen.AddCamera },
                            imageUri = capturedImageUri,
                            onChangePhoto = { screen = Screen.AddCamera },
                            zones = zonesList,
                            onAddZone = { name, imageUri ->
                                if (zonesList.none { it.name.equals(name, ignoreCase = true) }) {
                                    zonesList.add(
                                        Zone(
                                            name = name,
                                            meta = "0 items • Just now",
                                            image = 0,
                                            imageUri = imageUri
                                        )
                                    )
                                    activitiesList.add(0, ActivityEntry("Added", "Zone '$name'", "Just now", name))
                                    NookRepository.saveZones(context, userEmail, zonesList)
                                    NookRepository.saveActivities(context, userEmail, activitiesList)
                                }
                            },
                            onSaveItem = { name, category, zoneName, desc, imageUri ->
                                itemsList.add(
                                    NookItem(
                                        name = name,
                                        time = "Just now",
                                        image = 0,
                                        zone = zoneName,
                                        description = desc,
                                        category = category,
                                        imageUri = imageUri
                                    )
                                )
                                activitiesList.add(0, ActivityEntry("Added", name, "Just now", zoneName))
                                NookRepository.saveItems(context, userEmail, itemsList)
                                NookRepository.saveActivities(context, userEmail, activitiesList)
                                screen = Screen.ItemSaved
                            }
                        )
                        Screen.ItemSaved -> ItemSavedScreen({ screen = Screen.Home }) { screen = Screen.Home }
                        Screen.Activity -> ActivityScreen(
                            activities = activitiesList,
                            hasUnreadNotifications = hasUnreadNotifications,
                            onNotificationClick = handleNotificationClick,
                            onDeleteActivity = { entry ->
                                activitiesList.remove(entry)
                                NookRepository.saveActivities(context, userEmail, activitiesList)
                            },
                            onClearAllActivities = {
                                activitiesList.clear()
                                NookRepository.saveActivities(context, userEmail, activitiesList)
                            }
                        )
                        Screen.Settings -> SettingsScreen(
                            userName = userName,
                            userEmail = userEmail,
                            userProfileImageUri = userProfileImageUri,
                            isDarkMode = isDarkMode,
                            onDarkModeToggle = { enabled ->
                                isDarkMode = enabled
                                val editor = prefs.edit()
                                editor.putBoolean("is_dark_mode", enabled)
                                editor.apply()
                            },
                            onUpdateProfile = { name, imageUri ->
                                userName = name
                                userProfileImageUri = imageUri
                                NookRepository.saveUserProfile(context, userEmail, name, imageUri)
                            },
                            onChangePassword = { oldPassword, newPassword ->
                                val userIdx = users.indexOfFirst { it.first.equals(userEmail, ignoreCase = true) }
                                if (userIdx != -1 && users[userIdx].second == oldPassword) {
                                    users[userIdx] = userEmail to newPassword
                                    NookRepository.saveUsers(context, users)
                                    true
                                } else {
                                    false
                                }
                            },
                            onNavigateNotifications = { screen = Screen.NotificationSettings },
                            onNavigateReminders = { screen = Screen.Reminders },
                            onNavigateBackupRestore = { screen = Screen.BackupRestore },
                            onNavigateHelpCenter = { screen = Screen.HelpCenter },
                            onNavigateContactUs = { screen = Screen.ContactUs },
                            onLogout = {
                                NookRepository.saveZones(context, userEmail, zonesList)
                                NookRepository.saveItems(context, userEmail, itemsList)
                                NookRepository.saveActivities(context, userEmail, activitiesList)

                                val editor = prefs.edit()
                                editor.putBoolean("is_logged_in", false)
                                editor.apply()

                                isLoggedIn = false
                                zonesList.clear()
                                itemsList.clear()
                                activitiesList.clear()
                                screen = Screen.LoginLanding
                                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Screen.NotificationSettings -> NotificationSettingsScreen(
                            userEmail = userEmail,
                            onBack = { screen = Screen.Settings }
                        )
                        Screen.Reminders -> RemindersScreen(
                            userEmail = userEmail,
                            items = itemsList,
                            zones = zonesList,
                            onBack = { screen = Screen.Settings }
                        )
                        Screen.BackupRestore -> BackupRestoreScreen(
                            userEmail = userEmail,
                            onBack = { screen = Screen.Settings },
                            onDataRestored = {
                                zonesList.clear()
                                zonesList.addAll(NookRepository.loadZones(context, userEmail))

                                itemsList.clear()
                                itemsList.addAll(NookRepository.loadItems(context, userEmail))

                                activitiesList.clear()
                                activitiesList.addAll(NookRepository.loadActivities(context, userEmail))

                                val (name, photo) = NookRepository.getUserProfile(context, userEmail)
                                userName = name
                                userProfileImageUri = photo
                            }
                        )
                        Screen.HelpCenter -> HelpCenterScreen(
                            onBack = { screen = Screen.Settings }
                        )
                        Screen.ContactUs -> ContactUsScreen(
                            onBack = { screen = Screen.Settings }
                        )
                    }
                }
                if (screen in withBottomBar) {
                    NookBottomBar(
                        current = currentTab,
                        onTab = { tab ->
                            screen = when (tab) {
                                Tab.Home -> Screen.Home
                                Tab.Zones -> Screen.Zones
                                Tab.Activity -> Screen.Activity
                                Tab.Settings -> Screen.Settings
                            }
                        },
                        onAdd = { screen = Screen.AddCamera },
                    )
                }
            }
        }
    }
}
