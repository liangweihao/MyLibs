package com.nothing.commonutils.utils

import androidx.databinding.ObservableList.OnListChangedCallback
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.RecyclerView
import com.nothing.commonutils.widget.adapter.BaseListAdapter

/**
 *
 * [ObservableArrayList.addOnListChangedCallback]
 * @see AutoListChangedListUnbind
 */
open class ListChangedListener(private val adapter:RecyclerView.Adapter<*>?):OnListChangedCallback<ObservableList<*>?>() {
    protected open fun onListChanged() {}
    override fun onChanged(sender:ObservableList<*>?) {
        adapter?.notifyDataSetChanged()
        onListChanged()
    }

    override fun onItemRangeChanged(
        sender:ObservableList<*>?,
        positionStart:Int,
        itemCount:Int,
    ) {
        adapter?.notifyItemRangeChanged(positionStart, itemCount)
        onListChanged()
    }

    override fun onItemRangeInserted(
        sender:ObservableList<*>?,
        positionStart:Int,
        itemCount:Int,
    ) {
        adapter?.notifyItemRangeInserted(positionStart, itemCount)
        onListChanged()
    }

    override fun onItemRangeMoved(
        sender:ObservableList<*>?,
        fromPosition:Int,
        toPosition:Int,
        itemCount:Int,
    ) {
        adapter?.notifyItemMoved(fromPosition, toPosition)
        onListChanged()
    }

    override fun onItemRangeRemoved(
        sender:ObservableList<*>?,
        positionStart:Int,
        itemCount:Int,
    ) {
        adapter?.notifyItemRangeRemoved(positionStart, itemCount)
        onListChanged()
    }
}


open class ListDiffChangedListener(private val adapter:BaseListAdapter):OnListChangedCallback<ObservableList<*>?>() {
    protected open fun onListChanged() {}
    override fun onChanged(sender:ObservableList<*>?) {
        submitList(sender)

    }

    override fun onItemRangeChanged(
        sender:ObservableList<*>?,
        positionStart:Int,
        itemCount:Int,
    ) {
        submitList(sender)

    }

    override fun onItemRangeInserted(
        sender:ObservableList<*>?,
        positionStart:Int,
        itemCount:Int,
    ) {
        submitList(sender)
    }


    private fun submitList(sender:ObservableList<*>?) {
        adapter.submitList(ArrayList(sender)){
            onListChanged()
        }

    }

    override fun onItemRangeMoved(
        sender:ObservableList<*>?,
        fromPosition:Int,
        toPosition:Int,
        itemCount:Int,
    ) {
        submitList(sender)

    }

    override fun onItemRangeRemoved(
        sender:ObservableList<*>?,
        positionStart:Int,
        itemCount:Int,
    ) {
        submitList(sender)
    }
}
