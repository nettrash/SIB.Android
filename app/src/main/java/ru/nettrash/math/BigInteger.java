package ru.nettrash.math;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by nettrash on 10.01.2018.
 */

public final class BigInteger {

    private ArrayList<Integer> _numberData;
    public int t = 0;
    public int s = 0;

    @Contract(pure = true)
    public static Integer nbits(Integer x)
    {
        Integer r = 1;
        Integer t = x >> 16;
        if (t != 0)
        {
            x = t;
            r = r + 16;
        }
        t = x >> 8;
        if (t != 0)
        {
            x = t;
            r = r + 8;
        }
        t = x >> 4;
        if (t != 0)
        {
            x = t;
            r = r + 4;
        }
        t = x >> 2;
        if (t != 0) {
            x = t;
            r = r + 2;
        }
        t = x >> 1;
        if (t != 0) {
            x = t;
            r = r + 1;
        }
        return r;
    }

    public int bitLength()
    {
        if (t <= 0) return 0;
        return nbits(26 * (t - 1) + (_numberData.get(t - 1) ^ (s & ((1 << 26) - 1))));
    }

    public BigInteger()
    {
        _numberData = new ArrayList<Integer>();
        t = 0;
        s = 0;
    }

    public BigInteger(int[] data)
    {
        _numberData = new ArrayList<Integer>();
        if (data.length < 1) return;

        ArrayList<Integer> bytes = new ArrayList<Integer>();

        if ((data[0] & 0x80) > 0)
        {
           bytes.add(0, 0);
        }

        bytes.addAll(new ArrayList(Arrays.asList(data)));

        fromBytes(bytes);
    }

    public BigInteger(Integer number)
    {
        _numberData = new ArrayList<Integer>();
        _numberData.add(0);
        _numberData.add(0);
        t = 1;
        s =  number < 0 ? -1 : 0;
        if (number > 0)
        {
            _numberData.set(0, number);
        }
        else
        {
            if (number < -1)
            {
                _numberData.set(0, number + (1 << 26));
            }
            else
            {
                t = 0;
            }
        }
    }

    public BigInteger(String number)
    {
        //Только положительные
        _numberData = new ArrayList<Integer>();
        t = 0;
        s = 0;

        Integer j = 0;
        Integer w = 0;

        for (int i = 0; i < number.length(); i++)
        {
            String str = number.substring(i, 1);
            Integer x = Integer.valueOf(str);
            w = w * 10 + x;
            j = j + 1;
            if (j >= 7)
            {
                dMultiply((int)Math.pow(10, 7));
                dAddOffset(w, 0);
                j = 0;
                w = 0;
            }
        }
        if (j > 0)
        {
            dMultiply((int)Math.pow(10, j));
            dAddOffset(w, 0);
        }
    }

    private void fromBytes(ArrayList<Integer> bytes)
    {
        _numberData = new ArrayList<Integer>();

        boolean mi = false;
        Integer sh = 0;

        ArrayList<Integer> rbytes = new ArrayList<>(bytes);
        Collections.reverse(rbytes);
        for (Integer b: rbytes)
        {
            Integer x = b & 0xff;
            if (x < 0)
            {
                mi = true;
                continue;
            }
            mi = false;
            if (sh == 0)
            {
                _numberData.add(t, x);
                t = t + 1;
            }
            else
            {
                if (sh + 8 > 26)
                {
                    _numberData.set(t-1,  _numberData.get(t-1) | (x & ((1 << (26 - sh)) - 1)) << sh);
                    _numberData.add(t, x >> (26 - sh));
                    t = t + 1;
                }
                else
                {
                    _numberData.set(t - 1, _numberData.get(t-1) | x << sh);
                }
            }
            sh = sh + 8;
            if (sh >= 26)
            {
                sh = sh - 26;
            }
        }
        if ((bytes.get(0) & 0x80) != 0)
        {
            s = -1;
            if (sh >= 26)
            {
                _numberData.set(t-1, _numberData.get(t-1) | ((1 << (26 - sh)) - 1) << sh);
            }
        }
        clamp();
        if (mi)
        {
            BigInteger r = new BigInteger(0).subtract(this);
            s = r.s;
            t = r.t;
            _numberData = new ArrayList<>(r._numberData);
        }
    }

