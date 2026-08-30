package com.luckyzyx.luckytool.hook.scopes.camera

import android.net.Uri
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class CustomCameraOpenGalleryByDefault(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        val gallery = prefs(ModulePrefs).getString("custom_camera_open_gallery_by_default", "")
        if (gallery.isBlank()) return

        //Source GalleryUtil
        dexKitBridge.findClass {
            matcher {
                addFieldForType(Uri::class.java)
                addFieldForType(Boolean::class.java)
                addMethod { paramCount(0);returnType(String::class.java) }
                usingStrings("content://com.color.provider.removableapp", "removableapp")
            }
        }.apply {
            checkDataList("CustomCameraOpenGalleryByDefault Clazz")
            findMethod {
                matcher {
                    paramCount(0)
                    returnType(String::class.java)
                    usingStrings("com.oplus.gallery.base")
                }
            }.apply {
                checkDataList("CustomCameraOpenGalleryByDefault Method")
                single().className.toClass().resolve().apply {
                    firstMethod {
                        name = single().methodName
                        emptyParameters()
                        returnType = String::class
                    }.hook {
                        before {
                            if (gallery.isNotBlank()) result = gallery
                        }
                    }
                }
            }
        }
    }
}