package models;

import java.util.List;

import lombok.val;

public class Node {
    private Vertex<Double> UB ; 
    private Vertex<Double> LB ;
    private List<Double> objectiveFunction ;
    private List<Constraint> constraints ; 
    private int id ;
    private Double value ;
    
    private Node parent ; 
    private Node left ;
    private Node right ;
    private GraphicSolutionType type ;

    private double x = 0 ; 
    private double y = 0 ; 
    private int level = 0 ; 

    public Node(Vertex<Double> uB, Vertex<Double> lB, List<Double> objectiveFunction, List<Constraint> constraints, 
            int  id , Node parent, Node left, Node right) {
        UB = uB;
        LB = lB;
        this.objectiveFunction = objectiveFunction;
        this.constraints = constraints;
        this.parent = parent;
        this.left = left;
        this.right = right;
        this.id = id ; 
        if (uB != null)
            this.value = evaluateObjectiveFunction(uB) ; 
    }
    
    
    public Node(Vertex<Double> uB, Vertex<Double> lB, List<Double> objectiveFunction, List<Constraint> constraints   , int id) {
        UB = uB;
        LB = lB;
        this.objectiveFunction = objectiveFunction;
        this.constraints = constraints;
        this.id = id ;
        if (uB != null)
            this.value = evaluateObjectiveFunction(uB) ; 
    } 


    public Double evaluateObjectiveFunction(Vertex<Double> solution) {

        return objectiveFunction.get(0) * solution.getX() + objectiveFunction.get(1) * solution.getY() ; 


    }




    public Vertex<Double> getUB() {
        return UB;
    }
    public void setUB(Vertex<Double> uB) {
        UB = uB;
    }
    public Vertex<Double> getLB() {
        return LB;
    }
    public void setLB(Vertex<Double> lB) {
        LB = lB;
    }
    public List<Double> getObjectiveFunction() {
        return objectiveFunction;
    }
    public void setObjectiveFunction(List<Double> objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
    }
    public List<Constraint> getConstraints() {
        return constraints;
    }
    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }
    public Node getParent() {
        return parent;
    }
    public void setParent(Node parent) {
        this.parent = parent;
    }
    public Node getLeft() {
        return left;
    }
    public void setLeft(Node left) {
        this.left = left;
    }
    public Node getRight() {
        return right;
    }
    public void setRight(Node right) {
        this.right = right;
    }



    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "Node [" +  "id=" + id    + " , UB=" + UB + ", LB=" + LB + ", objectiveFunction=" + value  + ",type = " + type  +  "]";
    }


    public GraphicSolutionType getType() {
        return type;
    }


    public void setType(GraphicSolutionType type) {
        this.type = type;
    }


    public Double getValue() {
        return value;
    }


    public void setValue(Double value) {
        this.value = value;
    }


    public void setPosition(double x , double y) {
        this.x = x ; 
        this.y = y ; 
    }


    public double getX() {
        return x;
    }


    public void setX(double x) {
        this.x = x;
    }


    public double getY() {
        return y;
    }


    public void setY(double y) {
        this.y = y;
    }


    public int getLevel() {
        return level;
    }


    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isFeasible() {
        return this.type.equals(GraphicSolutionType.FEASIBLE) ;
    }

    public boolean isSolutionInteger() {
        if (UB != null && LB != null) {
            return DoubleUtils.getFractionalPart(UB.getX()) == 0.0 &&
           DoubleUtils.getFractionalPart(UB.getY()) == 0.0   ; 
            
        } 
        return false ; 
    }
}
