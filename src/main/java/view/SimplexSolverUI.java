package view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import logic.LinearProgramSolver;
import logic.SimplexStepListener;
import logic.Tableau;
import models.Constraint;
import models.ConstraintType;
import models.LPProblem;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.linear.FieldMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimplexSolverUI extends Application implements SimplexStepListener {

    private final List<ConstraintRow> constraintRows = new ArrayList<>();
    private final List<TextField> objectiveCoefficients = new ArrayList<>();
    // For tracking tableau metadata
    private final String[] currentColumnHeaders = {"x1", "x2", "x3"};
    private final String[] currentBasisVariables = {"x3", "x4", "x5", "x6"};
    private int numVariables = 3;
    private ComboBox<String> objectiveType;
    private VBox constraintsContainer;
    private HBox objectiveContainer;
    private VBox solutionDisplay;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simplex Solver - Two-Phase Method");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Top: Variable controls
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Center: Input section
        VBox inputSection = createInputSection();
        ScrollPane inputScrollPane = new ScrollPane(inputSection);
        inputScrollPane.setFitToWidth(true);
        inputScrollPane.setStyle("-fx-background: white;");

        // Right: Solution display
        solutionDisplay = new VBox(10);
        solutionDisplay.setPadding(new Insets(10));
        solutionDisplay.setStyle(
                "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");

        Label solutionTitle = new Label("Solution Steps");
        solutionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        solutionDisplay.getChildren().add(solutionTitle);

        ScrollPane solutionScrollPane = new ScrollPane(solutionDisplay);
        solutionScrollPane.setFitToWidth(true);
        solutionScrollPane.setPrefWidth(400);

        // Layout
        SplitPane splitPane = new SplitPane(inputScrollPane, solutionScrollPane);
        splitPane.setDividerPositions(0.6);
        root.setCenter(splitPane);

        Scene scene = new Scene(root, 1200, 700);

        primaryStage.setOnCloseRequest(_ -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label varLabel = new Label("Number of Variables:");
        varLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Spinner<Integer> varSpinner = new Spinner<>(1, 20, numVariables);
        varSpinner.setEditable(true);
        varSpinner.setPrefWidth(80);
        varSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateVariableCount(newVal);
        });

        Button solveButton = new Button("Solve");
        solveButton.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        solveButton.setOnAction(e -> solveProblem());

        Button clearButton = new Button("Clear Solution");
        clearButton.setStyle(
                "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 8 20;");
        clearButton.setOnAction(e -> clearSolution());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(varLabel, varSpinner, spacer, solveButton, clearButton);
        return topBar;
    }

    private VBox createInputSection() {
        VBox inputSection = new VBox(20);
        inputSection.setPadding(new Insets(10));

        // Objective Function Section
        VBox objectiveSection = createObjectiveSection();

        // Constraints Section
        VBox constraintsSection = createConstraintsSection();

        inputSection.getChildren().addAll(objectiveSection, new Separator(), constraintsSection);
        return inputSection;
    }

    private VBox createObjectiveSection() {
        VBox section = new VBox(10);

        Label title = new Label("Objective Function");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox typeRow = new HBox(10);
        typeRow.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label("Type:");
        objectiveType = new ComboBox<>();
        objectiveType.getItems().addAll("MAX", "MIN");
        objectiveType.setValue("MAX");
        objectiveType.setPrefWidth(100);
        typeRow.getChildren().addAll(typeLabel, objectiveType);

        objectiveContainer = new HBox(5);
        objectiveContainer.setAlignment(Pos.CENTER_LEFT);
        updateObjectiveCoefficients();

        section.getChildren().addAll(title, typeRow, objectiveContainer);
        return section;
    }

    private VBox createConstraintsSection() {
        VBox section = new VBox(10);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Constraints");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        Button addConstraintBtn = new Button("+ Add Constraint");
        addConstraintBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        addConstraintBtn.setOnAction(e -> addConstraint());

        titleRow.getChildren().addAll(title, addConstraintBtn);

        constraintsContainer = new VBox(8);

        // Add initial constraint
        addConstraint();

        section.getChildren().addAll(titleRow, constraintsContainer);
        return section;
    }

    private void updateObjectiveCoefficients() {
        objectiveContainer.getChildren().clear();
        objectiveCoefficients.clear();

        for (int i = 0; i < numVariables; i++) {
            if (i > 0) {
                objectiveContainer.getChildren().add(new Label("+"));
            }

            TextField coeff = new TextField("0");
            coeff.setPrefWidth(60);
            objectiveCoefficients.add(coeff);

            Label varLabel = new Label("x" + (i + 1));

            objectiveContainer.getChildren().addAll(coeff, varLabel);
        }
    }

    private void addConstraint() {
        ConstraintRow row = new ConstraintRow(numVariables, constraintRows.size() + 1);
        constraintRows.add(row);

        HBox rowBox = new HBox(5);
        rowBox.setAlignment(Pos.CENTER_LEFT);

        // Coefficient inputs
        for (int i = 0; i < numVariables; i++) {
            if (i > 0) {
                rowBox.getChildren().add(new Label("+"));
            }
            rowBox.getChildren().addAll(row.coefficients.get(i), new Label("x" + (i + 1)));
        }

        // Constraint type
        rowBox.getChildren().add(row.constraintType);

        // RHS value
        rowBox.getChildren().add(row.rhsValue);

        // Remove button
        Button removeBtn = new Button("×");
        removeBtn.setStyle(
                "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
        removeBtn.setOnAction(e -> removeConstraint(row, rowBox));
        rowBox.getChildren().add(removeBtn);

        row.displayBox = rowBox;
        constraintsContainer.getChildren().add(rowBox);
    }

    private void removeConstraint(ConstraintRow row, HBox rowBox) {
        constraintRows.remove(row);
        constraintsContainer.getChildren().remove(rowBox);
    }

    private void updateVariableCount(int newCount) {
        numVariables = newCount;

        // Update objective function
        updateObjectiveCoefficients();

        // Update constraints
        for (ConstraintRow row : constraintRows) {
            row.updateVariableCount(newCount);

            // Rebuild the display
            HBox rowBox = row.displayBox;
            rowBox.getChildren().clear();

            for (int i = 0; i < numVariables; i++) {
                if (i > 0) {
                    rowBox.getChildren().add(new Label("+"));
                }
                rowBox.getChildren().addAll(row.coefficients.get(i), new Label("x" + (i + 1)));
            }

            rowBox.getChildren().addAll(row.constraintType, row.rhsValue);

            Button removeBtn = new Button("×");
            removeBtn.setStyle(
                    "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
            removeBtn.setOnAction(e -> removeConstraint(row, rowBox));
            rowBox.getChildren().add(removeBtn);
        }
    }

    private void solveProblem() {
        clearSolution();

        try {
            // Parse input data
            LPProblem problem = parseInputData();

            // Display problem summary
            displayProblemSummary(problem);

            LinearProgramSolver solver = new LinearProgramSolver(problem, this);
            solver.solve();

        } catch (Exception e) {
            onError("Error parsing input: " + e.getMessage());
        }
    }

    private LPProblem parseInputData() throws NumberFormatException {
        // Parse objective coefficients
        double[] objCoeffs = new double[numVariables];
        for (int i = 0; i < numVariables; i++) {
            String text = objectiveCoefficients.get(i).getText().trim();
            objCoeffs[i] = text.isEmpty() ? 0.0 : Double.parseDouble(text);
        }

        // Parse constraints
        List<Constraint> constraints = new ArrayList<>();

        for (ConstraintRow row : constraintRows) {
            // Parse coefficients
            List<Double> coefficients = new ArrayList<>();
            for (int j = 0; j < numVariables; j++) {
                String text = row.coefficients.get(j).getText().trim();
                double coeff = text.isEmpty() ? 0.0 : Double.parseDouble(text);
                coefficients.add(coeff);
            }

            // Parse RHS value
            String rhsText = row.rhsValue.getText().trim();
            double rhsValue = rhsText.isEmpty() ? 0.0 : Double.parseDouble(rhsText);

            // Parse constraint type
            ConstraintType constraintType = ConstraintType.fromString(row.constraintType.getValue());

            // Create and add constraint
            Constraint constraint = new Constraint(rhsValue, constraintType, coefficients);
            constraints.add(constraint);
        }
        LPProblem.ObjectiveType objType = LPProblem.ObjectiveType.fromString(objectiveType.getValue());

        return new LPProblem(objCoeffs, constraints, objType);
    }

    private void displayProblemSummary(LPProblem problem) {
        VBox summary = new VBox(5);
        summary.setPadding(new Insets(10));
        summary.setStyle(
                "-fx-background-color: #e7f3ff; -fx-border-color: #0066cc; -fx-border-width: 1;");

        Label title = new Label("Problem Summary");
        title.setFont(Font.font("System", FontWeight.BOLD, 12));

        TextArea problemText = new TextArea(problem.toString());
        problemText.setEditable(false);
        problemText.setPrefRowCount(5);
        problemText.setStyle("-fx-font-family: monospace;");

        summary.getChildren().addAll(title, problemText);
        solutionDisplay.getChildren().add(summary);
    }

    // SimplexStepListener implementation
    @Override
    public void onPhase1Step(Tableau tableau,
                             String entering,
                             String leaving,
                             int iteration,
                             List<String> basisVariableNames,
                             List<String> variableNames) {
        Platform.runLater(() -> {
            if (iteration == 1) {
                Label phase1Title = new Label("Phase 1 - Finding Initial Feasible Solution");
                phase1Title.setFont(Font.font("System", FontWeight.BOLD, 14));
                phase1Title.setStyle("-fx-text-fill: #0066cc; -fx-padding: 10 0 5 0;");
                solutionDisplay.getChildren().add(phase1Title);
            }

            VBox iterBox = createIterationDisplay(
                    "Phase 1",
                    iteration,
                    entering,
                    leaving,
                    new Tableau(tableau),
                    basisVariableNames.toArray(new String[0]),
                    variableNames.toArray(new String[0]));
            solutionDisplay.getChildren().add(iterBox);
        });
    }

    @Override
    public void onPhase1Complete(boolean feasible) {
        Platform.runLater(() -> {
            Label completeLabel = new Label(feasible ? "✓ Phase 1 Complete - Feasible solution found" : "✗ Phase 1 Complete - Problem is INFEASIBLE");
            completeLabel.setStyle(feasible ? "-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-padding: 5;" : "-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-padding: 5;");
            solutionDisplay.getChildren().add(completeLabel);
        });
    }

    @Override
    public void onPhase2Step(Tableau tableau,
                             String entering,
                             String leaving,
                             int iteration,
                             List<String> basisVariableNames,
                             List<String> variableNames) {
        Platform.runLater(() -> {
            if (iteration == 1) {
                Label phase2Title = new Label("Phase 2 - Optimizing Objective Function");
                phase2Title.setFont(Font.font("System", FontWeight.BOLD, 14));
                phase2Title.setStyle("-fx-text-fill: #28a745; -fx-padding: 10 0 5 0;");
                solutionDisplay.getChildren().add(phase2Title);
            }

            VBox iterBox = createIterationDisplay(
                    "Phase 2",
                    iteration,
                    entering,
                    leaving,
                    new Tableau(tableau),
                    basisVariableNames.toArray(new String[0]),
                    variableNames.toArray(new String[0]));
            solutionDisplay.getChildren().add(iterBox);
        });
    }

    @Override
    public void onPhase2Complete(Fraction optimalValue,
                                 Fraction[] solution, ConstraintType.SolutionStatus status) {
        Platform.runLater(() -> {
            VBox resultsBox = new VBox(8);
            resultsBox.setPadding(new Insets(12));
            resultsBox.setStyle(
                    "-fx-background-color: #d4edda; -fx-border-color: #28a745; -fx-border-width: 2;");

            Label resultsTitle = new Label("Final Results");
            resultsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

            Label statusLabel = new Label("Status: " + status);
            statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
            statusLabel.setTextFill(javafx.scene.paint.Color.web(status == ConstraintType.SolutionStatus.OPTIMAL ? "#28a745" : "#dc3545"));

            Label optimalLabel = new Label(String.format(
                    "Optimal Value: %s = %.4f",
                    optimalValue.toString(),
                    optimalValue.doubleValue()));
            optimalLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

            StringBuilder solText = new StringBuilder("Solution: ");
            for (int i = 0; i < solution.length; i++) {
                if (i > 0) solText.append(", ");
                solText.append(String.format(
                        "x%d = %s = %.4f",
                        i + 1,
                        solution[i].toString(),
                        solution[i].doubleValue()));
            }
            Label solutionLabel = new Label(solText.toString());

            resultsBox.getChildren().addAll(resultsTitle, statusLabel, optimalLabel, solutionLabel);
            solutionDisplay.getChildren().add(resultsBox);
        });
    }

    @Override
    public void onError(String message) {
        Platform.runLater(() -> {
            Label errorLabel = new Label("ERROR: " + message);
            errorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; " + "-fx-padding: 10; -fx-background-color: #f8d7da; " + "-fx-border-color: #dc3545; -fx-border-width: 2;");
            solutionDisplay.getChildren().add(errorLabel);
        });
    }

    private VBox createIterationDisplay(String phase,
                                        int iteration,
                                        String entering,
                                        String leaving,
                                        FieldMatrix<Fraction> tableau,
                                        String[] currentBasisVariables,
                                        String[] currentVariableNames) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");

        Label iterLabel = new Label(phase + " - Iteration " + iteration);
        iterLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        Label enteringLabel = new Label("Entering: " + entering);
        enteringLabel.setStyle("-fx-text-fill: #0066cc;");

        Label leavingLabel = new Label("Leaving: " + leaving);
        leavingLabel.setStyle("-fx-text-fill: #cc0000;");

        // Render tableau using TableauRenderer
        int highLightRow = Arrays.asList(currentBasisVariables).indexOf(leaving);
        int highLightCol = Arrays.asList(currentVariableNames).indexOf(entering);
        GridPane tableauGrid = TableauRenderer.createTableauGrid(
                tableau,
                currentBasisVariables, currentVariableNames, highLightRow, highLightCol

        );

        box.getChildren().addAll(iterLabel, enteringLabel, leavingLabel, tableauGrid);
        return box;
    }

    private void clearSolution() {
        solutionDisplay.getChildren().clear();
        Label solutionTitle = new Label("Solution Steps");
        solutionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        solutionDisplay.getChildren().add(solutionTitle);
    }


    private static class ConstraintRow {
        List<TextField> coefficients = new ArrayList<>();
        ComboBox<String> constraintType;
        TextField rhsValue;
        HBox displayBox;

        ConstraintRow(int numVars, int rowNum) {
            for (int i = 0; i < numVars; i++) {
                TextField tf = new TextField("0");
                tf.setPrefWidth(60);
                coefficients.add(tf);
            }

            constraintType = new ComboBox<>();
            constraintType.getItems().addAll("<=", "=", ">=");
            constraintType.setValue("<=");
            constraintType.setPrefWidth(60);

            rhsValue = new TextField("0");
            rhsValue.setPrefWidth(80);
        }

        void updateVariableCount(int newCount) {
            int currentCount = coefficients.size();

            if (newCount > currentCount) {
                for (int i = currentCount; i < newCount; i++) {
                    TextField tf = new TextField("0");
                    tf.setPrefWidth(60);
                    coefficients.add(tf);
                }
            } else if (newCount < currentCount) {
                coefficients.subList(newCount, currentCount).clear();
            }
        }
    }
}