package com.luckyzyx.luckytool.hook.core

import android.util.ArraySet
import com.luckyzyx.luckytool.data.AppVerInfo
import com.luckyzyx.luckytool.utils.safeOfNull
import kotlinx.serialization.json.Json

/**
 * 同形 legacy utils 的 YukiHookPrefsBridge.getAppVerInfo：
 * 从远程偏好读取模块 App 保存的宿主版本信息（按包名 key 存 StringSet + JSON）。
 */
fun Env.NonNullPrefs.getAppVerInfo(packName: String): AppVerInfo? {
    val set = getStringSet(packName, ArraySet())
    return if (set.isEmpty()) null else safeOfNull {
        Json.decodeFromString<AppVerInfo>(set.firstOrNull() ?: "")
    }
}
