import java.util.Arrays;
import java.util.Objects;

public final class StringBuilderWithUndo
        implements Appendable, CharSequence, Comparable<StringBuilderWithUndo> {
    private static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    private char[] value;
    private int count;
    private static final char[] EMPTY_VALUE = new char[0];

    public StringBuilderWithUndo() {
        this.value = EMPTY_VALUE;
    }

    public StringBuilderWithUndo(int capacity) {
        value = new char[capacity];
    }

    public StringBuilderWithUndo(String str) {
        int length = str.length();
        int capacity = (length < Integer.MAX_VALUE - 16)
                ? length + 16 : Integer.MAX_VALUE;

        value = new char[capacity];
        append(str);
    }

    public StringBuilderWithUndo(CharSequence cs) {
        int length = cs.length();
        if (length < 0) {
            throw new NegativeArraySizeException("Negative length: " + length);
        }

        int capacity = (length < Integer.MAX_VALUE - 16)
                ? length + 16 : Integer.MAX_VALUE;

        value = new char[capacity];
        append(cs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringBuilderWithUndo that = (StringBuilderWithUndo) o;
        return count == that.count && Objects.deepEquals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(value), count);
    }

    @Override
    public int compareTo(StringBuilderWithUndo other) {
        if (this == other) {
            return 0;
        }
        char[] val1 = this.value;
        char[] val2 = other.value;
        int count1 = this.count;
        int count2 = other.count;

        int limit = Math.min(count1, count2);
        for (int i = 0; i < limit; i++) {
            char c1 = val1[i];
            char c2 = val2[i];
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return count1 - count2;
    }

    @Override
    public int length() {
        return count;
    }

    public int capacity() {
        return value.length;
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity > 0) {
            int oldCapacity = value.length;
            if (minCapacity - oldCapacity > 0) {
                value = Arrays.copyOf(value, newCapacity(minCapacity));
            }
        }
    }

    private int newCapacity(int newCapacity) {
        int oldCapacity = value.length;
        int growth = newCapacity - oldCapacity;
        int capacity = oldCapacity + Math.max(growth, oldCapacity + 2);
        if (capacity > 0 && capacity <= SOFT_MAX_ARRAY_LENGTH) {
            return capacity;
        } else {
            return SOFT_MAX_ARRAY_LENGTH;
        }
    }

    public void trimToSize() {
        value = Arrays.copyOf(value, count);
    }

    @Override
    public char charAt(int index) {
        checkIndex(index);
        return value[index];
    }

    @Override
    public boolean isEmpty() {
        return CharSequence.super.isEmpty();
    }

    public void setCharAt(int index, char c) {
        checkIndex(index);
        value[index] = c;
    }

    public StringBuilderWithUndo append(Object obj) {
        return append(String.valueOf(obj));
    }

    private StringBuilderWithUndo appendNull() {
        ensureCapacity(count + 4);
        int count = this.count;
        char[] val = this.value;
        val[count++] = 'n';
        val[count++] = 'u';
        val[count++] = 'l';
        val[count++] = 'l';
        this.count = count;
        return this;
    }

    public StringBuilderWithUndo append(String str) {
        if (str == null) {
            return appendNull();
        }
        int strLen = str.length();
        ensureCapacity(count + strLen);
        char[] strBytes = str.toCharArray();
        System.arraycopy(strBytes, 0, value, count, strLen);
        count += strLen;
        return this;
    }

    @Override
    public StringBuilderWithUndo append(CharSequence cs) {
        if (cs == null) {
            return appendNull();
        }
        if (cs instanceof String) {
            return this.append((String) cs);
        }
        return this.append(cs, 0, cs.length());
    }

    @Override
    public StringBuilderWithUndo append(CharSequence cs, int start, int end) {
        checkFromToIndex(cs.length(), start, end);
        if (cs == null) {
            cs = "null";
        }
        int len = end - start;
        ensureCapacity(count + len);
        if (cs instanceof String) {
            appendChars(cs, start, end);
        } else {
            appendChars(cs, start, end);
        }
        return this;
    }

    private void appendChars(CharSequence cs, int off, int end) {
        checkFromToIndex(cs.length(), off, end);
        char[] val = this.value;
        for (int i = off, j = count; i < end; i++) {
            char c = cs.charAt(i);
            val[j++] = c;
        }
        count += end - off;
    }

    private void appendChars(char[] chars, int off, int end) {
        checkFromToIndex(chars.length, off, end);
        int count = this.count;
        char[] val = this.value;
        for (int i = off, j = count; i < end; i++) {
            char c = chars[i];
            val[j++] = c;
        }
        this.count = count + end - off;
    }

    public StringBuilderWithUndo append(char[] str) {
        int len = str.length;
        ensureCapacity(count + len);
        appendChars(str, 0, len);
        return this;
    }

    public StringBuilderWithUndo append(char[] str, int off, int len) {
        checkFromToIndex(str.length, off, off + len);
        int end = off + len;
        ensureCapacity(count + len);
        appendChars(str, off, end);
        return this;
    }

    public StringBuilderWithUndo append(char c) {
        ensureCapacity(count + 1);
        value[count++] = c;
        return this;
    }

    public StringBuilderWithUndo delete(int start, int end) {
        checkIndex(start);
        int count = this.count;
        if (end > count) {
            end = count;
        }
        int len = end - start;
        if (len > 0) {
            System.out.println("end = " + end);
            System.out.println("-len = " + -len);
            shift(end, -len);
            this.count = count - len;
        }
        return this;
    }

    public StringBuilderWithUndo deleteCharAt(int index) {
        checkIndex(index);
        shift(index + 1, -1);
        count--;
        return this;
    }

    private void shift(int offset, int n) {
        System.out.println(count + n);
        System.arraycopy(value, offset, value, offset + n, count - offset);
    }

    public void clear() {
        delete(0, length());
    }

    @Override
    public String toString() {
        if (length() == 0) {
            return "";
        }
        return new String(value, 0, count);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    public String substring(int start, int end) {
        checkIndex(start);
        if (end > count) {
            end = count;
        }
        return new String(Arrays.copyOfRange(value, start, end));
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds " + count);
        }
    }

    private void checkFromToIndex(int length, int from, int to) {
        if (from < 0 || from > length || to > length) {
            throw new IndexOutOfBoundsException(
                    "Value from (" + from + ") or to(" + to + ") is out of bounds with length " + length);
        }
    }
}
