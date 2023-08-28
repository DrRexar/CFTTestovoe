package src.main.java.org.example;

import java.io.*;
import java.util.*;

public class MergeSortFiles {
    public static void main(String[] args) {
        // Проверяем наличие аргументов командной строки
        if (args.length < 3) {
            System.err.println("Использование: java MergeSortFiles [-a/-d] -s/-i output.txt input1.txt input2.txt");
            return;
        }

        String sortingMode = "-a"; // Режим сортировки по умолчанию (по возрастанию)
        String dataType = "";
        String outputFileName = "";
        List<String> inputFiles = new ArrayList<>();

        int argIndex = 0;

        // Если указан режим сортировки
        if (args[argIndex].equals("-a") || args[argIndex].equals("-d")) {
            sortingMode = args[argIndex++];
        }

        // Определение типа данных
        if (args[argIndex].equals("-s") || args[argIndex].equals("-i")) {
            dataType = args[argIndex++];
        }

        // Имя выходного файла
        outputFileName = args[argIndex++];

        // Имена входных файлов
        while (argIndex < args.length) {
            inputFiles.add(args[argIndex++]);
        }

        try {
            mergeSortFiles(sortingMode, dataType, inputFiles, outputFileName);
            System.out.println("Файлы объединены и отсортированы успешно.");
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    public static <T extends Comparable<T>> void mergeSortFiles(String sortingMode, String dataType, List<String> inputFiles, String outputFileName) throws IOException {
        List<BufferedReader> readers = new ArrayList<>();
        for (String inputFile : inputFiles) {
            readers.add(new BufferedReader(new FileReader(inputFile)));
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));

        Comparator<Pair<T, Integer>> ascendingComparator = Comparator.comparing(Pair::getKey);
        Comparator<Pair<T, Integer>> descendingComparator = (a, b) -> {
            int comparison = b.getKey().compareTo(a.getKey());
            if (comparison != 0) {
                return comparison;
            }
            // Если ключи равны, используйте индекс файла для сравнения
            return a.getValue().compareTo(b.getValue());
        };

        PriorityQueue<Pair<T, Integer>> minHeap;
        if (sortingMode.equals("-a")) {
            minHeap = new PriorityQueue<>(ascendingComparator);
        } else {
            minHeap = new PriorityQueue<>(descendingComparator);
        }

        // Чтение начальных значений из каждого файла и добавление их в кучу
        for (int i = 0; i < readers.size(); i++) {
            String line = readers.get(i).readLine();
            if (line != null) {
                T value = parseValue(line); // Преобразование строки в нужный тип (целое число или строка)
                minHeap.offer(new Pair<>(value, i));
            }
        }

        // Сортировка слиянием
        while (!minHeap.isEmpty()) {
            Pair<T, Integer> maxPair = minHeap.poll();
            T minValue = maxPair.getKey();
            int readerIndex = maxPair.getValue();
            writer.write(minValue.toString());
            writer.newLine();

            // Прочитать следующее значение из соответствующего файла и добавить его в кучу
            String nextLine = readers.get(readerIndex).readLine();
            if (nextLine != null) {
                T nextValue = parseValue(nextLine);
                minHeap.offer(new Pair<>(nextValue, readerIndex));
            }
        }

        // Закрыть все потоки
        for (BufferedReader reader : readers) {
            reader.close();
        }
        writer.close();
    }

    // Метод для преобразования строки в нужный тип (целое число или строка)
    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> T parseValue(String line) {
        try {
            return (T) Integer.valueOf(line);
        } catch (NumberFormatException e) {
            // Если не удалось преобразовать в целое число, оставляем как строку
            return (T) line;
        }
    }
}

class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}

