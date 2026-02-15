package logic;

import java.util.ArrayList;
import java.util.List;

import models.Constraint;
import models.ConstraintType;
import models.GraphicSolutionType;
import models.LPProblem;
import models.Vertex;

public class SimplexGraphicSolver {

    private List<Equation> equations ;
    private List<Double> objectiveFunction ;
    private List<Constraint> constraints ; 
    
    private Double objectiveValue ;
    private GraphicSolutionType type ;


    public SimplexGraphicSolver(List<Constraint> constraints ,List<Double> objectiveFunction ) {
        
        
        this.constraints = constraints ; 
        this.objectiveFunction = objectiveFunction ; 
        this.equations = processConstraints(constraints);

        objectiveValue = null ; 
        type = null ; 

        // System.out.println(equations);
    }

    private List<Equation> processConstraints(List<Constraint> constraints) {
        List<Equation> result = new ArrayList<>() ; 

        for (Constraint constraint : constraints) {
            List <Double> array = new ArrayList<>() ; 
            for (int i = 0; i < constraint.getCoefficients().size(); i++) {
                array.add(constraint.getCoefficients().get(i)) ; 
                
            }
            Equation equation = new Equation(array , constraint.getValue() ) ; 
            // constraint type is assumed to be <=
            result.add(equation) ; 
        }

        return result ;
    }

    public Vertex<Double> solve2Equations(Equation equation1 , Equation equation2) {
        //  if (this.equations.size() < 2)  {
        //     System.out.println("error cant solve for more than 2 variables ");
        //     return null; 
        // }
        double c = -equation1.value ; 
        double t = -equation2.value ; 
        double a = equation1.coefficients.get(0);
        double b = equation1.coefficients.get(1);
        double r = equation2.coefficients.get(0);
        double s = equation2.coefficients.get(1);
        
        Double x1 = (c*s - b * t) / (b * r - a * s) ;
        Double x2 = (a*t - c*r) / (b * r - a * s) ;
        List <Double> result = new ArrayList<>() ; 
        result.add(x1);
        result.add(x2);

        Vertex<Double> v = new Vertex<Double>(x1, x2) ; 
        return  v ; 


    }

    public Boolean verifySolution(Vertex<Double> solution) {
        // System.out.println("DEBUG : verifying if the solution is feasible or not ");
        
        int count = 1 ; 
        for (Constraint constraint : constraints) {
            
            Double result =  constraint.getCoefficients().get(0) * solution.getX() + constraint.getCoefficients().get(1) * solution.getY() ; 
            // System.out.println("DEBUG : constraint " + count + " type :  " + constraint.getType() + " result = " + result);

            if (constraint.getType().equals(ConstraintType.EQUAL)  ) {
                if (result != constraint.getValue()) {
                    // System.out.println("failed with ==");
                    return false ; 
                }
            } else if (constraint.getType().equals(ConstraintType.GREATER_THAN_OR_EQUAL)) {
                if (result < constraint.getValue()) {
                    // System.out.println("failed with >=");
                    return false ; 
                }
            } else if (constraint.getType().equals(ConstraintType.LESS_THAN_OR_EQUAL) ) {
                if (result > constraint.getValue()) {
                    // System.out.println("failed with <=");
                    return false ; 
                }
            }
            count++ ; 
        }

        return true ;
    }

    public Double evaluateObjectiveFunction(Vertex<Double> solution) {

        return objectiveFunction.get(0) * solution.getX() + objectiveFunction.get(1) * solution.getY() ; 


    }

    public Vertex<Double> solve() {

        List<Vertex<Double>> vertices = new ArrayList<>() ;


        for (int i = 0; i < equations.size(); i++) {
            for (int j = i+1; j < equations.size(); j++) {
            
                Vertex<Double> solution = solve2Equations(equations.get(i) , equations.get(j)) ; 
                if (solution == null) {
                    continue ; 
                }
                // debug 
                // System.out.println("DEBUG : pair = (" + (i+1) + "," + (j+1) + ")");
                if (solution != null) {
                    if (verifySolution(solution)) {
                        // System.out.println("DEBUG: yes it satisfies with = " + (solution));
                        vertices.add(solution) ; 
                    } else {
                        // System.out.println("DEBUG: no it doesnt satisfy with = " + (solution));

                    }
                }
            }
        } 
        Vertex<Double> vertex_solution = null ;
        Double maxEvaluatedValue = Double.NEGATIVE_INFINITY ; 
        type = GraphicSolutionType.UNFEASIBLE ; 


        for (Vertex<Double> vertex : vertices) {
            Double val = evaluateObjectiveFunction(vertex) ;  
            // System.out.println("DEBUG : vertex treated : " + vertex + " value = " + val);

            if (val > maxEvaluatedValue) {
                maxEvaluatedValue = val ;
                vertex_solution  = vertex.clone() ;  
                type = GraphicSolutionType.FEASIBLE ; 
            }
        }
        // System.out.println("DEBUG : solution vertex found : " + vertex_solution);      
          
        return vertex_solution ;
    }


    public void generatePairs() {

    }


    private record Equation(List<Double> coefficients, double value) {
    }


    public GraphicSolutionType getType() {
        return type;
    }

    public void setType(GraphicSolutionType type) {
        this.type = type;
    }

}
