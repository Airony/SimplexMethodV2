# Simplex Solver

A JavaFX desktop application for solving linear programming problems interactively. It allows you to input an objective function and constraints, then view the solution along with the intermediate simplex tableaux.

## What It Does

- Solves linear programs with `<=`, `>=`, and `=` constraints.
- Supports both maximization and minimization.
- Shows step-by-step tableaux with highlighted pivot elements, entering/leaving variables, and basis tracking.
- Identifies whether the problem has an optimal solution, is infeasible, or is unbounded.

## How It Works

The solver uses the **Two-Phase Simplex Method**:

1. **Standard Form**: Converts inequalities into equations by adding slack variables (`<=`), surplus and artificial variables (`>=`), or artificial variables (`=`).
2. **Phase 1**: Minimizes the sum of artificial variables to locate an initial basic feasible solution. If the artificial variables cannot be driven to zero, the problem is declared infeasible.
3. **Phase 2**: Removes the artificial variables and solves for the original objective function through standard simplex pivot steps until optimality or unboundedness is reached.
4. **Exact Arithmetic**: Uses rational fractions (`org.apache.commons.math3.fraction.Fraction`) for all matrix operations to avoid floating-point precision issues.

## Requirements

- JDK 24 (or compatible Java 21+)
- Maven

## How to Run

### Using Maven
Run the application with the JavaFX plugin:

```bash
mvn javafx:run
```

To run the test suite:

```bash
mvn test
```

### Using an IDE (e.g. IntelliJ IDEA)
1. Open the repository root as a Maven project.
2. Run `Main.java` (`src/main/java/Main.java`) or `SimplexSolverUI.java` (`src/main/java/view/SimplexSolverUI.java`).
