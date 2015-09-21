package com.jdistance.metric.impl;

import com.jdistance.metric.Distance;
import com.jdistance.metric.JeigenBuilder;
import com.jdistance.metric.Scale;
import jeigen.DenseMatrix;

public class Walk extends Distance {
    String name = "Walk";
    Scale scale = Scale.RHO;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String shortName) {
        this.name = shortName;
    }

    @Override
    public Scale getScale() {
        return scale;
    }

    @Override
    public void setScale(Scale scale) {
        this.scale = scale;
    }

    @Override
    public DenseMatrix getD(DenseMatrix A, double t) {
        JeigenBuilder db = new JeigenBuilder();
        DenseMatrix H0 = db.getH0Walk(A, t);
        DenseMatrix H = db.H0toH(H0);
        return db.getD(H);
    }
}