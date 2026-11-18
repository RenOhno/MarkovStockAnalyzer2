package com.example.markovstockanalyzer2.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact text encoding for 3x3 matrices, used for DB storage.
 * Rows are separated by ';', values by ','. A null value is written as
 * "null" (an unestimated probability row). Example: "142,88,128;95,77,78;121,86,115".
 */
public final class MatrixCodec {

    private MatrixCodec() {
    }

    public static String encodeCounts(List<List<Integer>> matrix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            List<Integer> row = matrix.get(i);
            for (int j = 0; j < row.size(); j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(row.get(j));
            }
        }
        return sb.toString();
    }

    public static List<List<Integer>> decodeCounts(String encoded) {
        List<List<Integer>> matrix = new ArrayList<>();
        for (String rowText : encoded.split(";")) {
            List<Integer> row = new ArrayList<>();
            for (String value : rowText.split(",")) {
                row.add(Integer.parseInt(value));
            }
            matrix.add(row);
        }
        return matrix;
    }

    public static String encodeProbabilities(List<List<Double>> matrix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            List<Double> row = matrix.get(i);
            for (int j = 0; j < row.size(); j++) {
                if (j > 0) {
                    sb.append(',');
                }
                Double value = row.get(j);
                sb.append(value == null ? "null" : Double.toString(value));
            }
        }
        return sb.toString();
    }

    public static List<List<Double>> decodeProbabilities(String encoded) {
        List<List<Double>> matrix = new ArrayList<>();
        for (String rowText : encoded.split(";")) {
            List<Double> row = new ArrayList<>();
            for (String value : rowText.split(",")) {
                row.add("null".equals(value) ? null : Double.parseDouble(value));
            }
            matrix.add(row);
        }
        return matrix;
    }
}
