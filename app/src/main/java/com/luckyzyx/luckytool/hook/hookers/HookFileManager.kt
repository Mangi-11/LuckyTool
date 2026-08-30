package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.scopes.filemanager.RemoveWordLimitForCompressFiles
import com.luckyzyx.luckytool.hook.scopes.filemanager.RemoveWordLimitForLabelNameFiles
import com.luckyzyx.luckytool.hook.scopes.filemanager.RemoveWordLimitForSavingFiles
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookFileManager : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            //移除文件保存字数限制
            if (prefs(ModulePrefs).getBoolean("remove_word_limit_for_saving_files", false)) {
                if (osCode >= 37) loadHooker(RemoveWordLimitForSavingFiles(dexKitBridge))
            }
            //移除压缩文件字数限制
            if (prefs(ModulePrefs).getBoolean("remove_word_limit_for_compress_files", false)) {
                if (osCode >= 37) loadHooker(RemoveWordLimitForCompressFiles(dexKitBridge))
            }
            //移除重命名文件字数限制
            if (prefs(ModulePrefs).getBoolean("remove_word_limit_for_label_name_files", false)) {
                if (osCode >= 37) loadHooker(RemoveWordLimitForLabelNameFiles(dexKitBridge))
            }
        }
    }
}