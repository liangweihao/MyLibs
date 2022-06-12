package com.nothing.commonutils.utils

import android.content.Context
import android.os.FileObserver
import androidx.lifecycle.MutableLiveData
import java.io.File
import kotlin.collections.ArrayList

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/5/19
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */

class SharePreferenceService {


    var shareFileResult:MutableLiveData<List<File>> = MutableLiveData(ArrayList())
    private fun getSharePreferenceDir(context:Context):File {
        return getPreferencesDir(context)
    }


    fun scanXml(context:Context) {
        val scan = scan(getSharePreferenceDir(context))
        shareFileResult.value = scan ?: ArrayList()
    }

    private fun scan(dir:File):MutableList<File>? {
        return getFiles(dir)?.toMutableList()
    }


    /**
     * 清理所有扫描道德文件
     * */
    fun clearSharePreference() {
        shareFileResult.value?.forEach {
            if (it.exists()) {
                it.delete()
                d("delete file:${it.path}")
            }
        }
    }

    private var fileObserver:FileObserver? = null

    fun listener(context:Context) {
        if (fileObserver == null) {
            val sharePreferenceDir = getSharePreferenceDir(context)
            fileObserver =
                object:FileObserver(sharePreferenceDir, FileObserver.MODIFY xor FileObserver.CREATE xor FileObserver.DELETE) {
                    override fun onEvent(event:Int, path:String?) {
                        d("event:$event path:$path")
                        if (event == FileObserver.CREATE) {
                            var value = shareFileResult.value
                            var newList = value!!.toMutableList()
                            newList.add(File(sharePreferenceDir, path))
                            shareFileResult.postValue(newList)
                        } else if (event == FileObserver.DELETE) {
                            var value = shareFileResult.value
                            var newList = value!!.toMutableList()
                            val iterator = newList.iterator()
                            if (iterator.hasNext()) {
                                val next = iterator.next()
                                if (next.equals(File(sharePreferenceDir, path))) {
                                    iterator.remove()
                                }
                            }

                            shareFileResult.postValue(newList)
                        }

                    }

                }
        }

        fileObserver?.startWatching()
    }


    fun unListener() {
        fileObserver?.stopWatching()
        fileObserver = null
    }

}