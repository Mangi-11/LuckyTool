package com.luckyzyx.luckytool.hook.scopes.gallery

import android.text.Spanned
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class GalleryWaterMarkWordDialog(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode
        if (osCode >= 30) loadHooker(WaterMarkWordDialog(dexKitBridge))
        else loadHooker(WaterMarkWordLimit(dexKitBridge))
    }

    @Obfuscate
    class WaterMarkWordDialog(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            //Source CustomInfoEditDialogHelper

        }
    }

    @Obfuscate
    class WaterMarkWordLimit(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            //Source CustomInfoEditDialogHelper -> picture_editor_text_watermark_character_limit_toast
            dexKitBridge.findMethod {
                matcher {
                    name("filter")
                    paramTypes(
                        CharSequence::class.java, Int::class.java, Int::class.java,
                        Spanned::class.java, Int::class.java, Int::class.java
                    )
                    returnType(CharSequence::class.java)
                    usingNumbers(0, 1, 2)
                    usingStrings("")
                }
            }.apply {
                checkDataList("RemoveGalleryWaterMarkWordLimit")
                single().className.toClass().resolve().apply {
                    firstMethod {
                        name = "filter"
                        parameters(
                            CharSequence::class, Int::class, Int::class,
                            Spanned::class, Int::class, Int::class
                        )
                        returnType = CharSequence::class
                    }.hook {
                        before {
                            result = args().first().cast<CharSequence>() ?: return@before
                        }
                    }
                }
            }
        }
    }
}