package logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

import org.apache.commons.math3.fraction.Fraction;

import models.Constraint;
import models.ConstraintType;
import models.DoubleUtils;
import models.GraphicSolutionType;
import models.LPProblem;
import models.LinearProgramSolverOutput;
import models.Node;
import models.SimplexIterationResult;
import models.Vertex;

public class BranchBoundSolverGraphic {
    public Node root ; 
    // private SimplexGraphicSolver solver ; 
    private List<Constraint> constraints ; 

    private Queue<Node> activeList ;
    private Double optimalValue ;
    private List<Double> objectiveFunction ;
    public int count ;


    public BranchBoundSolverGraphic(   List<Constraint> constraints , List<Double> objectiveFunction ) {

        this.constraints = constraints ; 
        SimplexGraphicSolver solver = new SimplexGraphicSolver(constraints, objectiveFunction) ; 
        
        Vertex<Double> UB = solver.solve() ;  
        Vertex<Double> LB ; 
        GraphicSolutionType temp_type = GraphicSolutionType.UNFEASIBLE ; 

        if (UB == null) {
            
            LB =  null ;  
            
        } else {
            temp_type =   GraphicSolutionType.FEASIBLE ;
            LB =  new Vertex<Double>( getIntegerPart(UB.getX()) , getIntegerPart(UB.getY()) ) ;  
            
        }

        Node left = null ; 
        Node right = null ; 
        
        Node parent = null ; 

        this.root = new Node(UB, LB, objectiveFunction, constraints  , 0, parent , left , right) ;       
        this.root.setType(temp_type);
        


        this.objectiveFunction = objectiveFunction ; 
        
        activeList = new LinkedList<>() ;
        activeList.add(root) ; 
        optimalValue = Double.NEGATIVE_INFINITY ; 
        count = 1 ; 
    }

    public Boolean isSolutionInteger(Vertex<Double> solution) {
        return DoubleUtils.getFractionalPart(solution.getX()) == 0.0 &&
           DoubleUtils.getFractionalPart(solution.getY()) == 0.0 ; 
    }    

    public void generateTree() {
        optimalValue = Double.NEGATIVE_INFINITY ; 

        activeList.removeAll(activeList) ; 
        activeList.add(root) ; 

        System.out.println("DEBUG : before entering loop there are " + activeList.size() + " element !");
        int j = 0 ; 
        while (!activeList.isEmpty() ) {
            // System.out.println("DEBUG : iteration number " + j  + "queue size : " + activeList.size());
            
            Node current = selectNode(new ArrayList<>(activeList) ) ;
            System.out.println("DEBUG : selected current node  " + current );
            
            solveNode(current);
            // solveSetChildrens(current);
            System.out.println("DEBUG : selected current node after solving  " + current );
            // System.out.println("DEBUG : after solving current node  " + current + " type " + current.get );
            

            if (current.getType().equals(GraphicSolutionType.UNFEASIBLE)) {
                System.out.println("DESBUG : GraphicSolutionType.UNFEASIBLE condition");
                // activeList.remove(current) ; 

                        boolean result_removing = activeList.remove(current) ; 
                        System.out.println("DEBUG : removing node with result + " + result_removing);


            } else if (current.getType().equals(GraphicSolutionType.FEASIBLE) 
                && current.getValue() < optimalValue ) {
                    System.out.println("DESBUG : GraphicSolutionType.FEASIBLE condition");
                    // activeList.remove(current) ; 

                        boolean result_removing = activeList.remove(current) ; 
                        System.out.println("DEBUG : removing node with result + " + result_removing);
                    
            } else if (current.getType().equals(GraphicSolutionType.FEASIBLE) && 
                                        current.getValue() > optimalValue     ) {
                if (isSolutionInteger(current.getUB())) {
                        System.out.println("DESBUG : GraphicSolutionType.FEASIBLE and integer condition");
                        optimalValue = current.getValue() ; 
                        // activeList.remove(current) ; 

                        boolean result_removing = activeList.remove(current) ; 
                        System.out.println("DEBUG : removing node with result + " + result_removing);
                } else {
                        System.out.println("DESBUG : GraphicSolutionType.FEASIBLE and fraction condition");
                        int index = chooseVariable(current.getUB().getX(), current.getUB().getY()) ; 
                        
                        boolean result_removing = activeList.remove(current) ; 
                        System.out.println("DEBUG : removing node with result + " + result_removing);
                        solveSetChildrens(current);

                        if (current.getLeft() != null) {
                            activeList.add(current.getLeft() ) ; 
                            
                        }

                        if (current.getRight() != null) {
                            activeList.add(current.getRight() ) ; 
                            
                        }
                        // activeList.add( current.getRight() ) ; 
                    }
            }


            j++ ; 
        }
    }


    public Double evaluateObjectiveFunction(Vertex<Double> solution) {
        if (solution == null) 
            return Double.NEGATIVE_INFINITY ; 
        return objectiveFunction.get(0) * solution.getX() + objectiveFunction.get(1) * solution.getY() ; 


    }

