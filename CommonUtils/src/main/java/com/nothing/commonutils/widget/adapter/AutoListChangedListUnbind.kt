package com.nothing.commonutils.utils

import androidx.databinding.ObservableList
import androidx.databinding.ObservableList.OnListChangedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2021/3/15
 *<p>Intro:
 *   基于生命周期的状态回调 记得解绑行为
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */
class AutoListChangedListUnbind(internal var list:ObservableList<*>):LifecycleEventObserver {


    private var cacheList:MutableList<WeakReference<OnListChangedCallback<*>>> = ArrayList()

    fun addListChangeCallback(callback:OnListChangedCallback<*>) {
        cacheList.add(WeakReference(callback))
        list.addOnListChangedCallback(callback as OnListChangedCallback<Nothing>)
    }


    override fun onStateChanged(source:LifecycleOwner, event:Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            for (weakReference in cacheList) {
                weakReference.get()?.also {
                    list.removeOnListChangedCallback(it as OnListChangedCallback<Nothing>)
                }
            }
            cacheList.clear()
        }
    }
}



class ProxyObservableList<T>(var primary:ObservableList<T>):ObservableList<T> {

    private var map = HashMap<T, HashMap<String, ObservableList<*>>>()

    init {
        addOnListChangedCallback(object:OnListChangedCallback<ObservableList<T>>() {
            override fun onChanged(sender:ObservableList<T>?) {
                map.clear()
                sender?.forEach {
                    map[it] = HashMap()
                }
            }

            override fun onItemRangeChanged(sender:ObservableList<T>?, positionStart:Int, itemCount:Int) {
                for (index in positionStart until itemCount + 1) {
                    val t = sender!!.get(index)!!
                    map[t] = HashMap()
                }
            }

            override fun onItemRangeInserted(sender:ObservableList<T>?, positionStart:Int, itemCount:Int) {
                val t = sender!![positionStart]!!
                map[t] = HashMap()
            }

            override fun onItemRangeMoved(sender:ObservableList<T>?, fromPosition:Int, toPosition:Int, itemCount:Int) {
                val from = sender!!.get(fromPosition)!!
                val to = sender!!.get(toPosition)!!
                val fromMap = map[from]!!
                map[from] = map[to]!!
                map[to] = fromMap
            }

            override fun onItemRangeRemoved(sender:ObservableList<T>?, positionStart:Int, itemCount:Int) {
                for (index in positionStart until itemCount + 1) {
                    val t = sender!!.get(index)!!
                    map.remove(t)
                }
            }
        })
    }


    override val size:Int
        get() = primary.size

    override fun contains(element:T):Boolean = primary.contains(element)

    override fun containsAll(elements:Collection<T>):Boolean = primary.containsAll(elements)

    override fun get(index:Int):T = primary.get(index)

    override fun indexOf(element:T):Int = primary.indexOf(element)

    override fun isEmpty():Boolean = primary.isEmpty()

    override fun iterator():MutableIterator<T> = primary.iterator()

    override fun lastIndexOf(element:T):Int = primary.lastIndexOf(element)

    override fun add(element:T):Boolean = primary.add(element)

    override fun add(index:Int, element:T) = primary.add(index, element)

    override fun addAll(index:Int, elements:Collection<T>):Boolean = primary.addAll(index, elements)

    override fun addAll(elements:Collection<T>):Boolean = primary.addAll(elements)

    override fun clear() = primary.clear()

    override fun listIterator():MutableListIterator<T> = primary.listIterator()

    override fun listIterator(index:Int):MutableListIterator<T> = primary.listIterator(index)

    override fun remove(element:T):Boolean = primary.remove(element)

    override fun removeAll(elements:Collection<T>):Boolean = primary.removeAll(elements)

    override fun removeAt(index:Int):T = primary.removeAt(index)
    override fun retainAll(elements:Collection<T>):Boolean = primary.retainAll(elements)

    override fun set(index:Int, element:T):T = primary.set(index, element)

    override fun subList(fromIndex:Int, toIndex:Int):MutableList<T> = primary.subList(fromIndex, toIndex)

    override fun addOnListChangedCallback(callback:OnListChangedCallback<out ObservableList<T>>?) = primary.addOnListChangedCallback(callback)

    override fun removeOnListChangedCallback(callback:OnListChangedCallback<out ObservableList<T>>?) = primary.removeOnListChangedCallback(callback)
}