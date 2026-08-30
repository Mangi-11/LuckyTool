package com.luckyzyx.luckytool.hook.scopes.speechassist

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ForceEnableAISpeechAssistCall : Hooker {
    override fun onHook() {
        //Source AiCallCommonBean
        "com.heytap.speechassist.aicall.setting.config.AiCallCommonBean".toClass().resolve().apply {
            firstMethod { name = "getSupportAiCall" }.hook {
                replaceToTrue()
            }
            firstMethod { name = "getSupportAiCallV2" }.hook {
                replaceToTrue()
            }
        }
    }
}