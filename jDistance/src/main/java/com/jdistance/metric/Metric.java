package com.jdistance.metric;

import com.jdistance.utils.MatrixUtils;
import com.jdistance.utils.NodesDistanceDTO;
import jeigen.DenseMatrix;

import java.util.Arrays;
import java.util.List;

public enum Metric {
    PLAIN_WALK("pWalk", Scale.RHO, 0.0) {
        public DenseMatrix getD(DenseMatrix A, double t) {
            DenseMatrix H = jb.getH0Walk(A, t);
            return jb.getD(H);
        }
    },
    WALK("Walk", Scale.RHO, 0.0) {
        public DenseMatrix getD(DenseMatrix A, double t) {
            DenseMatrix H0 = jb.getH0Walk(A, t);
            DenseMatrix H = jb.H0toH(H0);
            return jb.getD(H);
        }
    },
    FOREST("For", Scale.FRACTION_REVERSED, 0.0) {
        public DenseMatrix getD(DenseMatrix A, double t) {
            DenseMatrix L = jb.getL(A);
            DenseMatrix H = jb.getH0Forest(L, t);
            return jb.getD(H);
        }
    },
    LOG_FOREST("logFor", Scale.FRACTION_REVERSED, 0.0) {
        public DenseMatrix getD(DenseMatrix A, double t) {
            DenseMatrix L = jb.getL(A);
            DenseMatrix H0 = jb.getH0Forest(L, t);
            DenseMatrix H = jb.H0toH(H0);
            return jb.getD(H);
        }
    },
    COMM("Comm", Scale.FRACTION_REVERSED, 0.0001) {
        public DenseMatrix getD(DenseMatrix A, double t) {
            DenseMatrix H = jb.getH0Communicability(A, t);
            DenseMatrix D = jb.getD(H);
            return jb.sqrtD(D);
        }
    },
    COMM_D("Comm", Scale.FRACTION_REVERSED, 0.0001) {
        public DenseMatrix getD(DenseMatrix A, double t) {
            DenseMatrix H = jb.getH0DummyCommunicability(A, t);
            DenseMatrix D = jb.getD(H);
            return jb.sqrtD(D);
        }
    },
    LOG_COMM("logComm", Scale.FRACTION_REVERSED, 0.0001) {
        public DenseMatrix getD(DenseMatrix A, double t) {
            DenseMatrix H0 = jb.getH0Communicability(A, t);
            DenseMatrix H = jb.H0toH(H0);
            DenseMatrix D = jb.getD(H);
            return jb.sqrtD(D);
        }
    },
    LOG_COMM_D("logComm", Scale.FRACTION_REVERSED, 0.0001) {
        public DenseMatrix getD(DenseMatrix A, double t) {
            DenseMatrix H0 = jb.getH0DummyCommunicability(A, t);
            DenseMatrix H = jb.H0toH(H0);
            DenseMatrix D = jb.getD(H);
            return jb.sqrtD(D);
        }
    },
    SP_CT("SP-CT", Scale.LINEAR, 0.0) {
        public DenseMatrix getD(DenseMatrix A, double lambda) {
            DenseMatrix Ds = jb.getDShortestPath(A);

            DenseMatrix L = jb.getL(A);
            DenseMatrix H = jb.getHResistance(L);
            DenseMatrix Dr = jb.getD(H);

            Double avgDs = Ds.sum().sum().s() / (Ds.cols * (Ds.cols - 1));
            Double avgDr = Dr.sum().sum().s() / (Dr.cols * (Dr.cols - 1));
            Double norm = avgDs / avgDr;
            return Ds.mul(1 - lambda).add(Dr.mul(lambda * norm));
        }
    },
    FREE_ENERGY("FE", Scale.FRACTION_BETA, 0.0001) {
        public DenseMatrix getD(DenseMatrix A, double beta) {
            return jb.getDFreeEnergy(A, beta);
        }
    },
    RSP("RSP", Scale.FRACTION_BETA, 0.0001) {
        public DenseMatrix getD(DenseMatrix A, double beta) {
            return jb.getD_RSP(A, beta);
        }
    },
    SCCT_CT("SCCT-CT", Scale.LINEAR, 0.0) {
        public DenseMatrix getD(DenseMatrix A, double lambda) {
            DenseMatrix K_CCT = jb.getH_SCCT(A);
            DenseMatrix Dcct = jb.getD(K_CCT);

            DenseMatrix L = jb.getL(A);
            DenseMatrix H = jb.getHResistance(L);
            DenseMatrix Dr = jb.getD(H);

            Double avgDcct = Dcct.sum().sum().s() / (Dcct.cols * (Dcct.cols - 1));
            Double avgDr = Dr.sum().sum().s() / (Dr.cols * (Dr.cols - 1));
            Double norm = avgDcct / avgDr;
            return Dcct.mul(1 - lambda).add(Dr.mul(lambda * norm));
        }
    };

    private static JeigenBuilder jb = new JeigenBuilder();
    private String name;
    private Scale scale;
    private double bordersOffset;

    Metric(String name, Scale scale, double bordersOffset) {
        this.name = name;
        this.scale = scale;
        this.bordersOffset = bordersOffset;
    }

    public static List<Metric> getAll() {
        return Arrays.asList(Metric.values());
    }

    public static List<MetricWrapper> getDefaultDistances() {
        return Arrays.asList(
                new MetricWrapper(Metric.FREE_ENERGY),
                new MetricWrapper(Metric.RSP),
                new MetricWrapper(Metric.COMM_D),
                new MetricWrapper(Metric.LOG_COMM_D),
                new MetricWrapper(Metric.SP_CT),
                new MetricWrapper(Metric.WALK),
                new MetricWrapper(Metric.LOG_FOREST),
                new MetricWrapper(Metric.FOREST),
                new MetricWrapper(Metric.PLAIN_WALK),
                new MetricWrapper(Metric.SCCT_CT)
        );
    }

    public String getName() {
        return name;
    }

    public Scale getScale() {
        return scale;
    }

    public double getBordersOffset() {
        return bordersOffset;
    }

    public abstract DenseMatrix getD(DenseMatrix A, double t);

    public NodesDistanceDTO getBiggestDistance(DenseMatrix A, double t) {
        NodesDistanceDTO p = new NodesDistanceDTO(0, 0, 0);
        double[][] D = MatrixUtils.toArray2(getD(A, t));
        for (int i = 0; i < D.length; i++) {
            for (int j = 0; j < D[i].length; j++) {
                if (p.getDistance() < D[i][j]) {
                    p = new NodesDistanceDTO(i, j, D[i][j]);
                }
            }
        }
        return p;
    }
}
