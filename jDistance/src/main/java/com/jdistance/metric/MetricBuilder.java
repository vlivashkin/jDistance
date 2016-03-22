package com.jdistance.metric;

import com.jdistance.utils.MatrixUtils;
import com.keithschwarz.johnsons.JohnsonsAlgorithm;
import jeigen.DenseMatrix;

import java.util.function.UnaryOperator;

import static jeigen.Shortcuts.*;

public class MetricBuilder {
    private static DenseMatrix log(DenseMatrix A) {
        return elementWise(A, Math::log);
    }

    private static DenseMatrix exp(DenseMatrix A) {
        return elementWise(A, Math::exp);
    }

    private static DenseMatrix elementWise(DenseMatrix A, UnaryOperator<Double> operator) {
        double[][] values = MatrixUtils.toArray2(A);
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                values[i][j] = operator.apply(values[i][j]);
            }
        }
        return new DenseMatrix(values);
    }

    private static DenseMatrix diagToVector(DenseMatrix A) {
        DenseMatrix diag = new DenseMatrix(A.rows, 1);
        double[] values = A.getValues();
        for (int i = 0; i < A.rows; i++) {
            diag.set(i, values[i * (A.cols + 1)]);
        }
        return diag;
    }

    private static DenseMatrix pinv(DenseMatrix A) {
        if (A.cols != A.rows) {
            throw new RuntimeException("pinv matrix size error: must be square matrix");
        }

        return A.fullPivHouseholderQRSolve(diag(ones(A.cols, 1)));
    }

    private static DenseMatrix dummy_mexp(DenseMatrix A, int nSteps) {
        DenseMatrix totalSum = eye(A.rows);
        DenseMatrix currentElement = eye(A.rows);

        for (int i = 1; i <= nSteps; i++) {
            currentElement = currentElement.mmul(A.div(i));
            totalSum = totalSum.add(currentElement);
        }
        return totalSum;
    }

    public static DenseMatrix normalization(DenseMatrix dm) {
        Double avg = dm.sum().sum().s() / (dm.cols * (dm.cols - 1));
        return dm.div(avg);
    }

    static DenseMatrix sqrtD(DenseMatrix D) {
        return D.sqrt();
    }

    public DenseMatrix getL(DenseMatrix A) {
        return diag(A.sumOverRows().t()).sub(A);
    }

    // H = element-wise log(H0)
    DenseMatrix H0toH(DenseMatrix H0) {
        return log(H0);
    }

    // H = (L + J)^{-1}
    public DenseMatrix getHResistance(DenseMatrix L) {
        int d = L.cols;
        double j = 1.0 / d;
        DenseMatrix J = ones(d, d).mul(j);
        return pinv(L.add(J));
    }

    // H0 = (I - tA)^{-1}
    DenseMatrix getH0Walk(DenseMatrix A, double t) {
        int d = A.cols;
        DenseMatrix I = eye(d);
        DenseMatrix ins = I.sub(A.mul(t));
        return pinv(ins);
    }

    // H0 = (I + tL)^{-1}
    public DenseMatrix getH0Forest(DenseMatrix L, double t) {
        int d = L.cols;
        DenseMatrix I = eye(d);
        return pinv(I.add(L.mul(t)));
    }

    // H0 = exp(tA)
    DenseMatrix getH0DummyCommunicability(DenseMatrix A, double t) {
        return dummy_mexp(A.mul(t), 30);
    }

    DenseMatrix getH0Communicability(DenseMatrix A, double t) {
        return A.mul(t).mexp();
    }

    // D = (h*1^{T} + 1*h^{T} - H - H^T)/2
    public DenseMatrix getD(DenseMatrix H) {
        int d = H.cols;
        DenseMatrix h = diagToVector(H);
        DenseMatrix i = DenseMatrix.ones(d, 1);
        return h.mmul(i.t()).add(i.mmul(h.t())).sub(H).sub(H.t()).div(2);
    }

    // Johnson's Algorithm
    public DenseMatrix getDShortestPath(DenseMatrix A) {
        return JohnsonsAlgorithm.getAllShortestPaths(A);
    }

    DenseMatrix getDFreeEnergy(DenseMatrix A, double beta) {
        int d = A.cols;

        // P^{ref} = D^{-1}*A, D = Diag(A*e)
        DenseMatrix e = ones(d, 1);
        DenseMatrix D = diag(A.mmul(e));
        DenseMatrix Pref = pinv(D).mmul(A);

        // W = P^{ref} (element-wise)* exp(-βC)
        DenseMatrix C = JohnsonsAlgorithm.getAllShortestPaths(A);
        DenseMatrix W = Pref.mul(exp(C.mul(-beta)));

        // Z = (I - W)^{-1}
        DenseMatrix I = eye(d);
        DenseMatrix Z = pinv(I.sub(W));

        // Z^h = Z * D_h^{-1}, D_h = Diag(Z)
        DenseMatrix Dh = diag(diagToVector(Z));
        DenseMatrix Zh = Z.mmul(pinv(Dh));

        // Φ = -1/β * log(Z^h)
        DenseMatrix Φ = log(Zh).div(-beta);

        // Δ_FE = (Φ + Φ^T)/2
        DenseMatrix Δ_FE = Φ.add(Φ.t()).div(2);

        return Δ_FE.sub(diag(diagToVector(Δ_FE)));
    }

    DenseMatrix getD_RSP(DenseMatrix A, double beta) {
        int d = A.cols;

        // P^{ref} = D^{-1}*A, D = Diag(A*e)
        DenseMatrix e = ones(d, 1);
        DenseMatrix D = diag(A.mmul(e));
        DenseMatrix Pref = pinv(D).mmul(A);

        // W = P^{ref} ◦ exp(-βC); ◦ is element-wise *
        DenseMatrix C = JohnsonsAlgorithm.getAllShortestPaths(A);
        DenseMatrix W = Pref.mul(exp(C.mul(-beta)));

        // Z = (I - W)^{-1}
        DenseMatrix I = eye(d);
        DenseMatrix Z = pinv(I.sub(W));

        // Z^h = Z * D_h^{-1}, D_h = Diag(Z)
        DenseMatrix Dh = diag(diagToVector(Z));
        DenseMatrix Zh = Z.mmul(pinv(Dh));

        // S = (Z(C ◦ W)Z)÷Z; ÷ is element-wise /
        DenseMatrix S = Z.mmul(C.mul(W)).mmul(Z).div(Z);
        // C_ = S - e(d_S)^T; d_S = diag(S)
        DenseMatrix C_ = S.sub(e.mmul(diagToVector(S).t()));
        // Δ_RSP = (C_ + C_^T)/2
        DenseMatrix Δ_RSP = C_.add(C_.t()).div(2);

        return Δ_RSP.sub(diag(diagToVector(Δ_RSP)));
    }

    // K_CCT = K_CT + HD^{-1}AD^{-1}H
    DenseMatrix getH_SCCT(DenseMatrix A) {
        int d = A.cols;

        // K_CT = H_resistance
        DenseMatrix L = getL(A);
        DenseMatrix K_CT = getHResistance(L);

        // H = (I - ee^T/n)
        DenseMatrix I = eye(d);
        DenseMatrix H = I.sub(ones(d, d).mul(1.0 / d));

        // D = L + A
        DenseMatrix D = diag(A.sumOverRows().t());
        DenseMatrix pinvD = pinv(D);

        return K_CT.add(H.mmul(pinvD).mmul(A).mmul(pinvD).mmul(H));
    }
}