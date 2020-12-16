package project2;

import java.io.*;
import java.util.*;

public class ReadFile {

    private static List<Receipt> readFile(String fileName) {
        List<Receipt> receipts = new ArrayList<>();
        try {
            var reader = new BufferedReader(new FileReader(fileName));

            // Move past the first title line.
            reader.readLine();

            // Register each line into variable.
            String line = reader.readLine();

            // Start reading from second line till EOF (null), split each string at ","
            // and create a Receipt object for each line.
            while (line != null) {
                String[] attribute = line.split(",");
                String memberNumber = attribute[0];
                String date = attribute[1];
                Set<String> items = new HashSet<>();
                items.add(attribute[2]);
                Receipt temp = new Receipt(memberNumber, date, items);
                receipts.add(temp);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receipts;
    }

    private static void parse(final Map<Set<String>, Integer> map, final String fileName) {
        try {
            PrintWriter output = new PrintWriter(fileName);
            map.entrySet().forEach(output::println);
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void parse(final List<?> list, final String fileName) {
        try {
            PrintWriter output = new PrintWriter(fileName);
            for (var s : list) {
                output.println(s);
            }
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /** WARNING: Don't parse unnecessarily unless the file absolutely needs to be updated to shorten
     * compile time and save heap memory.*/
    private static void solve() {

        // Read the input. Sorting will potentially increase processing speed for other tasks below. Refer to:
        // https://stackoverflow.com/questions/11227809/why-is-processing-a-sorted-array-faster-than-processing-an-unsorted-array?rq=1
        var input = readFile("Groceries_dataset.csv");

        // Initialize the data mining algorithm class.
        Apriori a = new Apriori();

        // Convert the input into merged form (same member number & purchase date will have the item set merged)
        // and perform a quick sort on the input List<Receipt> based on member number.
        var converted = a.convert(input);
        QuickSort q = new QuickSort();
        q.quickSort(converted);

        // Declare the global cut off threshold. Calculate minimum support (minSup) using this threshold, ranging
        // from 0 to 1 (ex: 0.2 = 20%). Items with frequency below the x(th) percentile of the item with highest
        // frequency will be eliminated from the data set.
        double threshold = 0.1;

        // Create a map with (key = itemName, value = itemFrequency) from the new List<Receipt> above.
        var itemFrequency = a.getFrequency(converted);

        // Create a new map with key = itemName, value = itemFrequency using minSup to eliminate low frequency items.
        var itemFrequencyMinSup = a.getItemFrequencyMinSup(itemFrequency, threshold);

        // Use the minSup map to create a list of all possible pairings with other keys as Set<String> in a List<Set<String>>.
        // SetCount = 3 will cause heap space memory issue.
        var itemSet1 = a.createItemSet(itemFrequencyMinSup, 1);

        // Create a new map (key = itemSet, value = setFrequency) to count set frequency compared to the original input.
        var itemSet1Frequency = a.getFrequency(itemSet1, converted);

        // Create a new map with key = itemSet, value = setFrequency and use minSup to eliminate low frequency
        // items again (optional, since low frequency items are already eliminated from earlier. Set 0?).
        var itemSet1MinSup = a.getItemFrequencyMinSup(itemSet1Frequency, threshold);

        // Use the list with 1 item sets and generate all possible 2 item sets from it (no duplicate items in a set).
        var itemSet2 = a.createItemSet(itemSet1MinSup, 2);

        // Get frequency of the 2 item sets list compared to the original input.
        var itemSet2Frequency = a.getFrequency(itemSet2, converted);

        // Get frequency of the 2 item sets list compared to the original input, applying threshold to eliminate
        // low frequency items.
        var itemSet2MinSup = a.getItemFrequencyMinSup(itemSet2Frequency, threshold);

        // Generate a list with all possible 3 item sets ONLY IF set(i) and set(j) has at least one common element.
        var itemSet3 = a.createItemSet(itemSet2MinSup);

        // Get frequency of the 3 item sets list compared to the original input.
        var itemSet3Frequency = a.getFrequency(itemSet3, converted);

        // Get frequency of the 3 item sets list compared to the original input, applying threshold
        // to eliminate low frequency items again.
        var itemSet3MinSup = a.getItemFrequencyMinSup(itemSet3Frequency, threshold);

        // Generate the rules.
        var ruleSet = a.getRules(itemSet2MinSup, itemSet3MinSup, 20);

        // Parse the files.
        parse(converted, "itemConverted.txt");
        parse(itemFrequency, "itemFrequency.txt");
        parse(itemFrequencyMinSup, "itemFrequencyMinSup.txt");
        parse(itemSet1, "itemSet1.txt");
        parse(itemSet1Frequency, "itemSet1Frequency.txt");
        parse(itemSet1MinSup, "itemSet1MinSup.txt");
        parse(itemSet2, "itemSet2.txt");
        parse(itemSet2Frequency, "itemSet2Frequency.txt");
        parse(itemSet2MinSup, "itemSet2MinSup.txt");
        parse(itemSet3, "itemSet3.txt");
        parse(itemSet3Frequency, "itemSet3Frequency.txt");
        parse(itemSet3MinSup, "itemSet3MinSup.txt");
        parse(ruleSet, "ruleSet.txt");
    }

    public static void main(String[] args) {
//        long startTime = System.nanoTime() / 1000000;
        solve();
//        long endTime = System.nanoTime() / 1000000;
//        long duration = (endTime - startTime);
//        System.out.println(duration + " ms");
    }
}