    public Node selectNode(ArrayList<Node> nodes) {

        Node result = nodes.get(0) ; 
        Double max_val = evaluateObjectiveFunction(result.getUB()) ; 
        for (Node node : nodes) {
            if (evaluateObjectiveFunction(node.getUB()) > max_val) {
                result = node ; 
                max_val = evaluateObjectiveFunction(node.getUB()) ; 
            }
        }

        return result ; 
    }

/*     public void solve() {
        if (root == null) {
            return ; 
        }

        int variableToBranchOn = chooseVariable(root.getUB().getX(), root.getUB().getY()) ; 
        
        int leftConstraintValue = 0 ; 
        int rightConstraintValue = 0 ; 
        
        Double variable1 ;         
        
        if (variableToBranchOn == 0) {
            variable1 =  root.getUB().getX() ;             
        }  else {
            variable1 = root.getUB().getY() ; 
        }

        leftConstraintValue = DoubleUtils.getIntegerPart(variable1)  ; 
        rightConstraintValue = DoubleUtils.getIntegerPart(variable1) + 1  ; 

            
        List<Constraint> leftConstraints = new ArrayList<>() ;
        List<Constraint> rightConstraints = new ArrayList<>() ;
        
        
        for (Constraint constraint : this.constraints) {
            leftConstraints.add(constraint.makeCopy()) ;
            rightConstraints.add(constraint.makeCopy()) ;

        } 
        List<Double> newCoeff = new ArrayList<>() ; 

        if (variableToBranchOn == 0) {
            newCoeff = (Arrays.asList(1.0 , 0.0)) ; 
        } else {
            newCoeff = (Arrays.asList(0.0 , 1.0)) ; 
            
        }
        

            leftConstraints.add( new Constraint(
                leftConstraintValue,
                 ConstraintType.LESS_THAN_OR_EQUAL,
                  newCoeff
                )) ; 

            rightConstraints.add( new Constraint(
                rightConstraintValue,
                 ConstraintType.GREATER_THAN_OR_EQUAL,
                  newCoeff
                )) ; 



        Node left = new Node(null, null, root.getObjectiveFunction(), leftConstraints ,
         1 , root , null , null) ; 
        
        Node right = new Node(null, null, root.getObjectiveFunction(), rightConstraints ,
         2 , root , null , null) ; 
        
        solveNode(left);
        solveNode(right);
        
        root.setLeft(left);
        root.setRight(right);

        activeList.add(left) ; 
        activeList.add(right) ; 

    }

 */
    public void solve(Node current) {
        solveNode(current );
        if (current == null || current.getType().equals(GraphicSolutionType.UNFEASIBLE)) {
            return ; 
        }




        int variableToBranchOn = chooseVariable(current.getUB().getX(), current.getUB().getY()) ; 
        
        int leftConstraintValue = 0 ; 
        int rightConstraintValue = 0 ; 
        
        Double variable1  ;         
        
        if (variableToBranchOn == 0) {

            variable1 =  current.getUB().getX() ;             
        }  else {
            variable1 = current.getUB().getY() ; 
        }

        // leftConstraintValue = DoubleUtils.getIntegerPart(variable1)  ; 
        leftConstraintValue = DoubleUtils.getFloorPart(variable1)  ; 
        rightConstraintValue = DoubleUtils.getCeilPart(variable1)   ; 
        // rightConstraintValue = DoubleUtils.getIntegerPart(variable1) + 1  ; 

            
        List<Constraint> leftConstraints = new ArrayList<>() ;
        List<Constraint> rightConstraints = new ArrayList<>() ;
        
        // BUG HEREEEEEEEEEEEEEEEEEEEEEEEEEE
        // for (Constraint constraint : this.constraints) {
        //     leftConstraints.add(constraint.makeCopy()) ;
        //     rightConstraints.add(constraint.makeCopy()) ;

        // } 
        
        for (Constraint constraint : current.getConstraints()) {
            leftConstraints.add(constraint.makeCopy()) ;
            rightConstraints.add(constraint.makeCopy()) ;

        } 
        List<Double> newCoeff = new ArrayList<>() ; 

        if (variableToBranchOn == 0) {
            newCoeff = (Arrays.asList(1.0 , 0.0)) ; 
        } else {
            newCoeff = (Arrays.asList(0.0 , 1.0)) ; 
            
        }
        

            leftConstraints.add( new Constraint(
                leftConstraintValue,
                 ConstraintType.LESS_THAN_OR_EQUAL,
                  newCoeff
                )) ; 

            rightConstraints.add( new Constraint(
                rightConstraintValue,
                 ConstraintType.GREATER_THAN_OR_EQUAL,
                  newCoeff
                )) ; 

             ; 

        Node left = new Node(null, null, current.getObjectiveFunction(), leftConstraints ,
         count , current , null , null) ; 
        
        Node right = new Node(null, null, current.getObjectiveFunction(), rightConstraints ,
         count + 1 , current , null , null) ; 
            count += 2 ;        

        // solveNode(left);
        // solveNode(right);
        
        // current.setLeft(left);
        // current.setLeft(right);
        current.setLeft(null);
        current.setRight(null);

        // activeList.add(left) ; 
        // activeList.add(right) ; 

    }
    public void solveSetChildrens(Node current) {
        solveNode(current );
        if (current == null || current.getType().equals(GraphicSolutionType.UNFEASIBLE)) {
            return ; 
        }




        int variableToBranchOn = chooseVariable(current.getUB().getX(), current.getUB().getY()) ; 
        
        int leftConstraintValue = 0 ; 
        int rightConstraintValue = 0 ; 
        
        Double variable1  ;         
        
        if (variableToBranchOn == 0) {

            variable1 =  current.getUB().getX() ;             
        }  else {
            variable1 = current.getUB().getY() ; 
        }

        // leftConstraintValue = DoubleUtils.getIntegerPart(variable1)  ; 
        leftConstraintValue = DoubleUtils.getFloorPart(variable1)  ; 
        rightConstraintValue = DoubleUtils.getCeilPart(variable1)   ; 
        // rightConstraintValue = DoubleUtils.getIntegerPart(variable1) + 1  ; 

            
        List<Constraint> leftConstraints = new ArrayList<>() ;
        List<Constraint> rightConstraints = new ArrayList<>() ;
        
        // BUG HEREEEEEEEEEEEEEEEEEEEEEEEEEE
        // for (Constraint constraint : this.constraints) {
        //     leftConstraints.add(constraint.makeCopy()) ;
        //     rightConstraints.add(constraint.makeCopy()) ;

        // } 
        
        for (Constraint constraint : current.getConstraints()) {
            leftConstraints.add(constraint.makeCopy()) ;
            rightConstraints.add(constraint.makeCopy()) ;

        } 
        List<Double> newCoeff = new ArrayList<>() ; 

        if (variableToBranchOn == 0) {
            newCoeff = (Arrays.asList(1.0 , 0.0)) ; 
        } else {
            newCoeff = (Arrays.asList(0.0 , 1.0)) ; 
            
        }
        

            leftConstraints.add( new Constraint(
                leftConstraintValue,
                 ConstraintType.LESS_THAN_OR_EQUAL,
                  newCoeff
                )) ; 

            rightConstraints.add( new Constraint(
                rightConstraintValue,
                 ConstraintType.GREATER_THAN_OR_EQUAL,
                  newCoeff
                )) ; 

             ; 

        Node left = new Node(null, null, current.getObjectiveFunction(), leftConstraints ,
         count , current , null , null) ; 
        
        Node right = new Node(null, null, current.getObjectiveFunction(), rightConstraints ,
         count + 1 , current , null , null) ; 
            count += 2 ;        

        solveNode(left);
        solveNode(right);
        
        current.setLeft(left);
        current.setRight(right);
        // current.setLeft(null);
        // current.setRight(null);

        // activeList.add(left) ; 
        // activeList.add(right) ; 

    }


