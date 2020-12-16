package project2;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Apriori {

    /** Convert method 1, utilizing Java 8 Stream logic & Java 11 Collectors. **/
    protected List<Receipt> convert(final List<Receipt> list) {
        return list.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        () -> new TreeMap<>(Comparator.comparing(Receipt::getDate)
                                .thenComparing(Receipt::getMemberNumber)),
                        Collectors.flatMapping(r -> r.getItems().stream(),
                                Collectors.toCollection(TreeSet::new))))
                .entrySet().stream()
                .map(e -> new Receipt(e.getKey().getMemberNumber(), e.getKey().getDate(), e.getValue()))
                .collect(Collectors.toList());
    }

    /** Convert method 2, utilizing TreeMap and a custom Comparator **/
//    protected List<Receipt> convert(final List<Receipt> list) {
//        Map<Receipt, Set<String>> map = new TreeMap<>(Comparator.comparing(Receipt::getDate)
//                .thenComparing(Receipt::getMemberNumber));
//        for (Receipt receipt : list) {
//            map.merge(receipt, receipt.getItems(), (v1, v2) -> {
//                Set<String> newSet = new TreeSet<>(v1);
//                newSet.addAll(v2);
//                return newSet;
//            });
//        }
//        List<Receipt> receipts = new ArrayList<>();
//        for (Map.Entry<Receipt, Set<String>> entry : map.entrySet()) {
//            receipts.add(new Receipt(entry.getKey().getMemberNumber(),
//                    entry.getKey().getDate(),
//                    entry.getValue()));
//        }
//        return receipts;
//    }

    /** Generate frequency of an input list per se, not comparing to anything. **/
    protected Map<Set<String>, Integer> getFrequency(final List<Receipt> list) {
        List<Set<String>> receipts = new ArrayList<>();
        for (Receipt r : list) {
            receipts.add(r.getItems());
        }

        Map<String, Long> map = receipts
                .stream()
                .flatMap(Set::stream)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        Map<Set<String>, Integer> newMap = new HashMap<>();
        for (Map.Entry<String, Long> e : map.entrySet()) {
            if (e.getValue() != null) {
                newMap.put(Collections.singleton(e.getKey()), Math.toIntExact(e.getValue()));
            }
        }
        return newMap;
    }

    /** Generate the frequency of the input list that shows how many times they appeared in another list. **/
    protected Map<Set<String>, Integer> getFrequency(final List<Set<String>> list, final List<Receipt> compareTo) {
        List<Set<String>> temp = new ArrayList<>();
        Map<Set<String>, Integer> map = new HashMap<>();
        for (Set<String> s : list) {
            int count = 0;
            for (int i = 0; i < compareTo.size(); i++) {
                temp.add(compareTo.get(i).getItems());
                if (temp.get(i).containsAll(s)) {
                    count++;
                }
            }
            map.put(s, count);
        }
        return map;
    }

    /** Generate a list of item frequency, but eliminate low frequency values using threshold.
     * See more details at ReadFile.java, line 97. **/
    protected Map<Set<String>, Integer> getItemFrequencyMinSup(final Map<Set<String>, Integer> map, final double threshold) {
        int minSup = getMinSup(map, threshold);
        Map<Set<String>, Integer> newMap = new HashMap<>();
        for (Map.Entry<Set<String>, Integer> e : map.entrySet()) {
            if (e.getValue() > minSup) {
                newMap.put(e.getKey(), e.getValue());
            }
        }
        return newMap;
    }

    /** Input a map (with frequency), return a new map that contains every possible 1 or 2 element(s) sub sets. **/
    protected List<Set<String>> createItemSet(final Map<Set<String>, Integer> map, final int setCount) {
        List<Set<String>> keys = new ArrayList<>(map.keySet());
        List<Set<String>> result = new ArrayList<>();
        if (setCount == 1) {
            result.addAll(map.keySet());
        } else if (setCount == 2) {
            result.addAll(keyPair2(keys));
        } else {
            System.err.println("Set count range at createItemSet() must be from 1 to 3.");
            System.exit(0);
        }
        return result;
    }

    /** Input a list with frequency containing 2 element sub sets, then return a list with
     * all possible 3 item sets ONLY IF set(i) and set(j) has at least one common element. **/
    protected List<Set<String>> createItemSet(final Map<Set<String>, Integer> map) {
        return map.keySet().stream()
                .flatMap(set1 -> map.keySet().stream()
                        .filter(set2 -> containsElement(set1, set2) && !set1.equals(set2))
                        .map(set2 -> mergeSet(set1, set2))
                ).distinct().collect(Collectors.toList());
    }

    /** Generate the rules. **/
    protected List<String> getRules(final Map<Set<String>, Integer> set2, final Map<Set<String>, Integer> set3, final double minConf) {
        List<String> result = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("##.#");
        for (Map.Entry<Set<String>, Integer> two : set2.entrySet()) {
            Set<String> temp2 = two.getKey();
            for (Map.Entry<Set<String>, Integer> three : set3.entrySet()) {
                Set<String> temp3 = three.getKey();
                if (temp3.containsAll(temp2)) {
                    double confidence = ((double)three.getValue() / two.getValue()) * 100;
                    if (confidence > minConf) {
                        temp3.removeAll(temp2);
                        String s = temp2.toString() + " -> " + temp3.toString() + " -- Confidence: " + df.format(confidence) + "%";
                        result.add(s);
                    }
                }
            }
        }
        return result;
    }

    /** Generate the minSup value. See more details at ReadFile.java, line 97. **/
    private static Integer getMinSup(final Map<Set<String>, Integer> map, final double threshold) {
        if (threshold < 0 || threshold > 1) {
            System.err.println("Input a threshold between 0 and 1.");
            System.exit(0);
        }
        int max = 0;
        for (Map.Entry<?, Integer> e : map.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
            }
        }
        return (int)(Math.floor(threshold * max));
    }

    /** Generate a list with all possible 2 item sets. **/
    private static List<Set<String>> keyPair2(final List<Set<String>> keys) {
        List<Set<String>> result = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            for (int j = i + 1; j < keys.size(); j++) {
                Set<String> temp = new HashSet<>();
                temp.addAll(keys.get(i));
                temp.addAll(keys.get(j));
                result.add(temp);
            }
        }
        return result;
    }

    /** Merge two input sets. **/
    private static <T> Set<T> mergeSet(final Set<T> a, final Set<T> b) {
        return Stream
                .of(a, b)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /** Check whether if set2 contains any elements in set1. Returns true/false. **/
    private static boolean containsElement(final Set<String> set1, final Set<String> set2) {
        return set1
                .stream()
                .anyMatch(set2::contains);
    }
}
