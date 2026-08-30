package com.luckyzyx.luckytool.hook.scopes.pictorial

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.VagueType
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge
import java.io.File

@Obfuscate
class RemoveImageSaveWaterMark(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Search ImageSaveManager
        //Search getWaterMaskBitmap -> standard_water_mask_template / high_quality_water_mask_template
        dexKitBridge.findClass {
            matcher {
                fields {
                    addForType(File::class.java)
                    addForType(Handler::class.java)
                    addForType(Long::class.java)
                    addForType(Boolean::class.java)
                    addForType(String::class.java)
                }
                methods {
                    add { returnType(Handler::class.java) }
                    add { returnType(Bitmap::class.java) }
                    add { returnType(Boolean::class.java) }
                    add { paramTypes(Context::class.java) }
                    add { paramCount(5);returnType(Bitmap::class.java) }
                    add { paramTypes("com.heytap.pictorial.core.bean.BasePictorialData") }
                }
            }
        }.apply {
            checkDataList("RemoveImageSaveWaterMark")
            single().name.toClass().resolve().apply {
                firstMethod {
                    parameters(Boolean::class, VagueType, Bitmap::class, Boolean::class)
                    returnType = Bitmap::class
                }.hook {
                    after {
                        result = args(2).cast<Bitmap>() ?: return@after
                    }
                }
            }
        }
    }
}