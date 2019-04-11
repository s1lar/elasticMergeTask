package main.com.test.es;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        Index index = new Index(LocalDateTime.now(), "test");
        Index index1 = new Index(LocalDateTime.now().minusDays(1), "test");
        Index index2 = new Index(LocalDateTime.now().minusMonths(1), "test1");
        Index index3 = new Index(LocalDateTime.now(), "test2");
        Index index4 = new Index(LocalDateTime.now(), "test3");
        Index index5 = new Index(LocalDateTime.now().minusDays(1), "test3");
        Index index6 = new Index(LocalDateTime.now().minusDays(2), "test3");
        Index index7 = new Index(LocalDateTime.now(), "test");
        Index index8 = new Index(LocalDateTime.now().minusDays(5), "test");
        Index index9 = new Index(LocalDateTime.now().minusDays(6), "test");

        List<Index> indices = Arrays.asList(index, index1, index2, index3, index4, index5, index6, index7, index8, index9);
        mergeIndices(indices);
    }

    public static void mergeIndices(List<Index> indices) {

        ElasticClient elasticClient = new ElasticClient();
        Map<String, List<Index>> mappedIndexes = new HashMap<>();
        for (Index index : indices) {

            if (mappedIndexes.get(index.getSource()) == null){
                mappedIndexes.computeIfAbsent(index.getSource(), x -> new ArrayList<>()).add(index);
            }
            else if (!mappedIndexes.get(index.getSource()).contains(index)){
                mappedIndexes.get(index.getSource()).add(index);
            }
        }

        for (String key : mappedIndexes.keySet()) {
            Map<LocalDate, List<Index>> dateMappedIndexes = new TreeMap<>();
            List<Index> inds = mappedIndexes.get(key);

            for (Index ind : inds) {
                if (dateMappedIndexes.get(ind.getDateTime().toLocalDate()) == null){
                    dateMappedIndexes.computeIfAbsent(ind.getDateTime().toLocalDate(), x -> new ArrayList<>()).add(ind);
                }

                else if (!dateMappedIndexes.get(ind.getDateTime().toLocalDate()).contains(ind)){
                    Index index = dateMappedIndexes.get(ind.getDateTime().toLocalDate()).get(0);
                    //merging into a day range
                    elasticClient.merge(index, ind);
                    dateMappedIndexes.get(ind.getDateTime().toLocalDate()).add(ind);
                }
            }

            List<Index> weekIndexes = mergePeriod(LocalDate.now().minusDays(7), LocalDate.now(), dateMappedIndexes);

            Index weekInitialIndex = weekIndexes.get(0);
            weekIndexes.remove(weekInitialIndex);
            weekIndexes.forEach(ind -> elasticClient.merge(weekInitialIndex, ind));

            List<Index> monthIndexes = mergePeriod(LocalDate.now().minusMonths(1), LocalDate.now(), dateMappedIndexes);

            Index monthInitialIndex = monthIndexes.get(0);
            monthIndexes.remove(monthInitialIndex);
            monthIndexes.forEach(ind -> elasticClient.merge(monthInitialIndex, ind));

        }

    }

    private static List<Index> mergePeriod(LocalDate from, LocalDate to, Map<LocalDate, List<Index>> indexes){
        List<Index> indices = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Index>> entry : indexes.entrySet()) {
            if(entry.getKey().isEqual(from) || entry.getKey().isAfter(from) || entry.getKey().isBefore(to) || entry.getKey().isEqual(to)){
                indices.addAll(entry.getValue());
            }
        }
        return indices;
    }
}
