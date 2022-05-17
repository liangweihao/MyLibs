package com.nothing.commonutils.utils

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout


/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/5/17
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */
class FKeyBoardHeightUtils private constructor(activity:Activity, keyBoardHigthListener:KeyBoardVisiableListener) {
    private val mChildOfContent //activity 的布局View
            :View?
    private var usableHeightPrevious //activity的View的可视高度
            = 0
    private val keyBoardHigthListener:KeyBoardVisiableListener
    private val globalLayoutListener = OnGlobalLayoutListener { possiblyResizeChildOfContent() }
    @SuppressLint("ObsoleteSdkInt") fun removeKeyboardHeightListener() {
        if (mChildOfContent == null) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mChildOfContent.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener)
        } else {
            mChildOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    private fun possiblyResizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard:Int = mChildOfContent?.getRootView()?.getHeight() ?: 0
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                keyBoardHigthListener.showKeyBoard(usableHeightSansKeyboard - heightDifference, mChildOfContent)
            } else {
                keyBoardHigthListener.hideKeyBoard(usableHeightSansKeyboard, mChildOfContent)
            }
            usableHeightPrevious = usableHeightNow
        }
    }

    private fun computeUsableHeight():Int {
        val r = Rect()
        mChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top
    }

    interface KeyBoardVisiableListener {
        fun showKeyBoard(heigth:Int, contentView:View?)
        fun hideKeyBoard(heigth:Int, contentView:View?)
    }

    companion object {
        fun setKeyBoardHeigthListener(activity:Activity, keyBoardHigthListener:KeyBoardVisiableListener):FKeyBoardHeightUtils {
            return FKeyBoardHeightUtils(activity, keyBoardHigthListener)
        }
    }

    init {
        this.keyBoardHigthListener = keyBoardHigthListener
        val content = activity.findViewById<View>(R.id.content) as FrameLayout
        mChildOfContent = content.getChildAt(0)
        mChildOfContent.getViewTreeObserver()
            .addOnGlobalLayoutListener(globalLayoutListener) //监听视图高度变化
        mChildOfContent.setOnClickListener(object:View.OnClickListener {
            override fun onClick(v:View?) {

                hideInputForce(activity)
            }


        })
    }
}


/**
 * 打开软键盘
 * 魅族可能会有问题
 *
 * @param mEditText
 * @param mContext
 */
fun openKeybord(mEditText:EditText?, mContext:Context) {
    val imm:InputMethodManager =
        mContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN)
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

/**
 * 关闭软键盘
 *
 * @param mEditText
 * @param mContext
 */
fun closeKeybord(mEditText:EditText, mContext:Context) {
    val imm:InputMethodManager =
        mContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(mEditText.windowToken, 0)
}


/**
 * des:隐藏软键盘,这种方式参数为activity
 *
 * @param activity
 */
fun hideInputForce(activity:Activity?) {
    if (activity == null || activity.currentFocus == null) return
    (activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(activity.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

/**
 * 打开键盘
 */
fun showInput(context:Context, view:View) {
    val imm:InputMethodManager =
        context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm != null) {
        view.requestFocus()
        imm.showSoftInput(view, 0)
    }
}
