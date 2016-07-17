package com.jdistance.local.workflow;

import com.jdistance.measure.AbstractMeasureWrapper;
import com.jdistance.measure.Scale;
import com.jdistance.graph.GraphBundle;
import com.jdistance.learning.Estimator;
import com.jdistance.learning.Scorer;
import com.jdistance.local.workflow.gridsearch.GridSearch;

public class Task {
    private String name;
    private GridSearch gridSearch;
    private GraphBundle graphs;

    public Task(String name, Estimator estimator, Scorer scorer, AbstractMeasureWrapper metricWrapper, GraphBundle graphs, Integer pointsCount) {
        this(name, new GridSearch(name, estimator, metricWrapper, scorer, 0.0, 1.0, pointsCount,
                        Context.getInstance().isParallelGrid()),
                graphs);
    }

    public Task(Estimator estimator, Scorer scorer, AbstractMeasureWrapper metricWrapper, GraphBundle graphs, Integer pointsCount) {
        this(estimator.getName() + " " + scorer.getName() + " " + metricWrapper.getName(),
                new GridSearch(estimator.getName() + " " + scorer.getName() + " " + metricWrapper.getName(),
                        estimator, metricWrapper, scorer, 0.0, 1.0, pointsCount, Context.getInstance().isParallelGrid()),
                graphs);
    }

    public Task(Estimator estimator, Scorer scorer, AbstractMeasureWrapper metricWrapper, GraphBundle graphs, Double from, Double to, Integer pointsCount) {
        this(estimator.getName() + " " + scorer.getName() + " " + metricWrapper.getName(),
                new GridSearch(estimator.getName() + " " + scorer.getName() + " " + metricWrapper.getName(),
                        estimator, metricWrapper, scorer, from, to, pointsCount, Context.getInstance().isParallelGrid()),
                graphs);
        metricWrapper.setScale(Scale.LINEAR);
    }

    private Task(String name, GridSearch gridSearch, GraphBundle graphs) {
        this.name = name;
        this.gridSearch = gridSearch;
        this.graphs = graphs;
    }

    public String getName() {
        return name;
    }

    public Task execute() {
        gridSearch.fit(graphs);
        return this;
    }

    public GridSearch getGridSearch() {
        return gridSearch;
    }
}
