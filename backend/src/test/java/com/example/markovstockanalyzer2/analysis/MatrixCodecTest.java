package com.example.markovstockanalyzer2.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class MatrixCodecTest {

    @Test
    void countsRoundTrip() {
        List<List<Integer>> matrix =
                List.of(List.of(142, 88, 128), List.of(95, 77, 78), List.of(121, 86, 115));
        String encoded = MatrixCodec.encodeCounts(matrix);
        assertEquals("142,88,128;95,77,78;121,86,115", encoded);
        assertEquals(matrix, MatrixCodec.decodeCounts(encoded));
    }

    @Test
    void doubleListRoundTripWithNull() {
        List<Double> values = java.util.Arrays.asList(0.5, null, 0.25);
        String encoded = MatrixCodec.encodeDoubleList(values);
        assertEquals("0.5,null,0.25", encoded);
        assertEquals(values, MatrixCodec.decodeDoubleList(encoded));
    }

    @Test
    void probabilitiesRoundTripWithNullRow() {
        List<List<Double>> matrix =
                List.of(
                        List.of(0.6, 0.2, 0.2),
                        java.util.Arrays.<Double>asList(null, null, null),
                        List.of(0.25, 0.25, 0.5));
        String encoded = MatrixCodec.encodeProbabilities(matrix);
        assertEquals("0.6,0.2,0.2;null,null,null;0.25,0.25,0.5", encoded);
        assertEquals(matrix, MatrixCodec.decodeProbabilities(encoded));
    }
}
