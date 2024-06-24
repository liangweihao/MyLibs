package com.nothing.commonutils.livedata.collections;


import com.nothing.commonutils.utils.GsonUtils;

import java.util.Objects;

import androidx.annotation.NonNull;

public class Five<A,B,C,D,E> extends Four<A,B,C,D>{

    public final E five;

    public Five(A first, B second, C tree, D four, E five) {
        super(first, second, tree, four);
        this.five = five;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Five)) return false;
        if (!super.equals(o)) return false;

        Five<?, ?, ?, ?, ?> five1 = (Five<?, ?, ?, ?, ?>) o;

        return Objects.equals(five, five1.five);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (five != null ? five.hashCode() : 0);
        return result;
    }
    @NonNull
    @Override
    public String toString() {
        return GsonUtils.toJson(this);
    }
}
