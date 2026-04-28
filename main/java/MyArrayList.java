public class MyArrayList<T> {

    private Object[] data;
    private int size;

    public MyArrayList() {
        this(10);
    }

    public MyArrayList(int initialCapacity) {
        if (initialCapacity < 1)
            initialCapacity = 1;
        this.data = new Object[initialCapacity];
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < size; i++)
            data[i] = null;
        size = 0;
    }

    public void add(T value) {
        ensureCapacity(size + 1);
        data[size++] = value;
    }

    public void add(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("add index: " + index + ", size: " + size);
        }
        ensureCapacity(size + 1);

        // shift right
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }

        data[index] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        rangeCheck(index);
        return (T) data[index];
    }

    public void set(int index, T value) {
        rangeCheck(index);
        data[index] = value;
    }

    @SuppressWarnings("unchecked")
    public T removeAt(int index) {
        rangeCheck(index);

        T removed = (T) data[index];

        // shift left
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }

        data[size - 1] = null;
        size--;
        return removed;
    }


    private void rangeCheck(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length)
            return;

        int newCap = data.length * 2;
        if (newCap < minCapacity)
            newCap = minCapacity;

        Object[] newData = new Object[newCap];
        for (int i = 0; i < size; i++)
            newData[i] = data[i];
        data = newData;
    }
}
