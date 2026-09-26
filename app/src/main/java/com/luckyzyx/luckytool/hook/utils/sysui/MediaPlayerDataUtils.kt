package com.luckyzyx.luckytool.hook.utils.sysui

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class MediaPlayerDataUtils(val classLoader: ClassLoader?) {
    val clazz = VariousClass(
        "com.oplus.systemui.qs.media.OplusQsMediaCarouselController\$MediaPlayerData",  //C13.0 C13.1
        "com.oplusos.systemui.media.OplusMediaControllerImpl\$MediaPlayerData",  //C13.2
        "com.oplus.systemui.media.OplusMediaControllerImpl\$MediaPlayerData"  //C14
    ).load(classLoader)

    fun getMediaDataStatus(): Any? {
        val mediaPlayerData = clazz.resolve().firstField { name = "INSTANCE" }.get() ?: return null
        val firstActiveMediaKey = mediaPlayerData.asResolver().let {
            (it.firstMethodOrNull { name = "getFirstActiveMediaSortKey" }
                ?: it.firstMethod { name = "firstActiveMedia" }).invoke()
        } ?: return null
        val mediaDataKey = mediaPlayerData.asResolver().firstMethod { name = "getMediaDataKey" }
            .invoke(firstActiveMediaKey) ?: return null
        return firstActiveMediaKey.asResolver().firstMethod { name = "getData" }.invoke()
    }
}