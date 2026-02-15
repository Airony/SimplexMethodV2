import models.LPProblem;
import models.LPProblem.ObjectiveType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.Constraint;
import models.ConstraintType;


public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");


        double[] arr = {1 , 2 , 3 } ;
        Constraint constraint1 = new Constraint(
            2.0,
            ConstraintType.EQUAL,
            Arrays.asList(3.0, -1.0)
        );
        Constraint constraint2 = new Constraint(
            7.0,
            ConstraintType.EQUAL,
            Arrays.asList(2.0, 1.0)
        );

        List<Constraint> constraints = new ArrayList<Constraint>()  ;
        constraints.add(constraint1) ;  
        constraints.add(constraint2) ;  

        ObjectiveType object_type = ObjectiveType.MAXIMIZE ;

        LPProblem lpp = new LPProblem( arr  , constraints , object_type  ) ; 

        System.out.println( lpp.toString()); 
    }
}
