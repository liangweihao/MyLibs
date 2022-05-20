package com.nothing.commonutils.widget.adapter

import android.content.res.Resources
 import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.collection.ArraySet
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nothing.commonutils.inflate
import com.nothing.commonutils.utils.AutoListChangedListUnbind
import com.nothing.commonutils.utils.ListChangedListener
import com.nothing.commonutils.utils.ListDiffChangedListener
import com.nothing.commonutils.utils.d
import kotlin.reflect.KClass

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/5/10
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */

val TAG = "BaseListAdapter"
class BaseDiffItemCallback:DiffUtil.ItemCallback<Any>() {

    private var ad:BaseListAdapter? = null
    fun registerAdapter(adapter:BaseListAdapter) {
        ad = adapter
    }

    override fun areItemsTheSame(oldItem:Any, newItem:Any):Boolean {
        return ad!!.areItemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem:Any, newItem:Any):Boolean {
        return ad!!.areContentsTheSame(oldItem, newItem)
    }
}

fun RecyclerView.createAutoUnbindListChangedAdapter(lifecycle:Lifecycle, list:ObservableList<*>):BaseListAdapter {
    val autoListChangedListUnbind = AutoListChangedListUnbind(list)
    lifecycle.addObserver(autoListChangedListUnbind)
    val multiTypeAdapter = BaseListAdapter()
    autoListChangedListUnbind.addListChangeCallback(ListDiffChangedListener(multiTypeAdapter))
    this.adapter = multiTypeAdapter
    multiTypeAdapter.submitList(ArrayList(list))

    return multiTypeAdapter
}


open class BaseListAdapter:ListAdapter<Any, ViewHolderInner> {

    private var outDataObserver:OutAdapterDataObserver = OutAdapterDataObserver()


    constructor(callback:BaseDiffItemCallback = BaseDiffItemCallback()):super(callback) {
        callback.registerAdapter(this)
    }

    fun areItemsTheSame(oldItem:Any, newItem:Any):Boolean {
        var oldHashCode = 0
        var newHashCode = 0
        var binderInfo:BaseListAdapter.BinderInfo<Any>? = null
        hashMap.forEach {
            val old = it.key.isInstance(oldItem)
            if (old){
                oldHashCode = it.key.hashCode()
                binderInfo =  it.value.binderInfo
            }

            val new = it.key.isInstance(newItem)
            if (new){
                newHashCode = it.key.hashCode()
                binderInfo =  it.value.binderInfo
            }
        }

        if (oldHashCode != newHashCode){
            return false
        }

        return binderInfo?.compare(oldItem,newItem)?:false
    }

    fun areContentsTheSame(oldItem:Any, newItem:Any):Boolean {
        return true
    }

    private constructor(config:AsyncDifferConfig<Any>):super(config)

    override fun onAttachedToRecyclerView(recyclerView:RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        registerAdapterDataObserver(outDataObserver)
    }

    override fun onDetachedFromRecyclerView(recyclerView:RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        unregisterAdapterDataObserver(outDataObserver)

    }



    override fun onCreateViewHolder(parent:ViewGroup, viewType:Int):ViewHolderInner {
        val layoutID = getLayoutID(viewType)
        var child:View? = null
        if (layoutID > 0) {
            child = parent.inflate(layoutID)
            return ViewHolderInner(child)
        }
        throw Resources.NotFoundException("view type not found :$viewType") //        return null
    }

    private fun getLayoutID(viewType:Int):Int {
        hashMap.forEach {
            if (it.key.hashCode() == viewType) {
                return it.value.layoutID
            }
        }
        return 0
    }

    override fun getItemViewType(position:Int):Int {
        val get = currentList.get(position)
        hashMap.forEach {
            if (it.key.isInstance(get)) {
                return it.key.hashCode()
            }
        }
        return 0
    }


    fun <T> getItemElement(index:Int):T {
        return currentList[index] as T
    }

    private fun getArrayListCache():MutableList<Any> {
        return ArrayList()
    }

    @MainThread fun removeListItem(index:Int, result:Runnable? = null) {
        val arrayListCache = getArrayListCache()
        currentList.forEach {
            arrayListCache.add(it)
        }
        arrayListCache.removeAt(index)
        submitList(arrayListCache) {
            result?.run()
        }
    }

    override fun onCurrentListChanged(previousList:MutableList<Any>, currentList:MutableList<Any>) {
        super.onCurrentListChanged(previousList, currentList)

    }

    override fun onBindViewHolder(holder:ViewHolderInner, position:Int) {
        val itemViewType = getItemViewType(position)
        hashMap.forEach {
            if (it.key.hashCode() == itemViewType) {
                it.value.binderInfo.onBind(holder)
                return@forEach
            }
        }
    }

    override fun onViewAttachedToWindow(holder:ViewHolderInner) {
        super.onViewAttachedToWindow(holder)
        holder.bindDataBinding()
    }

