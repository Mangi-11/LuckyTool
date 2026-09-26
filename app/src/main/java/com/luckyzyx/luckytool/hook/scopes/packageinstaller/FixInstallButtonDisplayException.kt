package com.luckyzyx.luckytool.hook.scopes.packageinstaller

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate
import java.security.SecureRandom

@Obfuscate
object FixInstallButtonDisplayException : YukiBaseHooker() {
    override fun onHook() {
        //Source ConfusedButton
        "com.android.packageinstaller.oplus.view.ConfusedButton".toClass().resolve().apply {
            firstMethod { name = "getAccessibilityViewId" }.hook {
                before {
                    firstMethod { name = "setCts" }.of(instance).invoke(true)
                    firstField { type = SecureRandom::class }.of(instance).set(SecureRandom())
                }
            }
            firstMethodOrNull { name = "getText" }?.hook {
                before {
                    firstMethod { name = "setCts" }.of(instance).invoke(true)
                    firstField { type = SecureRandom::class }.of(instance).set(SecureRandom())
                }
            }
        }
        //Source ConfusedTextView
        "com.android.packageinstaller.oplus.view.ConfusedTextView".toClass().resolve().apply {
            firstMethod { name = "getAccessibilityViewId" }.hook {
                before {
                    firstMethod { name = "setCts" }.of(instance).invoke(true)
                    firstField { type = SecureRandom::class }.of(instance).set(SecureRandom())
                }
            }
            firstMethodOrNull { name = "getText" }?.hook {
                before {
                    firstMethod { name = "setCts" }.of(instance).invoke(true)
                    firstField { type = SecureRandom::class }.of(instance).set(SecureRandom())
                }
            }
        }
    }
}