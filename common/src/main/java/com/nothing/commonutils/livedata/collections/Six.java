package com.nothing.commonutils.livedata.collections;


import java.util.Objects;

public class Six<A,B,C,D,E,F> extends Five<A,B,C,D,E>{
    public final F six;
    public Six(A first, B second, C tree, D four, E five, F six) {
        super(first, second, tree, four, five);
        this.six = six;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Six)) return false;
        if (!super.equals(o)) return false;

        Six<?, ?, ?, ?, ?, ?> six1 = (Six<?, ?, ?, ?, ?, ?>) o;

        return Objects.equals(six, six1.six);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (six != null ? six.hashCode() : 0);
        return result;
    }
}
