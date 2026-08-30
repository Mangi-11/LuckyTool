package com.luckyzyx.luckytool.hook.scopes.launcher

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableDockerBackground : Hooker {
    override fun onHook() {
        //Source ScreenUtils
        "com.android.common.util.ScreenUtils".toClass().resolve().apply {
            firstMethod { name = "isSupportDockerExpandScreen" }.hook {
                replaceToTrue()
            }
            //OplusTaskHeaderView showSplitWindowIcon
//            firstMethod { name = "hasLargeDisplayFeatures" }.hook {
//                replaceToTrue()
//            }
        }
    }
}