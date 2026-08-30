package org.lsposed.corepatch

import com.luckyzyx.luckytool.hook.core.Env
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.corepatch.App.Companion.rwPrefs

object Config {
    //上游功能键（保留原值，便于对照上游文档/未来 UI 扩展）
    const val BYPASS_DOWNGRADE = "downgrade"
    const val BYPASS_VERIFICATION = "bypass_verification"
    const val BYPASS_RESOURCE_ARSC_RESTRICTIONS = "bypass_resource_arsc_restrictions"
    const val BYPASS_DIGEST = "bypass_digest"
    const val BYPASS_EXACT_SIGNATURE_MATCH = "bypass_exact_sig_match"
    const val USE_PREVIOUS_SIGNATURES = "use_previous_signatures"
    const val ALLOW_HIDDEN_APIS_FOR_SYSTEM_APPS = "allow_hidden_apis_for_system_apps"
    const val BYPASS_SHARED_USER = "bypass_shared_user"
    const val DISABLE_VERIFICATION_AGENT = "disable_verification_agent"
    const val BYPASS_BLOCK = "bypass_block"

    //LuckyTool ModulePrefs 键映射（沿用旧版 CorePatch 的键名与默认值）
    private const val KEY_DOWNGRADE = "downgrade" //旧默认 true
    private const val KEY_AUTHCREAK = "authcreak" //旧开关：验证/digest/arsc 全链
    private const val KEY_DIGEST = "digestCreak" //旧默认 true
    private const val KEY_EXACT_SIG = "exactSigCheck"
    private const val KEY_PREV_SIG = "UsePreSig"
    private const val KEY_BLOCK = "bypassBlock"
    private const val KEY_DISABLE_AGENT = "disableVerificationAgent"
    private const val KEY_SHARED_USER = "sharedUser"
    private const val KEY_ALLOW_HIDDEN_APIS = "allow_hidden_apis_for_system_apps"

    /** 宿主进程内读 LuckyTool 的 ModulePrefs（远程偏好链路） */
    private val prefs get() = Env.prefs(ModulePrefs)

    private val allConfig = arrayOf(
        KEY_DOWNGRADE,
        KEY_AUTHCREAK,
        KEY_DIGEST,
        KEY_EXACT_SIG,
        KEY_PREV_SIG,
        KEY_BLOCK,
        KEY_DISABLE_AGENT,
        KEY_SHARED_USER,
        KEY_ALLOW_HIDDEN_APIS
    )

    fun printAllConfig() {
        allConfig.forEach {
            XposedHelper.log("$it: ${prefs.getBoolean(it, false)}")
        }
    }

    fun isBypassDowngradeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DOWNGRADE, true)
    }

    fun isBypassVerificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTHCREAK, false)
    }

    /** 旧 authcreak 门控 AssetManager.containsAllocatedTable，沿用同一开关 */
    fun isBypassResourceArscRestrictionsEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTHCREAK, false)
    }

    fun isBypassDigestEnabled(): Boolean {
        return prefs.getBoolean(KEY_DIGEST, true) || prefs.getBoolean(KEY_AUTHCREAK, false)
    }

    fun isBypassExactSignatureMatch(): Boolean {
        return prefs.getBoolean(KEY_EXACT_SIG, false)
    }

    fun isUsePreviousSignaturesEnabled(): Boolean {
        return prefs.getBoolean(KEY_PREV_SIG, false)
    }

    fun isAllowHiddenApisForSystemAppsEnabled(): Boolean {
        return prefs.getBoolean(KEY_ALLOW_HIDDEN_APIS, false)
    }

    fun isBypassSharedUserEnabled(): Boolean {
        return prefs.getBoolean(KEY_SHARED_USER, false)
    }

    fun isDisableVerificationAgentEnabled(): Boolean {
        return prefs.getBoolean(KEY_DISABLE_AGENT, false)
    }

    fun isBypassBlockEnabled(): Boolean {
        return prefs.getBoolean(KEY_BLOCK, false)
    }

    fun getConfig(key: String): Boolean {
        return rwPrefs?.getBoolean(key, false) ?: false
    }

    fun setConfig(key: String, value: Boolean) {
        rwPrefs?.edit()?.putBoolean(key, value)?.apply()
    }
}