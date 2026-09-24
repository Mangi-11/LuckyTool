package com.luckyzyx.luckytool.hook.scopes.launcher

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object UnlockTaskLocks : YukiBaseHooker() {
    override fun onHook() {
        //Source AppLockModel
        "com.oplus.quickstep.applock.AppLockModel".toClassOrNull()?.resolve()?.apply {
            firstFieldOrNull { name = "noDefaultLockedAppLimit" }?.let {
                firstMethod { name = "initData" }.hook {
                    after {
                        it.copy().of(instance).set(999)
                    }
                }
                firstMethod { name = "updateNoDefaultLockAppLimit" }.hook {
                    after {
                        it.copy().of(instance).set(999)
                    }
                }
                return
            }
        }

        //Source OplusLockManager
        VariousClass(
            "com.coloros.quickstep.applock.ColorLockManager",
            "com.oplus.quickstep.applock.OplusLockManager"
        ).toClass().resolve().apply {
            firstConstructor { parameters(Context::class) }.hook {
                after {
                    firstField { name = "mLockAppLimit" }.of(instance).set(999)
                }
            }
        }
    }
}