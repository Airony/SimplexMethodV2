package view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableauRendererTest {

    @Test
    void testCreateStandardHeadersWithRHS() {
        String[] headers = TableauRenderer.createStandardHeaders(2, 2, 1, true);
        assertArrayEquals(new String[]{"x1", "x2", "s1", "s2", "a1", "RHS"}, headers);
    }

    @Test
    void testCreateStandardHeadersWithoutRHS() {
        String[] headers = TableauRenderer.createStandardHeaders(3, 1, 0, false);
        assertArrayEquals(new String[]{"x1", "x2", "x3", "s1"}, headers);
    }
}
