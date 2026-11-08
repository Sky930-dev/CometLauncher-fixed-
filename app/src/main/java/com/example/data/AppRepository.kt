package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.model.AppCategory
import com.example.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        val selfPackage = context.packageName

        val apps = resolveInfos
            .filter { it.activityInfo != null && it.activityInfo.packageName != selfPackage }
            .map { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                val packageName = activityInfo.packageName
                val activityName = activityInfo.name
                val id = "$packageName/$activityName"
                val label = resolveInfo.loadLabel(packageManager)?.toString()
                    ?: activityInfo.name

                val drawable = resolveInfo.loadIcon(packageManager)
                val bitmap = drawableToBitmap(drawable)

                val isSystem = (activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val category = detectCategory(packageName, label, activityInfo.applicationInfo)

                AppInfo(
                    id = id,
                    packageName = packageName,
                    activityName = activityName,
                    label = label,
                    iconBitmap = bitmap,
                    category = category,
                    isSystemApp = isSystem
                )
            }
            .sortedBy { it.label.lowercase() }

        apps
    }

    fun launchApp(app: AppInfo): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)
                ?: Intent().apply {
                    component = ComponentName(app.packageName, app.activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun openAppDetails(app: AppInfo) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", app.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun uninstallApp(app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", app.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 160
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 160

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun detectCategory(packageName: String, label: String, appInfo: ApplicationInfo): AppCategory {
        val lowerPkg = packageName.lowercase()
        val lowerLabel = label.lowercase()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_SOCIAL,
                ApplicationInfo.CATEGORY_NEWS -> return AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.ESSENTIALS
            }
        }

        // Keywords detection
        return when {
            lowerPkg.contains("game") || lowerLabel.contains("game") || lowerPkg.contains("play.games") -> AppCategory.GAMES
            lowerPkg.contains("music") || lowerPkg.contains("spotify") || lowerPkg.contains("media") ||
                    lowerPkg.contains("player") || lowerPkg.contains("youtube") || lowerPkg.contains("video") ||
                    lowerPkg.contains("camera") || lowerPkg.contains("photos") || lowerPkg.contains("gallery") -> AppCategory.MEDIA
            lowerPkg.contains("message") || lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") ||
                    lowerPkg.contains("twitter") || lowerPkg.contains("facebook") || lowerPkg.contains("instagram") ||
                    lowerPkg.contains("tiktok") || lowerPkg.contains("social") || lowerPkg.contains("mail") ||
                    lowerPkg.contains("gmail") || lowerPkg.contains("chat") -> AppCategory.SOCIAL
            lowerPkg.contains("dialer") || lowerPkg.contains("phone") || lowerPkg.contains("contact") ||
                    lowerPkg.contains("browser") || lowerPkg.contains("chrome") || lowerPkg.contains("clock") ||
                    lowerPkg.contains("calendar") || lowerPkg.contains("settings") -> AppCategory.ESSENTIALS
            else -> AppCategory.TOOLS
        }
    }
}