    override fun onViewDetachedFromWindow(holder:ViewHolderInner) {
        super.onViewDetachedFromWindow(holder)
        holder.unBindDataBinding()
    }


    var hashMap = HashMap<KClass<*>, ViewInfo<Any>>()

    data class ViewInfo<T>(val layoutID:Int = 0, var binderInfo:BinderInfo<T>)

    fun <T> register(modelClass:KClass<*>, layoutID:Int, info:BinderInfo<T>) {
        hashMap[modelClass] = ViewInfo<Any>(layoutID = layoutID, binderInfo = info as BinderInfo<Any>)
    }


    fun updateOutData(position:Int, key:String, value:Any) {
        if (position == RecyclerView.NO_POSITION) {
            return
        }
        val itemElement = getItemElement<Any>(position)
        if (outDataMap[getHashIndex(position, itemElement)] == null) {
            outDataMap.put(getHashIndex(position, itemElement), HashMap())
        }
        outDataMap[getHashIndex(position, itemElement)]?.put(key, value)
    }

    private fun getHashIndex(position:Int, value:Any):Int {
        var result = position.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    fun <T> getOutData(position:Int, key:String, defaultValue:T, isUpdateDefaultValue:Boolean):T {
        if (position == RecyclerView.NO_POSITION) {
            return defaultValue
        }
        val itemElement = getItemElement<Any>(position)
        val get = outDataMap[getHashIndex(position, itemElement)]?.get(key)
        if (get == null && isUpdateDefaultValue) {
            updateOutData(position, key, defaultValue!!)
            return defaultValue
        } else if (get == null) {
            return defaultValue
        }
        return get as T
    }

    inner class OutAdapterDataObserver:RecyclerView.AdapterDataObserver() {

        override fun onChanged() {
            super.onChanged()
            updateOutDataMap(0, currentList.size)
        }


        private fun updateOutDataMap(start:Int, count:Int) {
            val sameList = ArraySet<Int>()
            val removeList = ArraySet<Int>() // N
            for (index in start until count) {
                sameList.add(currentList[index].hashCode())
            } //0-N
            outDataMap.forEach { key, value ->
                val add = sameList.add(key)
                if (add) { // 说明数据多余
                    removeList.add(key)
                }
            } //0 - N
            // 剩下的就是有效的数据
            removeList.forEach {
                outDataMap.remove(it)
            }
        }

        private fun removeOutDataMap(start:Int, count:Int) {

            val sameList = ArraySet<Int>() // N
            val removeList = ArrayList<Int>()
            currentList.forEachIndexed { index, t ->
                sameList.add(getHashIndex(index, t as Any))
            } // 剩下的就是有效的数据
            outDataMap.forEach { key, value ->
                val add = sameList.add(key) // true 代表 没有这个数据
                if (add) {
                    removeList.add(key)
                }
            }
            removeList.forEach {
                outDataMap.remove(it)
            }
        }

        override fun onItemRangeChanged(positionStart:Int, itemCount:Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            updateOutDataMap(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart:Int, itemCount:Int, payload:Any?) {
            super.onItemRangeChanged(positionStart, itemCount, payload)
            updateOutDataMap(positionStart, itemCount)
        }

        override fun onItemRangeInserted(positionStart:Int, itemCount:Int) {
            super.onItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeRemoved(positionStart:Int, itemCount:Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            removeOutDataMap(positionStart, itemCount)
        }


        override fun onItemRangeMoved(fromPosition:Int, toPosition:Int, itemCount:Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            removeOutDataMap(fromPosition, itemCount)
        }
    }


    var outDataMap = SparseArray<HashMap<String, Any>>()

    public abstract class BinderInfo<T> {
        // TODO:LWH  2022/5/16  这里把position去掉了 原因是 如果数据发生变化了 那么这个position其实是不准确的
        abstract fun onBind(holder:ViewHolderInner)

        open fun compare(old:T, now:T):Boolean{

            return false
        }
    }
}

/**
 *  getAdapterPosition 这个是holder 当前的位置 假设数据没有了试图还在那么就会出现越界的问题
 *
 * 推荐使用这个： getLayoutPosition  布局重新绘制之前的位置， 至少这个能够保证当前的数据和试图的位置是相对应的
 * */
class ViewHolderInner(itemView:View):RecyclerView.ViewHolder(itemView) {

    private var binding:ViewDataBinding? = null
    fun bindDataBinding() {
        val findBinding = DataBindingUtil.findBinding<ViewDataBinding>(this.itemView)
        if (findBinding == null) {
            binding = DataBindingUtil.bind(this.itemView)!!
        } else {
            binding = findBinding
        }
    }

    fun <T:ViewDataBinding> getBinding():T {
        if (binding == null) {
            bindDataBinding()
        }
        if (binding == null) {
            throw Resources.NotFoundException("binding is not exit")
        }
        return binding as T
    }


    fun unBindDataBinding() {
        binding?.unbind()
    }


}