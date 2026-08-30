package com.luckyzyx.luckytool.hook.scopes.cloudservice

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.XLog
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object DisableForcedBackupAppList : Hooker {
    override fun onHook() {
        val backupRestoreOptUiStyle =
            "com.heytap.cloud.backuprestore.bswitch.BackupRestoreOptUiStyle"

        val uiStyleEnum = backupRestoreOptUiStyle.toClassOrNull() ?: run {
            XLog.debug("DisableForcedBackupAppList clazz is null!")
            return
        }
        if (!uiStyleEnum.isEnum) {
            XLog.debug("DisableForcedBackupAppList enum is error!")
            return
        }
        val switchStyle = uiStyleEnum.enumConstants?.find { it.toString() == "STYLE_SWITCH" }
            ?: return

        //Source BackupRestoreOpt
        "com.heytap.cloud.backuprestore.bswitch.BackupRestoreOpt".toClass().resolve().apply {
            firstMethodOrNull { name = "getForceSelect" }?.hook {
                replaceToFalse()
            }
        }

        //Source BackupRestoreOptUiData
        "com.heytap.cloud.backuprestore.bswitch.bean.BackupRestoreOptUiData".toClass().resolve()
            .apply {
                firstMethod { name = "getOptStyle" }.hook {
                    before {
                        val optId = firstField { name = "optId" }.of(instance).get<String>()
                        if (optId == "backup_switch_key_third_app") {
                            val style = switchStyle.asResolver().firstMethod { name = "getStyle" }
                                .invoke()
                            result = style
                        }
                    }
                }
            }
    }
}