package com.nothing.commonutils.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nothing.commonutils.R
import com.nothing.commonutils.databinding.ActivityBaseListBinding
import com.nothing.commonutils.widget.adapter.BaseListAdapter
import com.nothing.commonutils.widget.adapter.createBaseAdapter
import kotlin.reflect.KClass

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/6/12
 *<p>Intro:
 *
 *  列表Activity
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */
open class BaseListActivity:AppCompatActivity() {

    lateinit var bind:ActivityBaseListBinding
    lateinit var adapter:BaseListAdapter

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_base_list)
        adapter = bind.rvList.createBaseAdapter()
        setLayoutManager(LinearLayoutManager(this))
    }

    fun addItemDecration(id:RecyclerView.ItemDecoration) {
        bind.rvList.addItemDecoration(id)
    }
    fun setLayoutManager(lm:RecyclerView.LayoutManager?) {
        bind.rvList.layoutManager = lm
    }

    fun <T> registerItem(clazz:KClass<*>, layoutid:Int, binderInfo:BaseListAdapter.BinderInfo<T>) {
        adapter.register(clazz,layoutid,binderInfo)
    }

    protected fun subList(list:List<Any>?, runnable:Runnable?) {
        adapter.submitList(list,runnable)
    }

}