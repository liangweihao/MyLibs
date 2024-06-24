package com.nothing.commonutils.livedata.collections;


import com.inair.space.utils.GsonUtils;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

public class Tree<F, S, T> extends Pair<F, S> {
    public final T tree;

    public Tree(F first, S second, T tree) {
        super(first, second);
        this.tree = tree;
    }

    @NonNull
    @Override
    public String toString() {
        return GsonUtils.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tree)) return false;
        if (!super.equals(o)) return false;

        Tree<?, ?, ?> tree1 = (Tree<?, ?, ?>) o;

        return Objects.equals(tree, tree1.tree);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (tree != null ? tree.hashCode() : 0);
        return result;
    }
}
