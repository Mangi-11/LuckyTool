package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.pictorial.RemoveImageSaveWaterMark
import com.luckyzyx.luckytool.hook.scopes.pictorial.RemoveVideoSaveWaterMark
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs

@Obfuscate
object HookPictorial : Hooker {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            //移除图片保存水印
            if (prefs(ModulePrefs).getBoolean("remove_image_save_watermark", false)) {
                loadHooker(RemoveImageSaveWaterMark(dexKitBridge))
            }
        }
        //移除视频保存水印
        if (prefs(ModulePrefs).getBoolean("remove_video_save_watermark", false)) {
            loadHooker(RemoveVideoSaveWaterMark)
        }
    }
}