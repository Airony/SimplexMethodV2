package view;

import javafx.application.Application;
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

import java.util.ArrayList;
import java.util.List;

public class SimplexSolverUI extends Application {

    private final List<ConstraintRow> constraintRows = new ArrayList<>();
    private final List<TextField> objectiveCoefficients = new ArrayList<>();
    private int numVariables = 3;
    private ComboBox<String> objectiveType;
    private VBox constraintsContainer;
    private HBox objectiveContainer;
    private VBox solutionDisplay;
    private ScrollPane solutionScrollPane;

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

        solutionScrollPane = new ScrollPane(solutionDisplay);
        solutionScrollPane.setFitToWidth(true);
        solutionScrollPane.setPrefWidth(400);

        // Layout
        SplitPane splitPane = new SplitPane(inputScrollPane, solutionScrollPane);
        splitPane.setDividerPositions(0.6);
        root.setCenter(splitPane);

        Scene scene = new Scene(root, 1200, 700);
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

        // Display mock solution (replace with actual solver integration)
        displayMockSolution();
    }

    private void displayMockSolution() {
        // Phase 1 Header
        Label phase1Title = new Label("Phase 1 - Finding Initial Feasible Solution");
        phase1Title.setFont(Font.font("System", FontWeight.BOLD, 14));
        phase1Title.setStyle("-fx-text-fill: #0066cc;");
        solutionDisplay.getChildren().add(phase1Title);

        // Mock Phase 1 iterations
        for (int i = 1; i <= 2; i++) {
            VBox iterBox = createIterationDisplay("Phase 1", i, "x" + i, "s" + i);
            solutionDisplay.getChildren().add(iterBox);
        }

        // Phase 2 Header
        Label phase2Title = new Label("Phase 2 - Optimizing Objective Function");
        phase2Title.setFont(Font.font("System", FontWeight.BOLD, 14));
        phase2Title.setStyle("-fx-text-fill: #009900;");
        solutionDisplay.getChildren().add(phase2Title);

        // Mock Phase 2 iterations
        for (int i = 1; i <= 3; i++) {
            VBox iterBox = createIterationDisplay("Phase 2", i, "x" + (i + 1), "s" + i);
            solutionDisplay.getChildren().add(iterBox);
        }

        // Final Results
        VBox resultsBox = new VBox(5);
        resultsBox.setPadding(new Insets(10));
        resultsBox.setStyle(
                "-fx-background-color: #e7f3ff; -fx-border-color: #0066cc; -fx-border-width: 2;");

        Label resultsTitle = new Label("Final Results");
        resultsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label statusLabel = new Label("Status: OPTIMAL");
        statusLabel.setStyle("-fx-text-fill: #009900; -fx-font-weight: bold;");

        Label optimalValue = new Label("Optimal Value: 150.00");
        Label solution = new Label("Solution: x1 = 10.00, x2 = 5.00, x3 = 0.00");

        resultsBox.getChildren().addAll(resultsTitle, statusLabel, optimalValue, solution);
        solutionDisplay.getChildren().add(resultsBox);
    }

    private VBox createIterationDisplay(String phase,
                                        int iteration,
                                        String entering,
                                        String leaving) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1;");

        Label iterLabel = new Label(phase + " - Iteration " + iteration);
        iterLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label enteringLabel = new Label("Entering: " + entering);
        Label leavingLabel = new Label("Leaving: " + leaving);

        // Mock tableau display
        GridPane tableau = new GridPane();
        tableau.setHgap(8);
        tableau.setVgap(4);
        tableau.setPadding(new Insets(5));
        tableau.setStyle("-fx-background-color: #f8f9fa;");

        String[][] mockData = {
                {"Basis", "x1", "x2", "x3", "RHS"},
                {"s1", "1/2", "3/4", "0", "10"},
                {"s2", "1/3", "0", "1/2", "5"},
                {"z", "-2", "-3", "0", "0"}
        };

        for (int i = 0; i < mockData.length; i++) {
            for (int j = 0; j < mockData[i].length; j++) {
                Label cell = new Label(mockData[i][j]);
                cell.setStyle("-fx-padding: 2 8;");
                if (i == 0) cell.setStyle("-fx-font-weight: bold; -fx-padding: 2 8;");
                tableau.add(cell, j, i);
            }
        }

        box.getChildren().addAll(iterLabel, enteringLabel, leavingLabel, tableau);
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