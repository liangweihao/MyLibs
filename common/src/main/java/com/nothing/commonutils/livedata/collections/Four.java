package com.nothing.commonutils.livedata.collections;


import com.nothing.commonutils.utils.GsonUtils;

import java.util.Objects;

import androidx.annotation.NonNull;

public class Four<F, S, T, A> extends Tree<F, S, T> {
    public final A four;

    public Four(F first, S second, T tree, A four) {
        super(first, second, tree);
        this.four = four;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Four)) return false;
        if (!super.equals(o)) return false;

        Four<?, ?, ?, ?> four1 = (Four<?, ?, ?, ?>) o;

        return Objects.equals(four, four1.four);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (four != null ? four.hashCode() : 0);
        return result;
    }
    @NonNull
    @Override
    public String toString() {
        return GsonUtils.toJson(this);
    }
}