    public void clamp()
    {
        Integer c = s & ((1 << 26) - 1);
        while (t > 0 && _numberData.get(t - 1) == c)
        {
            t = t - 1;
        }
    }

    public BigInteger add(BigInteger a)
    {
        BigInteger retVal = new BigInteger();
        Integer i = 0;
        Integer c = 0;
        Integer m = a.t < t ? a.t : t;
        while (i < m)
        {
            c += _numberData.get(i) + a._numberData.get(i);
            retVal._numberData.add(i, c & ((1 << 26) - 1));
            i = i + 1;
            c >>= 26;
        }
        if (a.t < t)
        {
            c += a.s;
            while (i < t)
            {
                c += _numberData.get(i);
                retVal._numberData.add(i, c & ((1 << 26) - 1));
                i = i + 1;
                c >>= 26;
            }
            c += s;
        }
        else
        {
            c += s;
            while (i < a.t)
            {
                c += a._numberData.get(i);
                retVal._numberData.add(i, c & ((1 << 26) - 1));
                i = i + 1;
                c >>= 26;
            }
            c += a.s;
        }
        retVal.s = c < 0 ? -1 : 0;
        if (c > 0)
        {
            retVal._numberData.add(i, c);
            i = i + 1;
        }
        else
        {
            if (c < 0)
            {
                retVal._numberData.add(i, (1 << 26) + c);
                i = i + 1;
            }
        }
        retVal.t = i;
        retVal.clamp();
        return retVal;
    }

    public BigInteger subtract(BigInteger a)
    {
        BigInteger retVal = new BigInteger();

        retVal._numberData = new ArrayList<Integer>(Collections.nCopies(t+a.t+2, 0));
        Integer i = 0;
        Integer c = 0;
        Integer m = a.t < this.t ? a.t : this.t;
        while (i < m)
        {
            c = c + _numberData.get(i) - a._numberData.get(i);
            retVal._numberData.set(i, c & ((1 << 26) - 1));
            i = i + 1;
            c = c >> 26;
        }
        if (a.t < this.t)
        {
            c = c - a.s;
            while (i < t)
            {
                c = c + _numberData.get(i);
                retVal._numberData.set(i, c & ((1 << 26) - 1));
                i = i + 1;
                c = c >> 26;
            }
            c = c + s;
        }
        else
        {
            c = c + s;
            while (i < a.t)
            {
                c = c - a._numberData.get(i);
                retVal._numberData.set(i, c & ((1 << 26) - 1));
                i = i + 1;
                c = c >> 26;
            }
            c = c - a.s;
        }
        retVal.s = c < 0 ? -1 : 0;
        if (c < -1)
        {
            retVal._numberData.set(i, (1 << 26) + c);
            i = i + 1;
        }
        else
        {
            if (c > 0)
            {
                retVal._numberData.set(i, c);
                i = i + 1;
            }
        }
        retVal.t = i;
        retVal.clamp();
        return retVal;
    }

    public boolean isEven()
    {
        return (t > 0 ? _numberData.get(0) & 1 : s) == 0;
    }

    public BigInteger abs()
    {
        if (s < 0)
        {
            return negate();
        }
        else
        {
            return this;
        }
    }

    public BigInteger negate()
    {
        return new BigInteger(0).subtract(this);
    }

