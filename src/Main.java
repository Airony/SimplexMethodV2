import logic.SimplexTableauSolver;
import org.ejml.simple.SimpleMatrix;

public class Main {
    public static void main(String[] args) {
        var mat = new SimpleMatrix(4, 6);
        // Input the following values into the matrix please
        // 2 1 1 0 1 2000
        // 4 8 0 1 1 8000
        // 10 10 0 0 1 12500
        // 600 400 0 0 0 0
        mat.setRow(0, 0, 2, 1, 1, 0, 1, 2000);
        mat.setRow(1, 0, 4, 8, 0,
                1, 1, 8000);
        mat.setRow(2, 0, 10, 10, 0, 0, 1, 12500);
        mat.setRow(3, 0, -600, -400, 0, 0, 0, 0);
        var baseVars = java.util.Arrays.asList(2, 3, 4);
        var solver = new SimplexTableauSolver(mat, baseVars);
        var result = solver.solve();
        result.tableau().print();
    }
}