import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Search {

    /**
     * Search Constructor.
     * 
     * @param searchStr - String that is being searched for.
     * @param fList     - List being search in.
     */
    public Search(String searchStr, File[] fList) {
        this.searchStr = searchStr;
        this.fList = fList;
    }

    private String searchStr;
    private File[] fList;

    /**
     * Performs the search.
     * 
     * @return - Returns the search result of matches.
     */
    public ArrayList<String> searcher() {
        Map<String, Integer> distanceMap = new HashMap<>();
        /* Create list of files in directory */
        List<String> fileNameStr = new ArrayList<>();
        for (File f : fList) {
            fileNameStr.add(f.getName());
        }
        int f = 100;
        /* Calculate Levenshtein Distance */
        for (String str : fileNameStr) {
            int distance = levenshteinDis(searchStr.toUpperCase(), str.toUpperCase());
            if (f > distance) {
                f = distance;
            }
        }
        for (String str : fileNameStr) {
            int distance = levenshteinDis(searchStr.toUpperCase(), str.toUpperCase());
            if (Math.abs(f - distance) <= 10) {
                distanceMap.put(str, distance);
            }
        }
        /* Sort Map of matches */
        List<Map.Entry<String, Integer>> list = new ArrayList<>(distanceMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> x1, Map.Entry<String, Integer> x2) {
                return x1.getValue().compareTo(x2.getValue());
            }
        });
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        /* Convert to list */
        ArrayList<String> myList = new ArrayList<>(sortedMap.keySet());
        return myList;
    }

    /**
     * Calculates the Levenshtein distance between the two strings.
     * 
     * @param str1 - the searched String.
     * @param str2 - string in list.
     * @return - distance matrix.
     */
    private int levenshteinDis(String str1, String str2) {
        int str1Length = str1.length();
        int str2Length = str2.length();
        int[][] distance = new int[str1Length + 1][str2Length + 1];
        for (int i = 0; i <= str1Length; i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= str2Length; j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= str1Length; i++) {
            for (int j = 1; j <= str2Length; j++) {
                int cost = 1;
                /* if first characters match decrease cost */
                if (i == 1 && j == 1) {
                    if (str1.charAt(i) == str2.charAt(j)) {
                        cost -= 20;
                    }
                }
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    cost -= 15;
                }
                int x1 = Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1);
                int x2 = distance[i - 1][j - 1] + cost;
                distance[i][j] = Math.min(x1, x2);

            }
        }
        return distance[str1Length][str2Length];
    }
}
