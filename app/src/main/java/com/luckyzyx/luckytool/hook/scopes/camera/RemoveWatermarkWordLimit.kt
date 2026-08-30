package com.luckyzyx.luckytool.hook.scopes.camera

import android.text.Spanned
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.SDK
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveWatermarkWordLimit(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source CameraSubSettingFragment -> camera_namelength_outofrange -> filter
        //Source CameraSloganSettingFragment -> camera_namelength_outofrange -> filter
        dexKitBridge.findMethod {
            matcher {
                name("filter")
                paramTypes(
                    CharSequence::class.java, Int::class.java, Int::class.java,
                    Spanned::class.java, Int::class.java, Int::class.java
                )
                returnType(CharSequence::class.java)
                usingStrings("")
                addInvoke {
                    paramCount(2..3)
                    returnType(Void.TYPE)
                }
            }
        }.apply {
            val onlyOne = SDK >= A13
            checkDataList("RemoveWatermarkWordLimit", onlyOne)
            if (onlyOne.not() && size == 2) {
                forEach {
                    it.className.toClass().resolve().apply {
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
            } else {
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



