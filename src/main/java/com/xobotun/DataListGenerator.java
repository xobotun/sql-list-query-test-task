package com.xobotun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DataListGenerator {
    private static final Logger log = LoggerFactory.getLogger(DataListGenerator.class);

    private static final int LIST_NUMBER = 100;
    private static final Random rnd = new Random();

    public static List<Set<Integer>> generateSets(int listSize, int maxId) {
        List<Set<Integer>> result = new ArrayList<>(LIST_NUMBER);

        for (int list = 0; list < LIST_NUMBER; list++) {
            Set<Integer> set = new HashSet<>(listSize, 1.0f);
            // counting up not `set.size()`, but num. Just in case maxId < listSize
            for (int num = 0; num < listSize; num++) set.add(rnd.nextInt(maxId) + 1);
            result.add(set);
        }

        if (log.isDebugEnabled()) {
            log.debug("Generated {} sets of following dimensions: {}", result.size(), result.stream().map(Set::size).sorted().toList());
        }

        return result;
    }
}