    public void traverseTree() {
        if (root == null) {
            System.out.println("ERROR : your tree is empty");
            return ; 
        }

        Queue<Node> queue = new LinkedList<>() ; 

        queue.add(root) ; 
        int i = 0 ; 
        while (!queue.isEmpty()) {
            Node current = queue.poll() ;
            System.out.println("Level : " + i + " " + current);
            
            if (current.getLeft() != null) {
                queue.add(current.getLeft()) ; 
            }
            
            if (current.getRight() != null) {
                queue.add(current.getRight()) ; 
            }
            i++ ; 
        }

    }



    public int chooseVariable(Double x1 ,Double x2 ) {
        Double fraction1 =  DoubleUtils.getFractionalPart(x1) ; 
        Double fraction2 =  DoubleUtils.getFractionalPart(x2) ; 
        

        return fraction1 > fraction2 ? 0 : 1 ; 
    }

    public void solveNode(Node node) {
        SimplexGraphicSolver solver = new SimplexGraphicSolver(node.getConstraints(),node.getObjectiveFunction()) ; 
        
        Vertex<Double> solution = solver.solve() ; 
        node.setUB(solution);
        
        if (solution == null) {
            node.setType( GraphicSolutionType.UNFEASIBLE );
            node.setLB( null );
            node.setValue(Double.NEGATIVE_INFINITY);
            
        } else {
            node.setType( GraphicSolutionType.FEASIBLE );
            Vertex<Double> integerSolution = new Vertex<Double>(getIntegerPart(solution.getX())   , getIntegerPart(solution.getY())) ; 
            node.setLB( integerSolution );
            node.setValue(solver.evaluateObjectiveFunction(solution));

        }

        


    }


    public void printResult() {
        System.out.println(root);
        System.out.println("Left -> "  + root.getLeft());
        System.out.println("right -> " +  root.getRight());
    }


    public Double getIntegerPart(Double x) {
        return Double.valueOf( DoubleUtils.getFloorPart(x)) ; 
    }

}
