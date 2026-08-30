package com.luckyzyx.luckytool.hook.scopes.games

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.util.SparseIntArray
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class CompetitionModeSound(val dexKitBridge: DexKitBridge) : Hooker {
    val key = "remove_competition_mode_sound"
    override fun onHook() {
        //Source SoundPoolPlayManager -> competition_mode_sound
        dexKitBridge.findClass {
            matcher {
                fields {
                    addForType(Context::class.java)
                    addForType(Boolean::class.java)
                    addForType(SoundPool::class.java)
                    addForType(AudioManager::class.java)
                    addForType(SparseIntArray::class.java)
                }
                methods {
                    add {
                        paramCount(0)
                        returnType(Void.TYPE)
                    }
                    add {
                        paramTypes(Int::class.java)
                        returnType(Void.TYPE)
                    }
                }
            }
        }.apply {
            checkDataList("CompetitionModeSound")
            single().name.toClass().resolve().apply {
                method { parameters(Int::class) }.hookAll {
                    before {
                        if (args().first().int() == 9) resultNull()
                    }
                }
            }
        }
    }
}