    @NonNull
    public Integer[] toByteArray() {
        Integer[] retVal = new Integer[64];
        Arrays.fill(retVal, 0);
        Integer i = t;
        retVal[0] = s;
        Integer p = 26 - (i * 26) % 8;
        Integer k = 0;
        if (i > 0)
        {
            i = i - 1;
            Integer d = _numberData.get(i) >> p;
            if ((p < 26) && (d !=  ((s & ((1 << 26) - 1)) >> p)))
            {
                retVal[k] = d | s << (26 - p);
                k = k + 1;
            }
            while (i >= 0)
            {
                if (p < 8)
                {
                    d = (_numberData.get(i) & (( 1 << p) - 1)) << (8 - p);
                    p = p + 26 - 8;
                    i = i - 1;
                    d |= _numberData.get(i) >> p;
                }
                else
                {
                    p = p - 8;
                    d = (_numberData.get(i) >> p) & 0xff;
                    if (p <= 0)
                    {
                        p = p + 26;
                        i = i - 1;
                    }
                }
                if ((d & 0x80) != 0)
                {
                    d |= -256;
                }
                if (k == 0 && (s & 0x80) != (d & 0x80))
                {
                    k = k + 1;
                }
                if (k > 0 || d != s)
                {
                    retVal[k] = d;
                    k = k + 1;
                }
            }
        }
        return Arrays.copyOfRange(retVal, 0, k-1);
    }

    public Integer[] toByteArrayUnsigned()
    {
        Integer[] ba = abs().toByteArray();

        if (ba.length < 1)
        {
            return new Integer[0];
        }
        if (ba[0] == 0)
        {
            ba = Arrays.copyOfRange(ba, 1, ba.length - 1);
        }
        for (int idx = 0; idx < ba.length; idx++)
        {
            if (ba[idx] < 0)
            {
                ba[idx] = ba[idx] & 0xff;
            }
        }
        return ba;
    }

    public Integer[] toByteArraySigned()
    {
        Integer[] retVal = toByteArrayUnsigned();
        if ((retVal[0] & 0x80) > 0)
        {
            ArrayList<Integer> l = new ArrayList(Arrays.asList(retVal));
            l.add(0, (s < 0 ? 0x80 : 0x00));
            retVal = (Integer[])l.toArray();
        } else {
            if (s < 0)
            {
                retVal[0] |= 0x80;
            }
        }
        return retVal;
    }

    public Integer signum()
    {
        if (s < 0) return -1;
        if (t <= 0 || (t == 1 && _numberData.get(0) <= 0)) return 0;
        return 1;
    }

    public Integer compareTo(BigInteger a)
    {
        Integer r = s - a.s;
        if (r != 0) return r;
        Integer i = t;
        r = i - a.t;
        if (r != 0) return s < 0 ? -r : r;
        while (i > 0)
        {
            i = i - 1;
            r = _numberData.get(i) - a._numberData.get(i);
            if (r != 0) return r;
        }
        return 0;
    }

    public BigInteger clone()
    {
        BigInteger retVal = new BigInteger();
        retVal.s = s;
        retVal.t = t;
        retVal._numberData = new ArrayList<Integer>(Collections.nCopies(t, 0));
        for (int i = 0; i < t; i++)
        {
            retVal._numberData.set(i, _numberData.get(i));
        }
        return retVal;
    }

    public boolean equals(BigInteger b)
    {
        return compareTo(b) == 0;
    }

    public BigInteger power(Integer e)
    {
        if (e > 0xffffffff || e < 1) return new BigInteger(1);
        BigInteger retVal = clone();
        for (int i = 1; i < e; i++)
        {
            retVal = retVal.multiply(this);
        }
        return retVal;
    }

    public BigInteger mod(BigInteger a) throws Exception
    {
        BigInteger retVal = abs().div(a);
        if (s < 0 && retVal.compareTo(new BigInteger(0)) > 0)
        {
            retVal = a.subtract(retVal);
        }
        return retVal;
    }

