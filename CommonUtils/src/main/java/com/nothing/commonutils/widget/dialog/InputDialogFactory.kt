package com.nothing.commonutils.widget.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.TextViewBindingAdapter
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nothing.commonutils.databinding.ViewInputWidgetBinding
import com.nothing.commonutils.R

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

/**
 *
 * 创建输入框的dialog
 * */
fun createInputDialog(context:FragmentActivity,title:String,sureText:String, listener:View.OnClickListener, changeListener:TextViewBindingAdapter.AfterTextChanged):Dialog {

    val binding =
        DataBindingUtil.inflate<ViewInputWidgetBinding>(LayoutInflater.from(context), R.layout.view_input_widget, null, false)
    binding.sureText = sureText
    binding.afterTextChanged = changeListener
    binding.onSureListener = listener
    binding.title = title
    return BottomSheetDialog(context).also {
        it.setContentView(binding.root)

    }

}