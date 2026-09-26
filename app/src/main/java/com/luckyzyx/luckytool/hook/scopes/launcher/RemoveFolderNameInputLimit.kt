package com.luckyzyx.luckytool.hook.scopes.launcher

import android.text.Editable
import android.text.TextWatcher
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveFolderNameInputLimit : YukiBaseHooker() {
    override fun onHook() {
        //Source OplusFolder
        "com.android.launcher3.folder.OplusFolder".toClass().resolve().apply {
            firstConstructor().hook {
                after {
                    firstField { type = TextWatcher::class }.of(instance).set(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {

                        }

                        override fun beforeTextChanged(
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) {

                        }

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
                        ) {

                        }
                    })
                }
            }
        }
    }
}