    public BigInteger modInverse(BigInteger m)
    {
        boolean ac = m.isEven();
        if ((isEven() && ac) || m.signum() == 0)
        {
            return new BigInteger(0);
        }
        BigInteger u = m.clone();
        BigInteger v = clone();
        BigInteger a = new BigInteger(1);
        BigInteger b = new BigInteger(0);
        BigInteger c = new BigInteger(0);
        BigInteger d = new BigInteger(1);

        while (u.signum() != 0)
        {
            while (u.isEven())
            {
                u = u.rShift(1);
                if (ac)
                {
                    if (!a.isEven() || !b.isEven())
                    {
                        a = a.add(this);
                        b = b.subtract(m);
                    }
                    a = a.rShift(1);
                }
                else
                {
                    if (!b.isEven())
                    {
                        b = b.subtract(m);
                    }
                }
                b = b.rShift(1);
            }
            while (v.isEven())
            {
                v = v.rShift(1);
                if (ac)
                {
                    if (!c.isEven() || !d.isEven())
                    {
                        c = c.add(this);
                        d = d.subtract(m);
                    }
                    c = c.rShift(1);
                }
                else
                {
                    if (!d.isEven())
                    {
                        d = d.subtract(m);
                    }
                }
                d = d.rShift(1);
            }
            if (u.compareTo(v) >= 0)
            {
                u = u.subtract(v);
                if (ac)
                {
                    a = a.subtract(c);
                }
                b = b.subtract(d);
            }
            else
            {
                v = v.subtract(u);
                if (ac)
                {
                    c = c.subtract(a);
                }
                d = d.subtract(b);
            }
        }
        if (v.compareTo(new BigInteger(1)) != 0)
        {
            return new BigInteger(0);
        }
        if (d.compareTo(m) >= 0)
        {
            return d.subtract(m);
        }
        if (d.signum() < 0)
        {
            d = d.add(m);
        }
        else
        {
            return d;
        }
        if (d.signum() < 0)
        {
            return d.add(m);
        }
        else
        {
            return d;
        }
    }

    public BigInteger lShift(Integer n)
    {
        Integer bs = n % 26;
        Integer cbs = 26 - bs;
        Integer bm = (1 << cbs) - 1;
        Integer ds = (int)(Math.floor(n / 26));
        Integer c = (s << bs) & ((1 << 26) - 1);
        BigInteger r = new BigInteger();
        r._numberData = new ArrayList<Integer>(Collections.nCopies(t+ds+2, 0));
        if (t > 0)
        {
            for (int i=t-1; i>=0; i--)
            {
                r._numberData.set(i+ds+1, (_numberData.get(i) >> cbs) | c);
                c = (_numberData.get(i) & bm) << bs;
            }
        }
        if (ds > 0)
        {
            for (int i=ds-1;i>=0;i--)
            {
                r._numberData.set(i, 0);
            }
        }
        r._numberData.set(ds, c);
        r.t = t + ds + 1;
        r.s = s;
        r.clamp();
        return r;
    }

    public BigInteger dlShift(Integer n)
    {
        BigInteger retVal = new BigInteger();
        retVal._numberData = new ArrayList<Integer>(Collections.nCopies(t+n+1, 0));
        for (int i=0; i<t; i++)
        {
            retVal._numberData.set(i+n, _numberData.get(i));
        }
        if (n > 0)
        {
            for (int i=0; i<n; i++)
            {
                retVal._numberData.set(i, 0);
            }
        }
        retVal.t = t + n;
        retVal.s = s;
        return retVal;
    }

    public BigInteger rShift(Integer n)
    {
        BigInteger retVal = new BigInteger();
        retVal.s = s;
        Integer ds = (int)Math.floor(n / 26);
        if (ds > t)
        {
            retVal.t = 0;
            return retVal;
        }
        retVal._numberData = new ArrayList<Integer>(Collections.nCopies(t-ds+1, 0));
        Integer bs = n % 26;
        Integer cbs = 26 - bs;
        Integer bm = (1 << bs) - 1;
        retVal._numberData.set(0, _numberData.get(ds) >> bs);
        if (t > 0)
        {
            for (int i=ds+1; i<t; i++)
            {
                retVal._numberData.set(i-ds-1, retVal._numberData.get(i-ds-1) | (_numberData.get(i) & bm) << cbs);
                retVal._numberData.set(i-ds, _numberData.get(i) >> bs);
            }
            if (bs > 0)
            {
                retVal._numberData.set(t-ds-1,  retVal._numberData.get(t-ds-1) | (s & bm) << cbs);
            }
        }
        retVal.t = t - ds;
        retVal.clamp();
        return retVal;
    }

