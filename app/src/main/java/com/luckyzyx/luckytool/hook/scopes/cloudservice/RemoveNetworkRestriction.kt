package com.luckyzyx.luckytool.hook.scopes.cloudservice

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.hook.core.result
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveNetworkRestriction(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source BackUpActivity / BackupRestoreHelper -> backup_currently_mobile
        //Source BackupRestoreCheckerUtils -> check -> ? 2 : 0
        //Source RestoreCheck / WifiCheck -> check -> BackupRestoreCode -> NO_WIFI / SUCCESS
        //Source NetworkUtil -> 2 == ?() -> getSystemService -> connectivity
        //Search Const.Callback.NetworkState.NetworkType.NETWORK_MOBILE -> ? 1 : 0 -> Method
        dexKitBridge.findClass {
            matcher {
                methods {
                    add {
                        paramCount(0)
                        returnType(Int::class.java)
                        usingStrings("connectivity")
                        usingNumbers(0, 1, 2)
                    }
                    add {
                        paramTypes(Int::class.java)
                        returnType(Boolean::class.java)
                    }
                    add {
                        paramTypes(Context::class.java)
                        returnType(Boolean::class.java)
                        usingStrings("NetworkUtil", "connectivity", "isMobileDataNetwork")
                    }
                    add {
                        paramTypes(Context::class.java)
                        returnType(Boolean::class.java)
                        usingStrings("NetworkUtil", "connectivity", "isNetworkConnected")
                    }
                }
            }
        }.apply {
            checkDataList("RemoveNetworkRestriction")
            single().name.toClass().resolve().apply {
                method { emptyParameters();returnType = Int::class }.hookAll {
                    after { if (result<Int>() == 1) result = 2 }
                }
            }
        }
    }
}