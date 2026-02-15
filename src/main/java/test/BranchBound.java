package test;
import models.LPProblem;
import models.LinearProgramSolverOutput;
import models.Node;
import models.Vertex;
import view.BranchBoundTreeVisualizer;
import models.LPProblem.ObjectiveType;
import models.Constraint;
import models.ConstraintType;
import logic.BranchBoundSolverGraphic;
import logic.LinearProgramSolver;
import logic.SimplexGraphicSolver;
import logic.SimplexStepListener;
import logic.Tableau;
import org.apache.commons.math3.fraction.Fraction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.ArrayList;


public class BranchBound {
    public static void main(String[] args) {
        System.out.println("=== Testing Simplex Solver ===");
        // System.out.println("Warning : constraint type is assumed to be <= ");
        // System.out.println("\n--- Test 1: Maximize 100*x1 + 150*x2  ---");
        // System.out.println("Constraints: 8000*x1 + 4000*x2 <= 40 000\n 15*x1 + 30*x2 <= 200 \n x2 <= 5");
        
        double[] objectiveCoefficients = {5.0, 4.0};
        // double[] objectiveCoefficients = {100.0, 150.0};
        
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint(
            12.0,
            ConstraintType.LESS_THAN_OR_EQUAL,
            Arrays.asList(2.0, 3.0 )
        ));
        constraints.add(new Constraint(
            9,
            ConstraintType.LESS_THAN_OR_EQUAL,
            Arrays.asList(3.0, 1.0 )
        ));
        constraints.add(new Constraint(
            19,
            ConstraintType.LESS_THAN_OR_EQUAL,
            Arrays.asList(2.0, 5.0 )
        ));



        // constraints.add(new Constraint(
        //     6.0,
        //     ConstraintType.GREATER_THAN_OR_EQUAL,
        //     Arrays.asList(0.0, 1.0 )
        // ));
        


        // x1 >= 0 , x2 >= 0
        constraints.add(new Constraint(
            0.0,
            ConstraintType.GREATER_THAN_OR_EQUAL,
            Arrays.asList(1.0, 0.0 )
        ));
        
        constraints.add(new Constraint(
            0.0,
            ConstraintType.GREATER_THAN_OR_EQUAL,
            Arrays.asList(0.0, 1.0 )
        ));

        // constraints.add(new Constraint(
        //     2.0,
        //     ConstraintType.GREATER_THAN_OR_EQUAL,
        //     Arrays.asList(1.0, 0.0 )
        // ));
        

        for (Constraint constraint : constraints) {
            System.out.println(constraint);
        }
        
        ObjectiveType objectiveType = ObjectiveType.MAXIMIZE;
        
        // LPProblem problem = new LPProblem(objectiveCoefficients, constraints, objectiveType);
        
        SimplexGraphicSolver solver = new SimplexGraphicSolver(constraints ,
            Arrays.stream(objectiveCoefficients)
            .boxed()
            .collect(Collectors.toList()));


        // Vertex<Double> result = solver.solve();

        // System.out.println("result : " + (result!=null ?    result.toString() + " Z = " + solver.evaluateObjectiveFunction(result)  : "no solution" ));



            BranchBoundSolverGraphic solverBB = new BranchBoundSolverGraphic(constraints , 
                Arrays.stream(objectiveCoefficients)
                .boxed()
                .collect(Collectors.toList())
            ) ; 


            Vertex<Double> solution =  solver.solve();
            System.out.println((solution == null ? "null" : solution  ));
            // solver.printResult();

            solverBB.generateTree();
            System.out.println("\n\n\n\n");
            solverBB.traverseTree();
            Node rootNode = solverBB.root ; 
            BranchBoundTreeVisualizer.showTree(rootNode);

            // System.out.println(solverBB.root.getLeft() == null);

            
            // System.out.println(solverBB.root);



    }
    
    private static void testSimpleMaximization() {
        // System.out.println("a = " + a);
        // System.out.println("b = " + b);
    
    }
}

class ConsoleListener implements SimplexStepListener {
    @Override
    public void onPhase1Step(Tableau tableau,
                           String entering, String leaving, int iteration,
                           java.util.List<String> basisVariableNames, java.util.List<String> variableNames) {
        System.out.println("Phase 1 - Iteration " + iteration + ": Entering=" + entering + ", Leaving=" + leaving);
    }
    
    @Override
    public void onPhase1Complete(boolean feasible) {
        System.out.println("Phase 1 complete. Feasible: " + feasible);
    }
    
    @Override
    public void onPhase2Step(Tableau tableau,
                           String entering, String leaving, int iteration,
                           java.util.List<String> basisVariableNames, java.util.List<String> variableNames) {
        System.out.println("Phase 2 - Iteration " + iteration + ": Entering=" + entering + ", Leaving=" + leaving);
    }
    
    @Override
    public void onPhase2Complete(Fraction optimalValue, Fraction[] solution, 
                               models.ConstraintType.SolutionStatus status) {
        System.out.println("\n=== FINAL RESULT ===");
        System.out.println("Status: " + status);
        if (optimalValue != null) {
            System.out.println("Optimal Value: " + optimalValue);
        }
        if (solution != null) {
            System.out.println("Solution:");
            for (int i = 0; i < solution.length; i++) {
                System.out.println("  x" + (i+1) + " = " + solution[i]);
            }
        }
        System.out.println("===================\n");
    }
    
    @Override
    public void onError(String message) {
        System.out.println("Error: " + message);
    }
}