    public BigInteger drShift(Integer n)
    {
        BigInteger retVal = new BigInteger();
        Integer tt = t-n > 0 ? t-n : 0;
        retVal._numberData = new ArrayList<Integer>(Collections.nCopies(tt, 0));
        if (tt > 0)
        {
            for (int i=n; i<t; i++)
            {
                retVal._numberData.set(i-n, _numberData.get(i));
            }
        }
        retVal.t = tt;
        retVal.s = s;
        return retVal;
    }

    public void dMultiply(Integer n)
    {
        if (t < _numberData.size()) {
            _numberData.set(t, am(0, n-1, this, 0, 0, t));
        } else {
            _numberData.add(am(0, n-1, this, 0, 0, t));
        }
        t = t + 1;
        clamp();
    }

    public void dAddOffset(Integer n, Integer w)
    {
        if (n == 0) return;
        Integer ww = w;
        while (t <= ww)
        {
            _numberData.add(t, 0);
            t = t + 1;
        }
        _numberData.set(ww, _numberData.get(ww) + n);
        while (_numberData.get(ww) >= 1 << 26)
        {
            _numberData.set(ww, _numberData.get(ww) - (1 << 26));
            ww = ww + 1;
            if (ww > t)
            {
                _numberData.add(t, 0);
                t = t + 1;
            }
            _numberData.set(ww, _numberData.get(ww) + 1);
        }
    }

    public BigInteger shiftLeft(Integer n)
    {
        if (n < 0)
        {
            return rShift(-n);
        }
        else
        {
            return lShift(n);
        }
    }

    public BigInteger shiftRight(Integer n)
    {
        if (n < 0)
        {
            return lShift(-n);
        }
        else
        {
            return rShift(n);
        }
    }

    public BigInteger div(BigInteger m) throws Exception
    {
        BigInteger pm = m.abs();
        if (pm.t <= 0) throw new ArithmeticException();
        BigInteger pt = abs();
        if (pt.t < pm.t)
        {
            return this;
        }
        BigInteger y;
        Integer ts = s;

        Integer nsh = 26 - nbits(pm._numberData.get(pm.t - 1));
        BigInteger r;
        if (nsh > 0)
        {
            y = pm.lShift(nsh);
            r = pt.lShift(nsh);
        }
        else
        {
            y = pm;
            r = pt;
        }

        Integer ys = y.t;
        Integer y0 = y._numberData.get(ys - 1);
        if (y0 == 0) return r;
        Integer yt = (y0 * (1 << (52 - 26))) + (ys > 1 ? y._numberData.get(ys - 2) >> (2 * 26 - 52) : 0);
        Integer d1 = (int)(Math.pow(2, 52) / yt);
        Integer d2 = (int)((double)(1 << (52 - 26)) / yt);
        Integer e = 1 << (2 * 26 - 52);

        Integer i = r.t;
        Integer j = i - ys;
        BigInteger tt = y.dlShift(j);
        if (r.compareTo(tt) >= 0)
        {
            r._numberData.set(r.t, 1);
            r.t = r.t + 1;
            r = r.subtract(tt);
        }
        tt = new BigInteger(1).dlShift(ys);
        y = tt.subtract(y);
        while (y.t < ys)
        {
            y._numberData.set(y.t, 0);
            y.t = y.t + 1;
        }
        j = j - 1;
        while (j >= 0)
        {
            i = i - 1;
            Integer qd = 0;
            if (r._numberData.get(i) == y0)
            {
                qd = (1 << 26) - 1;
            }
            else
            {
                Integer k1 = r._numberData.get(i) * d1;
                Integer k2 = (r._numberData.get(i-1) + e) * d2;
                qd = (int)Math.floor(k1 + k2);
            }
            r._numberData.set(i, r._numberData.get(i) + y.am(0, qd, r, j, 0, ys));
            if (r._numberData.get(i) < qd)
            {
                tt = y.dlShift(j);
                r = r.subtract(tt);
            }
            qd = qd - 1;
            while (r._numberData.get(i) < qd)
            {
                r = r.subtract(tt);
                qd = qd - 1;
            }
            j = j - 1;
        }
        r.t = ys;
        r.clamp();
        if (nsh > 0)
        {
            r = r.rShift(nsh);
        }
        if (ts < 0)
        {
            r = new BigInteger(0).subtract(r);
        }
        return r;
    }

