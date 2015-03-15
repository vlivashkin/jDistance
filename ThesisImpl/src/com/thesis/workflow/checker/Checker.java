package com.thesis.workflow.checker;

import com.thesis.adapter.parser.graph.Graph;
import com.thesis.adapter.parser.graph.SimpleNodeData;
import com.thesis.metric.Distance;
import com.thesis.metric.Scale;
import com.thesis.utils.Cloneable;
import org.jblas.DoubleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Checker implements Cloneable {
    private static final Logger log = LoggerFactory.getLogger(Checker.class);

    public abstract List<Graph> getGraphs();

    public Map<Double, Double> seriesOfTests(final Distance distance, Double from, Double to, Double step, Scale scale) {
        int countOfPoints = (int) Math.round(Math.floor((to - from) / step) + 1);

        final Map<Double, Double> results = new ConcurrentHashMap<>();

        Date start = new Date();
        log.info("Start {}", distance.getName());

        IntStream.range(0, countOfPoints).boxed().collect(Collectors.toList()).forEach(idx -> {
            Double base = from + idx * step;
            Double i = scale.calc(base);
            Double result = test(distance, i);
            results.put(base, result);
        });
        Date finish = new Date();
        long diff = finish.getTime() - start.getTime();
        log.info("End {}; time: {} ms", distance.getName(), diff);

        return results;
    }

    public Double test(Distance distance, Double parameter) {
        Integer total = 0;
        Integer countErrors = 0;
        Integer coloredNodes = 0;

        if (parameter < 0.001) {
            parameter = 0.001;
        }

        for (Graph graph : getGraphs()) {
            ArrayList<SimpleNodeData> simpleNodeData = graph.getSimpleNodeData();

            DoubleMatrix A = graph.getSparseMatrix();
            DoubleMatrix D = distance.getD(A, parameter);
            Integer[] result = roundErrors(D, simpleNodeData);

            total += result[0];
            countErrors += result[1];
            coloredNodes += result[2];
        }

        Double rate = rate((double) countErrors, (double) total, coloredNodes);
        log.info("{}: {} {}", distance.getShortName(), parameter, rate);

        return rate;
    }

    protected abstract Integer[] roundErrors(DoubleMatrix D, ArrayList<SimpleNodeData> simpleNodeData);

    protected abstract Double rate(Double countErrors, Double total, Integer coloredNodes);

    public abstract Checker clone();
}