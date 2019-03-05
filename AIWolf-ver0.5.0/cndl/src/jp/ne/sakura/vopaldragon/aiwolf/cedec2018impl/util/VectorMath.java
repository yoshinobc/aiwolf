package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util;

import java.util.Arrays;

public class VectorMath {

    public static double euclid(double[] vector1, double[] vector2) {
        double sum = 0;
        for (int i = 0; i < vector1.length; i++) {
            sum += Math.pow(vector1[i] - vector2[i], 2);
        }
        return sum;
    }

    public static double pearson(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) return 0;
        double sum1 = sum(vector1);
        double sum2 = sum(vector2);

        double sum1Sq = sqSum(vector1);
        double sum2Sq = sqSum(vector2);

        double psum = 0;
        for (int i = 0; i < vector1.length; i++) {
            psum += (vector1[i] * vector2[i]);
        }

        double length = (double) vector1.length;

        double num = psum - sum1 * sum2 / length;
        double den = Math.sqrt((sum1Sq - sum1 * sum1 / length) * (sum2Sq - sum2 * sum2 / length));
        if (den == 0) return 0;

        return 1 - Math.abs(num / den);

    }

    public static double sum(double[] vector) {
        double sum = 0;
        for (double d : vector) {
            sum += d;
        }
        return sum;
    }

    public static double sqSum(double[] vector) {
        double sum = 0;
        for (double d : vector) {
            sum += d * d;
        }
        return sum;
    }

    /**
     * 破壊的メソッド
     */
    public static double[] normalize(double[] vector) {
        double d = 0;
        for (double dd : vector) {
            d += dd * dd;
        }
        if (d != 0) {
            d = Math.sqrt(d);
            for (int i = 0; i < vector.length; i++) {
                vector[i] = vector[i] / d;
            }
        }
        return vector;
    }

    /**
     * 破壊的メソッド
     */
    public static double[] normalizeL1(double[] vector) {
        double d = 0;
        for (double dd : vector) {
            d += dd;
        }
        d = d / vector.length;
        if (d != 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = vector[i] / d;
            }
        }
        return vector;
    }

    public static double ave(double[] vector) {
        double value = 0;
        for (int i = 0; i < vector.length; i++) {
            value += vector[i];
        }
        return value / vector.length;
    }

    public static double stddev(double[] values) {
        if (values.length == 1) return 0;
        double mean = ave(values);
        double temp = 0;
        for (double a : values)
            temp += (a - mean) * (a - mean);
        return Math.sqrt(temp / (values.length - 1));
    }

    public static double[] average(double[]... vector) {
        double[] result = addAll(vector);
        for (int i = 0; i < vector[0].length; i++) {
            result[i] = result[i] / vector.length;
        }
        return result;
    }

    public static double[] productAll(double geta, double[]... vector) {
        double[] start = new double[vector[0].length];
        Arrays.fill(start, 1);
        for (int i = 0; i < vector.length; i++) {
            add(vector[i], geta);
            start = product(start, vector[i]);
        }
        return start;
    }

    public static double[] addAll(double[]... vector) {
        double[] result = new double[vector[0].length];
        Arrays.fill(result, 0);
        for (double[] v2 : vector) {
            for (int i = 0; i < vector[0].length; i++) {
                result[i] += v2[i];
            }
        }
        return result;
    }

    public static void add(double[] vector, double[] toAdd) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] += toAdd[i];
        }
    }

    public static void add(double[] vector, double toAdd) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] += toAdd;
        }
    }

    public static void divide(double[] vector, double d) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / d;
        }
    }

    public static void multiply(double[] vector, double m) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] * m;
        }
    }

    public static double[] product(double[] vector1, double[] vector2) {
        double[] result = new double[vector1.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = vector1[i] * vector2[i];
        }
        return result;
    }

    public static double max(double[] vector) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] > max) {
                max = vector[i];
            }
        }
        return max;
    }

    public static void invert(double[] vector) {
        double max = max(vector);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = max - vector[i];
        }
    }

}