    public BigInteger divide(BigInteger m) throws Exception
    {
        BigInteger pm = m.abs();
        if (pm.t <= 0) throw new ArithmeticException();
        BigInteger pt = this.abs();
        if (pt.t < pm.t)
        {
            return new BigInteger(0);
        }
        BigInteger y;
        Integer ts = s;
        Integer ms = m.s;

        Integer nsh = 26 - nbits(pm._numberData.get(pm.t - 1));
        BigInteger r;
        if (nsh > 0)
        {
            y = pm.lShift(nsh);
            r = pt.lShift(nsh);
        }
        else
        {
            y = pm;
            r = pt;
        }

        Integer ys = y.t;
        Integer y0 = y._numberData.get(ys - 1);
        if (y0 == 0) return r;
        Integer yt = (y0 * (1 << (52 - 26))) + (ys > 1 ? y._numberData.get(ys - 2) >> (2 * 26 - 52) : 0);
        Double d1 = Math.pow(2, 52) / yt;
        Double d2 = (double)(1 << (52 - 26)) / yt;
        Integer e = 1 << (2 * 26 - 52);

        Integer i = r.t;
        Integer j = i - ys;
        BigInteger tt = y.dlShift(j);
        if (r.compareTo(tt) >= 0)
        {
            r._numberData.set(r.t, 1);
            r.t = r.t + 1;
            r = r.subtract(tt);
        }
        tt = new BigInteger(1).dlShift(ys);
        y = tt.subtract(y);
        while (y.t < ys)
        {
            y._numberData.set(y.t, 0);
            y.t = y.t + 1;
        }
        j = j - 1;
        while (j >= 0)
        {
            i = i - 1;
            Integer qd = 0;
            if (r._numberData.get(i) == y0)
            {
                qd = (1 << 26) - 1;
            }
            else
            {
                double k1 = (double)r._numberData.get(i) * d1;
                double k2 = (double)(r._numberData.get(i-1) + e) * d2;
                qd = (int)Math.floor(k1 + k2);
            }
            r._numberData.set(i, r._numberData.get(i) + y.am(0, qd, r, j, 0, ys));
            if (r._numberData.get(i) < qd)
            {
                tt = y.dlShift(j);
                r = r.subtract(tt);
            }
            qd = qd - 1;
            while (r._numberData.get(i) < qd)
            {
                r = r.subtract(tt);
                qd = qd - 1;
            }
            j = j - 1;
        }
        r = r.drShift(ys);
        if (ts != ms)
        {
            r = new BigInteger(0).subtract(r);
        }
        return r;
    }

    public Integer am(Integer i, Integer x, BigInteger w, Integer j, Integer c, Integer n)
    {
        Integer nn = n - 1;
        Integer ii = i;
        Integer cc = c;
        Integer jj = j;
        while (nn >= 0)
        {
            Integer v = x * _numberData.get(ii) + w._numberData.get(jj) + cc;
            ii = ii + 1;
            cc = (int)Math.floor((double)v / (double)0x4000000);
            w._numberData.set(jj, (int)(v & 0x3ffffff));
            jj = jj + 1;
            nn = nn - 1;
        }
        return cc;
    }

