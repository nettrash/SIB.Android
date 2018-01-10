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
                dMultiply(Math.pow(10, 7));
                dAddOffset(w, 0);
                j = 0;
                w = 0;
            }
        }
        if (j > 0)
        {
            dMultiply(Math.pow(10, j));
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
                c += _numberData.get(i)
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
        Integer m = a.t < self.t ? a.t : self.t;
        while (i < m)
        {
            c = c + _numberData.get(i) - a._numberData.get(i);
            retVal._numberData.set(i, c & ((1 << 26) - 1));
            i = i + 1;
            c = c >> 26;
        }
        if (a.t < self.t)
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

    public BigInteger mod(BigInteger a)
    {
        BigInteger retVal = abs().div(a);
        if (s < 0 && retVal.compareTo(new BigInteger(0)) > 0)
        {
            retVal = a.subtract(retVal);
        }
        return retVal;
    }

    public func modInverse(_ m: BigInteger) -> BigInteger {
        let ac = m.isEven()
        if (isEven() && ac) || m.signum() == 0 {
            return BigInteger(0)
        }
        var u = m.clone()
        var v = clone()
        var a = BigInteger(1)
        var b = BigInteger(0)
        var c = BigInteger(0)
        var d = BigInteger(1)

        while u.signum() != 0 {
            while u.isEven() {
                u = u.rShift(1)
                if ac {
                    if !a.isEven() || !b.isEven() {
                        a = a.add(self)
                        b = b.subtract(m)
                    }
                    a = a.rShift(1)
                } else {
                    if !b.isEven() {
                        b = b.subtract(m)
                    }
                }
                b = b.rShift(1)
            }
            while v.isEven() {
                v = v.rShift(1)
                if ac {
                    if !c.isEven() || !d.isEven() {
                        c = c.add(self)
                        d = d.subtract(m)
                    }
                    c = c.rShift(1)
                } else {
                    if !d.isEven() {
                        d = d.subtract(m)
                    }
                }
                d = d.rShift(1)
            }
            if u.compareTo(v) >= 0 {
                u = u.subtract(v)
                if ac {
                    a = a.subtract(c)
                }
                b = b.subtract(d)
            } else {
                v = v.subtract(u)
                if ac {
                    c = c.subtract(a)
                }
                d = d.subtract(b)
            }
        }
        if v.compareTo(BigInteger(1)) != 0 {
            return BigInteger(0)
        }
        if d.compareTo(m) >= 0 {
            return d.subtract(m)
        }
        if d.signum() < 0 {
            d = d.add(m)
        } else {
            return d
        }
        if d.signum() < 0 {
            return d.add(m)
        } else {
            return d
        }
    }

    public func lShift(_ n: Int) -> BigInteger {
        let bs = n % 26
        let cbs = 26 - bs
        let bm = (1 << cbs) - 1
        let ds = Int(floor(Double(n) / 26))
        var c = (s << bs) & ((1 << 26) - 1)
        let r: BigInteger = BigInteger()
        r._numberData = [Int](repeating: 0, count: t+ds+2)
        if t > 0 {
            for i in (0...t-1).reversed() {
                r._numberData[i+ds+1] = (_numberData[i] >> cbs) | c
                c = (_numberData[i] & bm) << bs
            }
        }
        if ds > 0 {
            for i in (0...ds-1).reversed() {
                r._numberData[i] = 0
            }
        }
        r._numberData[ds] = c
        r.t = t + ds + 1
        r.s = s
        r.clamp()
        return r
    }

    public func dlShift(_ n: Int) -> BigInteger {
        let retVal: BigInteger = BigInteger()
        retVal._numberData = [Int](repeating: 0, count: t+n+1)
        for i in 0...t-1 {
            retVal._numberData[i+n] = _numberData[i]
        }
        if n > 0 {
            for i in 0...n-1 {
                retVal._numberData[i] = 0
            }
        }
        retVal.t = t + n
        retVal.s = s
        return retVal
    }

    public func rShift(_ n: Int) -> BigInteger {
        let retVal : BigInteger = BigInteger()
        retVal.s = s
        let ds = Int(floor(Double(n) / Double(26)))
        if ds > t {
            retVal.t = 0
            return retVal
        }
        retVal._numberData = [Int](repeating: 0, count: t-ds+1)
        let bs = n % 26
        let cbs = 26 - bs
        let bm = (1 << bs) - 1
        retVal._numberData[0] = _numberData[ds] >> bs
        if t > 0 {
            for i in (ds+1)..<t {
                retVal._numberData[i-ds-1] |= (_numberData[i] & bm) << cbs
                retVal._numberData[i-ds] = _numberData[i] >> bs
            }
            if bs > 0 {
                retVal._numberData[t - ds - 1] |= (s & bm) << cbs
            }
        }
        retVal.t = t - ds
        retVal.clamp()
        return retVal
    }

    public func drShift(_ n: Int) -> BigInteger {
        let retVal: BigInteger = BigInteger()
        let tt = t-n > 0 ? t-n : 0
        retVal._numberData = [Int](repeating: 0, count: tt)
        if tt > 0 {
            for i in n..<t {
                retVal._numberData[i-n] = _numberData[i]
            }
        }
        retVal.t = tt
        retVal.s = s
        return retVal
    }

    public func dMultiply(_ n: Int) -> Void {
        if t < _numberData.count {
            _numberData[t] = am(0, n-1, self, 0, 0, t)
        } else {
            _numberData.append(am(0, n-1, self, 0, 0, t))
        }
        t = t + 1
        clamp()
    }

    public func dAddOffset(_ n: Int, _ w: Int) -> Void {
        if n == 0 { return }
        var ww = w
        while t <= ww {
            _numberData.insert(0, at: t)
            t = t + 1
        }
        _numberData[ww] += n
        while _numberData[ww] >= 1 << 26 {
            _numberData[ww] -= 1 << 26
            ww = ww + 1
            if ww > t {
                _numberData.insert(0, at: t)
                t = t + 1
            }
            _numberData[ww] = _numberData[ww] + 1
        }
    }

    public func shiftLeft(_ n: Int) -> BigInteger {
        if n < 0 {
            return rShift(-n)
        } else {
            return lShift(n)
        }
    }

    public func shiftRight(_ n: Int) -> BigInteger {
        if n < 0 {
            return lShift(-n)
        } else {
            return rShift(n)
        }
    }

    public func div(_ m: BigInteger) throws -> BigInteger {
        let pm = m.abs()
        if pm.t <= 0 { throw Exception.DivisionByZero }
        let pt = self.abs()
        if pt.t < pm.t {
            return self
        }
        var y: BigInteger
        let ts = self.s

        let nsh = 26 - pm._numberData[pm.t - 1].nbits
        var r: BigInteger
        if nsh > 0 {
            y = pm.lShift(nsh)
            r = pt.lShift(nsh)
        } else {
            y = pm
            r = pt
        }

        let ys = y.t
        let y0 = y._numberData[ys - 1]
        if y0 == 0 { return r}
        let yt = (y0 * (1 << (52 - 26))) + Int((ys > 1 ? y._numberData[ys - 2] >> (2 * 26 - 52) : 0))
        let d1 = pow(2, 52) / Double(yt)
        let d2 = Double(1 << (52 - 26)) / Double(yt)
        let e = 1 << (2 * 26 - 52)

        var i = r.t
        var j = i - ys
        var tt = y.dlShift(j)
        if r.compareTo(tt) >= 0 {
            r._numberData[r.t] = 1
            r.t = r.t + 1
            r = r.subtract(tt)
        }
        tt = BigInteger(1).dlShift(ys)
        y = tt.subtract(y)
        while y.t < ys {
            y._numberData[y.t] = 0
            y.t = y.t + 1
        }
        j = j - 1
        while j >= 0 {
            i = i - 1
            var qd: Int = 0
            if r._numberData[i] == y0 {
                qd = (1 << 26) - 1
            } else {
                let k1: Double = Double(r._numberData[i]) * d1
                let k2: Double = Double(r._numberData[i-1] + e) * d2
                qd = Int(floor(k1 + k2))
            }
            r._numberData[i] = r._numberData[i] + y.am(0, qd, r, j, 0, ys)
            if r._numberData[i] < qd {
                tt = y.dlShift(j)
                r = r.subtract(tt)
            }
            qd = qd - 1
            while r._numberData[i] < qd {
                r = r.subtract(tt)
                qd = qd - 1
            }
            j = j - 1
        }
        r.t = ys
        r.clamp()
        if nsh > 0 {
            r = r.rShift(nsh)
        }
        if ts < 0 {
            r = BigInteger(0).subtract(r)
        }
        return r
    }

    public func divide(_ m: BigInteger) throws -> BigInteger {
        let pm = m.abs()
        if pm.t <= 0 { throw Exception.DivisionByZero }
        let pt = self.abs()
        if pt.t < pm.t {
            return BigInteger(0)
        }
        var y: BigInteger
        let ts = self.s
        let ms = m.s

        let nsh = 26 - pm._numberData[pm.t - 1].nbits
        var r: BigInteger
        if nsh > 0 {
            y = pm.lShift(nsh)
            r = pt.lShift(nsh)
        } else {
            y = pm
            r = pt
        }

        let ys = y.t
        let y0 = y._numberData[ys - 1]
        if y0 == 0 { return r}
        let yt = (y0 * (1 << (52 - 26))) + Int((ys > 1 ? y._numberData[ys - 2] >> (2 * 26 - 52) : 0))
        let d1 = pow(2, 52) / Double(yt)
        let d2 = Double(1 << (52 - 26)) / Double(yt)
        let e = 1 << (2 * 26 - 52)

        var i = r.t
        var j = i - ys
        var tt = y.dlShift(j)
        if r.compareTo(tt) >= 0 {
            r._numberData[r.t] = 1
            r.t = r.t + 1
            r = r.subtract(tt)
        }
        tt = BigInteger(1).dlShift(ys)
        y = tt.subtract(y)
        while y.t < ys {
            y._numberData[y.t] = 0
            y.t = y.t + 1
        }
        j = j - 1
        while j >= 0 {
            i = i - 1
            var qd: Int = 0
            if r._numberData[i] == y0 {
                qd = (1 << 26) - 1
            } else {
                let k1: Double = Double(r._numberData[i]) * d1
                let k2: Double = Double(r._numberData[i-1] + e) * d2
                qd = Int(floor(k1 + k2))
            }
            r._numberData[i] = r._numberData[i] + y.am(0, qd, r, j, 0, ys)
            if r._numberData[i] < qd {
                tt = y.dlShift(j)
                r = r.subtract(tt)
            }
            qd = qd - 1
            while r._numberData[i] < qd {
                r = r.subtract(tt)
                qd = qd - 1
            }
            j = j - 1
        }
        r = r.drShift(ys)
        if ts != ms {
            r = BigInteger(0).subtract(r)
        }
        return r
    }

    public func am(_ i: Int, _ x: Int, _ w: BigInteger, _ j: Int, _ c: Int, _ n: Int) -> Int {
        var nn = n - 1
        var ii = i
        var cc = c
        var jj = j
        while nn >= 0 {
            let v = x * _numberData[ii] + w._numberData[jj] + cc
            ii = ii + 1
            cc = Int(floor(Double(v) / Double(0x4000000)))
            w._numberData[jj] = Int(v & 0x3ffffff)
            jj = jj + 1
            nn = nn - 1
        }
        return cc
    }

    public func multiply(_ a: BigInteger) -> BigInteger {
        var retVal: BigInteger = BigInteger()
        let x = abs()
        let y = a.abs()
        retVal.t = x.t + y.t
        retVal._numberData = [Int](repeating: 0, count: retVal.t+1)
        if y.t > 0 {
            for i in 0...y.t-1 {
                retVal._numberData[i+x.t] = x.am(0, y._numberData[i], retVal, i, 0, x.t)
            }
        }
        retVal.s = 0
        retVal.clamp()
        if s != a.s {
            retVal = BigInteger(0).subtract(retVal)
        }
        return retVal
    }

    public func multiplyUpper(_ a: BigInteger, _ n: Int) -> BigInteger {
        let nn = n - 1
        let retVal: BigInteger = BigInteger()
        retVal.t = t + a.t - nn
        retVal._numberData = [Int](repeating: 0, count: retVal.t+1)
        retVal.s = 0
        let tt = nn-t > 0 ? nn-t : 0
        for i in tt..<a.t {
            retVal._numberData[t+i-nn] = am(nn-i, a._numberData[i], retVal, 0, 0, t+i-nn)
        }
        retVal.clamp()
        return retVal.drShift(1)
    }

    public func multiplyLower(_ a: BigInteger, _ n: Int) -> BigInteger {
        let retVal: BigInteger = BigInteger()
        retVal.s = 0
        retVal.t = (t + a.t < n) ? t + a.t : n
        retVal._numberData = [Int](repeating: 0, count: retVal.t)
        var j = retVal.t - t
        var i = 0
        while i < j {
            retVal._numberData[i+t] = am(0, a._numberData[i], retVal, i, 0, t)
            i = i + 1
        }
        j = a.t < n ? a.t : n
        while i < j {
            let _ = am(0, a._numberData[i], retVal, i, 0, n-i)
            i = i + 1
        }
        retVal.clamp()
        return retVal
    }

    public func testBit(_ n: Int) -> Bool {
        let j = Int(floor(Double(n / 26)))
        if j >= t { return s != 0 }
        return _numberData[j] & (1 << (n % 26)) != 0
    }

    public func square() -> BigInteger {
        let retVal = BigInteger()
        let x = abs()
        retVal.t = 2 * x.t
        retVal._numberData = [Int](repeating:0, count:retVal.t+1)
        if x.t > 0 {
            for i in 0..<x.t-1 {
                let c = x.am(i, x._numberData[i], retVal, 2*i, 0, 1)
                retVal._numberData[i+x.t] += x.am(i+1, 2*x._numberData[i], retVal, 2*i+1, c, x.t-i-1)
                if retVal._numberData[i+x.t] >= 1 << 26 {
                    retVal._numberData[i + x.t] -= 1 << 26
                    retVal._numberData[i + x.t + 1] = 1
                }
            }
        }
        if retVal.t > 0 {
            retVal._numberData[retVal.t-1] += x.am(x.t-1, x._numberData[x.t-1], retVal, 2*(x.t-1), 0, 1)
        }
        retVal.s = 0
        retVal.clamp()
        return retVal
    }

    public func intValue() -> Int {
        if s < 0 {
            if t == 1 {
                return _numberData[0] - (1 << 26)
            } else {
                if t == 0 { return -1 }
            }
        } else {
            if t == 1 {
                return _numberData[0]
            } else {
                if t == 0 { return 0 }
            }
        }
        return ((_numberData[1] & ((1 << (32 - 26)) - 1)) << 26) | _numberData[0]
    }
}
