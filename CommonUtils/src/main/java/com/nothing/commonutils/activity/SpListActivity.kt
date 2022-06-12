package com.nothing.commonutils.activity

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nothing.commonutils.R
import com.nothing.commonutils.databinding.ViewSingleTextBinding
import com.nothing.commonutils.services.CommonBackgroundConnection
import com.nothing.commonutils.services.CommonBackgroundServer
import com.nothing.commonutils.services.bindServices
import com.nothing.commonutils.widget.adapter.BaseListAdapter
import com.nothing.commonutils.widget.adapter.ViewHolderInner
import java.io.File

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/6/10
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */
class SpListActivity:BaseListActivity() {
    private val connection = object:CommonBackgroundConnection(){
        override fun onServiceConnected(binder:CommonBackgroundServer.CommonBackgroundBinder) {
            binder.getShareFileList().observe(this@SpListActivity){
                subList(it){}
            }
         }

        override fun onServiceDisconnected(name:ComponentName?) {

        }
    }

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = "数据库列表"
        registerItem(File::class, R.layout.view_single_text, object:BaseListAdapter.BinderInfo<File>() {
            override fun onBind(holder:ViewHolderInner) {
                val itemBind = DataBindingUtil.bind<ViewSingleTextBinding>(holder.itemView)
                itemBind?.title = "数据库：${model?.name?.replace(".xml","")}"
            }

            override fun genLayoutParams(parent:View, viewType:Int):ViewGroup.LayoutParams? {
                parent.setPadding(resources.getDimensionPixelSize(R.dimen.default_container_padding))
                return ViewGroup.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.WRAP_CONTENT)
            }
        })
        setLayoutManager(object :LinearLayoutManager(this){
            override fun generateLayoutParams(c:Context?, attrs:AttributeSet?):RecyclerView.LayoutParams {
                return super.generateLayoutParams(c, attrs)
            }

            override fun generateDefaultLayoutParams():RecyclerView.LayoutParams {
                return super.generateDefaultLayoutParams()
            }
        })
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        addItemDecration(dividerItemDecoration)
        bindServices(connection)

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}