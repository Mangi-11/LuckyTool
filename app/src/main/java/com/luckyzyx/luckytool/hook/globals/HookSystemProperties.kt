package com.luckyzyx.luckytool.hook.globals

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class HookSystemProperties(private val props: Map<String, Any>) : Hooker {
    override fun onHook() {
        if (props.isEmpty()) return
        //Source SystemProperties
        arrayOf(
            "android.os.SystemProperties",
            "com.oplus.wrapper.os.SystemProperties"
        ).forEach {
            it.toClass().resolve().apply {
                method { name = "get"; returnType = String::class }.hookAll {
                    before {
                        val key = args().first().cast<String>()
                        if (key.isNullOrBlank()) return@before
                        when (val value = props[key]) {
                            null -> return@before
                            is Boolean -> result = value.toString()
                            is String -> result = value
                            is Int -> result = value
                        }
                    }
                }
                firstMethod { name = "getBoolean"; returnType = Boolean::class }.hook {
                    before {
                        val key = args().first().cast<String>()
                        if (key.isNullOrBlank()) return@before
                        when (val value = props[key]) {
                            null -> return@before
                            "1" -> resultTrue()
                            "0" -> resultFalse()
                            "true" -> resultTrue()
                            "false" -> resultFalse()
                            is Boolean -> result = value
                        }
                    }
                }
                firstMethod { name = "getInt"; returnType = Int::class }.hook {
                    before {
                        val key = args().first().cast<String>()
                        if (key.isNullOrBlank()) return@before
                        when (val value = props[key]) {
                            null -> return@before
                            is Long -> result = value.toInt()
                            is Int -> result = value
                        }
                    }
                }
                firstMethod { name = "getLong"; returnType = Long::class }.hook {
                    before {
                        val key = args().first().cast<String>()
                        if (key.isNullOrBlank()) return@before
                        when (val value = props[key]) {
                            null -> return@before
                            is Int -> result = value.toLong()
                            is Long -> result = value
                        }
                    }
                }
            }
        }
    }
}