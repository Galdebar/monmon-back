package lt.galdebar.monmonapi.webscraper.services.helpers;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.max;

@Log4j2
@Component
public class StringMatcherHelper {
    private final float PHRASE_WEIGHT = 0.5f;
    private final float WORD_WEIGHT = 1f;
    private final float LENGTH_WEIGHT = -0.3f;
    private final float MIN_WEIGHT = 10f;
    private final float MAX_WEIGHT = 1f;


    public String findBestMatch(String originalString, List<String> keywords) {
        Map<Float, String> keywordsWithWeights = new HashMap<>();

        for (String keyword : keywords) {
            float matchValue = findMatchValue(originalString, keyword);
            keywordsWithWeights.put(matchValue, keyword);
        }

        TreeMap<Float, String> sorted = new TreeMap<>();


        sorted.putAll(keywordsWithWeights);
        if(sorted.size() == 0){
            return "";
        }
        Float first = sorted.firstKey();
        String result = sorted.get(first);
        log.info(
                "Original: " + originalString + ". Match: " + first + " --- " + result
        );
        return result;
    }

    private float findMatchValue(String originalName, String keyword) {

        int levensteinDistance = getLevensteinDistance(originalName, keyword);
        float adjustedLevensteinDistance = (float) (levensteinDistance -
                (0.8 * abs(originalName.length() - keyword.length())));
        int wordsDistances = getWordsDistances(originalName, keyword);

        float minValue = Math.min(PHRASE_WEIGHT * adjustedLevensteinDistance, WORD_WEIGHT * wordsDistances) * MIN_WEIGHT;
        float maxValue = max(PHRASE_WEIGHT * adjustedLevensteinDistance, WORD_WEIGHT * wordsDistances) * MAX_WEIGHT;
        float lengthValue = max(originalName.length(), keyword.length()) * LENGTH_WEIGHT;

        return minValue + maxValue + lengthValue;

    }

    private int getLevensteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private static int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }

    private int getWordsDistances(String originalName, String keyword) {
        String[] originalWords = originalName.split("\\s*(,|\\s)\\s*");
        String[] keywords = keyword.replace("&", "").split("\\s*(,|\\s)\\s*");
        int totalDistance = 0;
        for (String originalWord : originalWords) {
            int smallestDistance = keyword.length();

            for (String currentKeyword : keywords) {
                int currentDistance = getLevensteinDistance(originalWord, currentKeyword);

                if (currentDistance < smallestDistance) {
                    smallestDistance = currentDistance;
                }

                if (smallestDistance == 0) {
                    break;
                }
            }

            totalDistance = totalDistance + smallestDistance;
        }


        return totalDistance;
    }
}
