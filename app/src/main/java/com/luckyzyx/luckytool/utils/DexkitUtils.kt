package com.luckyzyx.luckytool.utils

import android.annotation.SuppressLint
import com.luckyzyx.luckytool.hook.core.XLog
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.ClassDataList
import org.luckypray.dexkit.result.FieldDataList
import org.luckypray.dexkit.result.MethodDataList

@Obfuscate
object DexkitUtils {
    val tag = "LuckyTool"

    private val findClass = "findClass"
    private val findMethod = "findMethod"
    private val findField = "findField"

    /**
     * 创建Dexkit实例
     * @param appPath String
     * @return DexKitBridge?
     */
    fun create(appPath: String): DexKitBridge {
        System.loadLibrary("dexkit")
        return DexKitBridge.create(appPath)
    }

    /**
     * 创建Dexkit安全实例
     * @param appPath String
     * @param result Function1<DexKitBridge, Unit>
     */
    @SuppressLint("DuplicateCreateDexKit")
    fun create(appPath: String, result: (DexKitBridge) -> Unit) {
        System.loadLibrary("dexkit")
        DexKitBridge.create(appPath).use { result(it) }
    }

    /**
     * 检查搜索到的类列表并打印LOG
     * @receiver ClassDataList
     * @param instance String
     * @param onlyOne Boolean
     * @param isDebug Boolean
     * @return ClassDataList
     */
    fun ClassDataList.checkDataList(
        instance: String, onlyOne: Boolean = true, isDebug: Boolean = false
    ): ClassDataList {
        when {
            isNullOrEmpty() -> XLog.error("$instance -> $findClass isNullOrEmpty", tag = tag)
            size != 1 && (isDebug || onlyOne) -> {
                if (isDebug) {
                    XLog.debug(
                        "$instance -> $findClass size ($size) | onlyOne: $onlyOne",
                        tag = tag
                    )
                } else XLog.error("$instance -> $findClass size ($size)", tag = tag)
                if (isDebug) forEachIndexed { index, it ->
                    XLog.debug("$instance -> $findClass ($index) | ${it.name}", tag = tag)
                }
            }

            size == 1 -> if (isDebug) XLog.debug(
                "$instance -> $findClass ${single().name}", tag = tag
            )
        }
        return this
    }

    /**
     * 检查搜索到的方法列表并打印LOG
     * @receiver MethodDataList
     * @param instance String
     * @param onlyOne Boolean
     * @param isDebug Boolean
     * @return MethodDataList
     */
    fun MethodDataList.checkDataList(
        instance: String, onlyOne: Boolean = true, isDebug: Boolean = false
    ): MethodDataList {
        when {
            isNullOrEmpty() -> XLog.error("$instance -> $findMethod isNullOrEmpty", tag = tag)
            size != 1 && (isDebug || onlyOne) -> {
                if (isDebug) {
                    XLog.debug(
                        "$instance -> $findMethod size ($size) | onlyOne: $onlyOne", tag = tag
                    )
                } else XLog.error("$instance -> $findMethod size ($size)", tag = tag)
                if (isDebug) forEachIndexed { index, it ->
                    XLog.debug(
                        "$instance -> $findMethod ($index) | ${it.className} | ${it.methodName}",
                        tag = tag
                    )
                }
            }

            size == 1 -> if (isDebug) {
                XLog.debug(
                    "$instance -> $findMethod Method -> ${single().className} | ${single().methodName}",
                    tag = tag
                )
                XLog.debug(
                    "$instance -> $findMethod Type -> ${single().paramTypeNames} | ${single().returnTypeName}",
                    tag = tag
                )
            }
        }
        return this
    }

    /**
     * 检查搜索到的Field列表并打印LOG
     * @receiver FieldDataList
     * @param instance String
     * @param onlyOne Boolean
     * @param isDebug Boolean
     * @return FieldDataList
     */
    fun FieldDataList.checkDataList(
        instance: String, onlyOne: Boolean = true, isDebug: Boolean = false
    ): FieldDataList {
        when {
            isNullOrEmpty() -> XLog.error("$instance -> $findField isNullOrEmpty", tag = tag)
            size != 1 && (isDebug || onlyOne) -> {
                if (isDebug) {
                    XLog.debug(
                        "$instance -> $findField size ($size) | onlyOne: $onlyOne", tag = tag
                    )
                } else XLog.error("$instance -> $findField size ($size)", tag = tag)
                if (isDebug) forEachIndexed { index, it ->
                    XLog.debug(
                        "$instance -> $findField ($index) | ${it.className} | ${it.fieldName} | ${it.typeName}",
                        tag = tag
                    )
                }
            }

            size == 1 -> if (isDebug) {
                XLog.debug("$instance -> $findField Class -> ${single().className}", tag = tag)
                XLog.debug(
                    "$instance -> $findField Field -> ${single().fieldName} | ${single().typeName}",
                    tag = tag
                )
            }
        }
        return this
    }
}