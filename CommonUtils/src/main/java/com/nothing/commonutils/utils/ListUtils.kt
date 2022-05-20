package com.nothing.commonutils.utils

/**
 *--------------------
 *<p>Author：
 *         liangweihao
 *<p>Created Time:
 *          2022/5/20
 *<p>Intro:
 *
 *<p>Thinking:
 *
 *<p>Problem:
 *
 *<p>Attention:
 *--------------------
 */

fun <T> MutableList<T>.replace(target:List<T>) {
    if (target.size > this.size) {
        val oldSize = this.size
        target.forEachIndexed { index, t ->
            if (index < oldSize) {
                this.set(index, t)
            } else {
                this.add(t)
            }
        }
    } else if (target.size < this.size) {
        val moreListIterator = this.listIterator()
        val lessListIterator = target.listIterator()
        while (moreListIterator.hasNext()) {
            val nextIndex = moreListIterator.nextIndex()
            if (nextIndex > target.size - 1) {
                moreListIterator.remove()
            } else {
                moreListIterator.set(lessListIterator.next())
            }
        }
    }
}