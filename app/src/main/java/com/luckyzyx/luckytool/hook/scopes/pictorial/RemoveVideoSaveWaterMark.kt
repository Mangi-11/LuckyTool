package com.luckyzyx.luckytool.hook.scopes.pictorial

import android.widget.LinearLayout
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.hook.core.instance
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveVideoSaveWaterMark : Hooker {
    override fun onHook() {
        //Source VideoWaterMarkView -> view_video_water_mark
        "com.heytap.pictorial.data.VideoWaterMarkView".toClass().resolve().apply {
            constructor {}.hookAll {
                after {
                    instance<LinearLayout>().removeAllViews()
                }
            }
        }
    }
}