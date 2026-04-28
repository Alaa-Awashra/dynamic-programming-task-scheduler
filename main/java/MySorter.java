public class MySorter {

    public static <T> void heapSort(MyArrayList<T> list, MyComparator<T> cmp) {
        int n = list.size();
        if (n <= 1) return;

        // Build max-heap
        for (int i = (n / 2) - 1; i >= 0; i--) {
            heapify(list, cmp, i, n);
        }

        // Extract elements from heap one by one
        for (int end = n - 1; end > 0; end--) {
            swap(list, 0, end);
            heapify(list, cmp, 0, end);
        }
    }

    private static <T> void heapify(MyArrayList<T> list, MyComparator<T> cmp, int i, int n) {
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int largest = i;

            if (left < n && cmp.compare(list.get(left), list.get(largest)) > 0) {
                largest = left;
            }
            if (right < n && cmp.compare(list.get(right), list.get(largest)) > 0) {
                largest = right;
            }
            if (largest == i) return;

            swap(list, i, largest);
            i = largest;
        }
    }

    private static <T> void swap(MyArrayList<T> list, int i, int j) {
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }
}