    public BigInteger multiply(BigInteger a)
    {
        BigInteger retVal = new BigInteger();
        BigInteger x = abs();
        BigInteger y = a.abs();
        retVal.t = x.t + y.t;
        retVal._numberData = new ArrayList<Integer>(Collections.nCopies(retVal.t+1, 0));
        if (y.t > 0)
        {
            for (int i=0; i<y.t; i++)
            {
                retVal._numberData.set(i+x.t, x.am(0, y._numberData.get(i), retVal, i, 0, x.t));
            }
        }
        retVal.s = 0;
        retVal.clamp();
        if (s != a.s)
        {
            retVal = new BigInteger(0).subtract(retVal);
        }
        return retVal;
    }

    public BigInteger multiplyUpper(BigInteger a, Integer n)
    {
        Integer nn = n - 1;
        BigInteger retVal = new BigInteger();
        retVal.t = t + a.t - nn;
        retVal._numberData = new ArrayList<Integer>(Collections.nCopies(retVal.t+1, 0));
        retVal.s = 0;
        Integer tt = nn-t > 0 ? nn-t : 0;
        for (int i=tt; i<a.t; i++)
        {
            retVal._numberData.set(t+i-nn, am(nn-i, a._numberData.get(i), retVal, 0, 0, t+i-nn));
        }
        retVal.clamp();
        return retVal.drShift(1);
    }

    public BigInteger multiplyLower(BigInteger a, Integer n)
    {
        BigInteger retVal = new BigInteger();
        retVal.s = 0;
        retVal.t = (t + a.t < n) ? t + a.t : n;
        retVal._numberData = new ArrayList<Integer>(Collections.nCopies(retVal.t, 0));
        Integer j = retVal.t - t;
        Integer i = 0;
        while (i < j)
        {
            retVal._numberData.set(i+t, am(0, a._numberData.get(i), retVal, i, 0, t));
            i = i + 1;
        }
        j = a.t < n ? a.t : n;
        while (i < j)
        {
            am(0, a._numberData.get(i), retVal, i, 0, n-i);
            i = i + 1;
        }
        retVal.clamp();
        return retVal;
    }

    public boolean testBit(Integer n)
    {
        Integer j = (int)Math.floor((double)n / 26);
        if (j >= t) return s != 0;
        return (_numberData.get(j) & (1 << (n % 26))) != 0;
    }

    public BigInteger square()
    {
        BigInteger retVal = new BigInteger();
        BigInteger x = abs();
        retVal.t = 2 * x.t;
        retVal._numberData = new ArrayList<Integer>(Collections.nCopies(retVal.t+1, 0));
        if (x.t > 0)
        {
            for (int i=0; i<x.t-1; i++)
            {
                Integer c = x.am(i, x._numberData.get(i), retVal, 2*i, 0, 1);
                retVal._numberData.set(i+x.t, retVal._numberData.get(i+x.t) + x.am(i+1, 2*x._numberData.get(i), retVal, 2*i+1, c, x.t-i-1));
                if (retVal._numberData.get(i+x.t) >= (1 << 26))
                {
                    retVal._numberData.set(i + x.t, retVal._numberData.get(i + x.t) - (1 << 26));
                    retVal._numberData.set(i + x.t + 1, 1);
                }
            }
        }
        if (retVal.t > 0)
        {
            retVal._numberData.set(retVal.t-1, retVal._numberData.get(retVal.t-1) + x.am(x.t-1, x._numberData.get(x.t-1), retVal, 2*(x.t-1), 0, 1));
        }
        retVal.s = 0;
        retVal.clamp();
        return retVal;
    }

    public Integer intValue()
    {
        if (s < 0)
        {
            if (t == 1)
            {
                return _numberData.get(0) - (1 << 26);
            }
            else
            {
                if (t == 0) return -1;
            }
        }
        else
        {
            if (t == 1)
            {
                return _numberData.get(0);
            }
            else
            {
                if (t == 0) return 0;
            }
        }
        return ((_numberData.get(1) & ((1 << (32 - 26)) - 1)) << 26) | _numberData.get(0);
    }
}
