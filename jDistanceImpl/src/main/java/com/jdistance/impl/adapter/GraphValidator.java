package com.jdistance.impl.adapter;

import com.jdistance.graph.Graph;
import com.jdistance.graph.SimpleNodeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphValidator {
    private static final Logger log = LoggerFactory.getLogger(GraphValidator.class);

    public static boolean validate(Graph graph, int numOfNodes, int numOfClusters, double deviation) {
        List<SimpleNodeData> data = graph.getSimpleNodeData();
        boolean valid = true;

        // validate number of nodes
        if (data.size() < numOfNodes * (1 - deviation) || data.size() > numOfNodes * (1 + deviation)) {
            log.info("Validation failed: numOfNodes - expected: {} but {} found", numOfNodes, data.size());
            valid = false;
        }

        // validate number of clusters
        Set<String> labels = new HashSet<>();
        data.forEach(item -> labels.add(item.getLabel()));
        if (labels.size() != numOfClusters) {
            log.info("Validation failed: numOfClusters - expected: {} but {} found", numOfClusters, labels.size());
            valid = false;
        }

        return valid;
    }
}
