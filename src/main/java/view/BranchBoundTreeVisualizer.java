package view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.GraphicSolutionType;
import models.Node;

import java.util.*;

public class BranchBoundTreeVisualizer extends Application {
    
    private static Node staticRootNode;
    private Pane treePane;
    
    // Dynamic spacing based on tree size
    private double nodeRadius;
    private double levelHeight;
    private double baseNodeSpacing;
    
    private static Stage primaryStage;
    private static volatile boolean fxLaunched = false;
    
    // Node info display components
    private Label nodeInfoTitle;
    private TextArea nodeInfoText;
    private Node selectedNode;
    private Circle selectedNodeCircle;
    
    // Tree data
    private Map<Node, Point> nodePositions;

    /**
     * Main method to visualize the branch and bound tree
     */
    public static void showTree(Node root) {
        staticRootNode = root;
        
        if (!fxLaunched) {
            // Launch JavaFX application
            new Thread(() -> {
                Application.launch(BranchBoundTreeVisualizer.class);
            }).start();
            
            // Wait for JavaFX to initialize
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            fxLaunched = true;
        }
        
        // // Update the tree visualization
        // Platform.runLater(() -> {
        //     if (primaryStage != null) {
        //         BranchBoundTreeVisualizer visualizer = new BranchBoundTreeVisualizer();
        //         try {
        //             Stage newStage = new Stage();
        //             visualizer.start(newStage);
        //         } catch (Exception e) {
        //             e.printStackTrace();
        //         }
        //     }
        // });
    }
    
    @Override
    public void start(Stage primaryStage) {
        BranchBoundTreeVisualizer.primaryStage = primaryStage;
        
        if (staticRootNode == null) {
            showError("No root node provided!");
            return;
        }
        
        // Create main layout
        BorderPane mainLayout = new BorderPane();
        
        // Create tree pane
        treePane = new Pane();
        treePane.setStyle("-fx-background-color: #f8f9fa;");
        
        // Create scroll pane for tree
        ScrollPane scrollPane = new ScrollPane(treePane);
        scrollPane.setPannable(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        
        // Create right panel with node info and controls
        VBox rightPanel = createRightPanel();
        
        mainLayout.setCenter(scrollPane);
        mainLayout.setRight(rightPanel);
        
        // Create scene with larger initial size
        Scene scene = new Scene(mainLayout, 1600, 1000);
        primaryStage.setTitle("Branch & Bound Tree Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Draw tree
        drawTreeAuto();
    }
    
    private void drawTreeAuto() {
        // Clear previous drawings
        treePane.getChildren().clear();
        selectedNode = null;
        selectedNodeCircle = null;
        updateNodeInfoPanel();
        
        if (staticRootNode == null) {
            showError("No root node provided!");
            return;
        }
        
        // Retrieve all nodes
        List<Node> allNodes = getAllNodes(staticRootNode);
        
        if (allNodes.isEmpty()) {
            showError("No nodes found in tree!");
            return;
        }
        
        // Assign levels to nodes if not already assigned
        assignLevels(staticRootNode, 0);
        
        // Calculate optimal spacing based on tree size
        calculateOptimalSpacing(allNodes);
        
        // Calculate positions using improved layout algorithm
        nodePositions = new HashMap<>();
        calculateTreePositions(staticRootNode, nodePositions, 0, 0);
        
        // Center the tree
        centerTree(nodePositions);
        
        // Draw connections first
        drawConnections(allNodes, nodePositions);
        
        // Draw nodes
        drawNodes(allNodes, nodePositions);
        
        // Adjust pane size
        adjustPaneSize(nodePositions);
    }
    
    private void calculateOptimalSpacing(List<Node> allNodes) {
        int nodeCount = allNodes.size();
        int treeDepth = getTreeDepth(staticRootNode);
        
        // Adjust parameters based on tree size
        if (nodeCount > 30) {
            // Large tree - compact layout
            nodeRadius = 20;
            levelHeight = 120;
            baseNodeSpacing = 150;
        } else if (nodeCount > 15) {
            // Medium tree - balanced layout
            nodeRadius = 25;
            levelHeight = 150;
            baseNodeSpacing = 200;
        } else {
            // Small tree - spacious layout
            nodeRadius = 30;
            levelHeight = 180;
            baseNodeSpacing = 250;
        }
        
        // Adjust further based on depth
        if (treeDepth > 6) {
            levelHeight = Math.max(100, levelHeight - (treeDepth - 6) * 10);
            baseNodeSpacing = Math.max(120, baseNodeSpacing - (treeDepth - 6) * 15);
        }
    }
    
    private void calculateTreePositions(Node node, Map<Node, Point> positions, double x, int level) {
        if (node == null) return;
        
        // Store position
        positions.put(node, new Point(x, level * levelHeight + 100));
        
        // Calculate child positions
        Node leftChild = node.getLeft();
        Node rightChild = node.getRight();
        
        if (leftChild == null && rightChild == null) {
            return;
        }
        
        // Calculate spacing for children
        double childSpacing = baseNodeSpacing / Math.pow(1.5, level);
        
        // Calculate subtree widths
        double leftWidth = getSubtreeWidth(leftChild);
        double rightWidth = getSubtreeWidth(rightChild);
        
        // Position left child
        if (leftChild != null) {
            double leftX = x - childSpacing * (rightWidth + 1) / (leftWidth + rightWidth + 2);
            calculateTreePositions(leftChild, positions, leftX, level + 1);
        }
        
        // Position right child
        if (rightChild != null) {
            double rightX = x + childSpacing * (leftWidth + 1) / (leftWidth + rightWidth + 2);
            calculateTreePositions(rightChild, positions, rightX, level + 1);
        }
    }
    
    private double getSubtreeWidth(Node node) {
        if (node == null) return 0;
        
        Node left = node.getLeft();
        Node right = node.getRight();
        
        if (left == null && right == null) {
            return 1;
        }
        
        return getSubtreeWidth(left) + getSubtreeWidth(right) + 1;
    }
    
    private void centerTree(Map<Node, Point> positions) {
        if (positions.isEmpty()) return;
        
        // Find boundaries
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        
        for (Point p : positions.values()) {
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }
        
        // Calculate center offset
        double centerX = (minX + maxX) / 2;
        double offsetX = 800 - centerX; // Center at 800px
        
        // Apply offset to all positions
        for (Node node : positions.keySet()) {
            Point p = positions.get(node);
            positions.put(node, new Point(p.x + offsetX, p.y));
        }
    }
    
    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setStyle("-fx-background-color: #f0f0f0;");
        rightPanel.setPrefWidth(350);
        
        // Tree info section
        VBox treeInfoSection = createTreeInfoSection();
        
        // Node info section
        VBox nodeInfoSection = createNodeInfoSection();
        
        // Spacing controls section
        VBox spacingControls = createSpacingControls();
        
        // Action buttons
        HBox buttonBox = createActionButtons();
        
        rightPanel.getChildren().addAll(
            treeInfoSection,
            new Separator(),
            nodeInfoSection,
            new Separator(),
            spacingControls,
            new Separator(),
            buttonBox
        );
        
        return rightPanel;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button clearSelectionBtn = new Button("Clear Selection");
        clearSelectionBtn.setPrefWidth(140);
        clearSelectionBtn.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
        clearSelectionBtn.setOnAction(e -> clearNodeSelection());
        
        Button autoLayoutBtn = new Button("Auto Layout");
        autoLayoutBtn.setPrefWidth(140);
        autoLayoutBtn.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
        autoLayoutBtn.setOnAction(e -> drawTreeAuto());
        
        buttonBox.getChildren().addAll(clearSelectionBtn, autoLayoutBtn);
        return buttonBox;
    }
    
    private VBox createTreeInfoSection() {
        VBox section = new VBox(10);
        
        Label title = new Label("Tree Overview");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.DARKBLUE);
        
        VBox statsBox = new VBox(8);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        if (staticRootNode != null) {
            List<Node> allNodes = getAllNodes(staticRootNode);
            long integerNodes = allNodes.stream()
                .filter(n -> n.isSolutionInteger() == true)
                .count();
            long fractionalNodes = allNodes.stream()
                .filter(n -> n.isSolutionInteger() == false)
                .count();
            
            statsBox.getChildren().addAll(
                createInfoRow("Total Nodes:", String.valueOf(allNodes.size())),
                createInfoRow("Tree Depth:", String.valueOf(getTreeDepth(staticRootNode))),
                createInfoRow("Integer Nodes:", String.valueOf(integerNodes)),
                createInfoRow("Fractional Nodes:", String.valueOf(fractionalNodes)),
                createInfoRow("Current Layout:", 
                    String.format("Node: %.0fpx, Level: %.0fpx, Spacing: %.0fpx", 
                        nodeRadius, levelHeight, baseNodeSpacing))
            );
        }
        
        section.getChildren().addAll(title, statsBox);
        return section;
    }
    
    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelLbl = new Label(label);
        labelLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        labelLbl.setPrefWidth(120);
        
        Label valueLbl = new Label(value);
        valueLbl.setFont(Font.font("Arial", 12));
        valueLbl.setTextFill(Color.DARKGREEN);
        
        row.getChildren().addAll(labelLbl, valueLbl);
        return row;
    }
    
    private VBox createNodeInfoSection() {
        VBox section = new VBox(10);
        
        nodeInfoTitle = new Label("Node Information");
        nodeInfoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nodeInfoTitle.setTextFill(Color.DARKBLUE);
        
        // Node info display area
        nodeInfoText = new TextArea();
        nodeInfoText.setEditable(false);
        nodeInfoText.setWrapText(true);
        nodeInfoText.setPrefHeight(250);
        nodeInfoText.setStyle("-fx-font-family: 'Monospace'; -fx-font-size: 12px;");
        nodeInfoText.setText("Click on any node to view its details");
        
        // Instructions
        Label instructions = new Label("Click any node in the tree to view its details");
        instructions.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        instructions.setTextFill(Color.DARKGRAY);
        
        section.getChildren().addAll(nodeInfoTitle, instructions, nodeInfoText);
        return section;
    }
    
    private VBox createSpacingControls() {
        VBox controls = new VBox(10);
        
        Label title = new Label("Tree Layout Controls");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Node size control
        VBox nodeSizeControl = new VBox(5);
        Label nodeSizeLabel = new Label("Node Size: " + String.format("%.0f", nodeRadius));
        nodeSizeLabel.setFont(Font.font("Arial", 12));
        
        Slider nodeSizeSlider = new Slider(15, 40, nodeRadius);
        nodeSizeSlider.setShowTickLabels(true);
        nodeSizeSlider.setShowTickMarks(true);
        nodeSizeSlider.setMajorTickUnit(5);
        nodeSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            nodeRadius = newVal.doubleValue();
            nodeSizeLabel.setText("Node Size: " + String.format("%.0f", nodeRadius));
            redrawTree();
        });
        
        // Vertical spacing control
        VBox verticalControl = new VBox(5);
        Label verticalLabel = new Label("Level Height: " + String.format("%.0f", levelHeight));
        verticalLabel.setFont(Font.font("Arial", 12));
        
        Slider verticalSpacingSlider = new Slider(80, 250, levelHeight);
        verticalSpacingSlider.setShowTickLabels(true);
        verticalSpacingSlider.setShowTickMarks(true);
        verticalSpacingSlider.setMajorTickUnit(30);
        verticalSpacingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            levelHeight = newVal.doubleValue();
            verticalLabel.setText("Level Height: " + String.format("%.0f", levelHeight));
            redrawTree();
        });
        
        // Horizontal spacing control
        VBox horizontalControl = new VBox(5);
        Label horizontalLabel = new Label("Base Spacing: " + String.format("%.0f", baseNodeSpacing));
        horizontalLabel.setFont(Font.font("Arial", 12));
        
        Slider horizontalSpacingSlider = new Slider(100, 350, baseNodeSpacing);
        horizontalSpacingSlider.setShowTickLabels(true);
        horizontalSpacingSlider.setShowTickMarks(true);
        horizontalSpacingSlider.setMajorTickUnit(50);
        horizontalSpacingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            baseNodeSpacing = newVal.doubleValue();
            horizontalLabel.setText("Base Spacing: " + String.format("%.0f", baseNodeSpacing));
            redrawTree();
        });
        
        nodeSizeControl.getChildren().addAll(nodeSizeLabel, nodeSizeSlider);
        verticalControl.getChildren().addAll(verticalLabel, verticalSpacingSlider);
        horizontalControl.getChildren().addAll(horizontalLabel, horizontalSpacingSlider);
        
        controls.getChildren().addAll(title, nodeSizeControl, verticalControl, horizontalControl);
        return controls;
    }
    
    private void redrawTree() {
        if (staticRootNode == null || nodePositions == null) return;
        
        // Clear previous drawings
        treePane.getChildren().clear();
        
        List<Node> allNodes = getAllNodes(staticRootNode);
        
        // Recalculate positions with current spacing
        nodePositions.clear();
        calculateTreePositions(staticRootNode, nodePositions, 0, 0);
        centerTree(nodePositions);
        
        // Draw connections
        drawConnections(allNodes, nodePositions);
        
        // Draw nodes
        drawNodes(allNodes, nodePositions);
        
        // Adjust pane size
        adjustPaneSize(nodePositions);
    }
    
    private void clearNodeSelection() {
        if (selectedNodeCircle != null) {
            // Reset to original color
            selectedNodeCircle.setFill(getNodeColor(selectedNode));
            selectedNodeCircle.setStroke(Color.BLACK);
            selectedNodeCircle.setStrokeWidth(2);
        }
        selectedNode = null;
        selectedNodeCircle = null;
        updateNodeInfoPanel();
    }
    
    private void selectNode(Node node, Circle nodeCircle) {
        // Clear previous selection
        clearNodeSelection();
        
        // Set new selection
        selectedNode = node;
        selectedNodeCircle = nodeCircle;
        
        // Highlight selected node
        nodeCircle.setFill(Color.DEEPSKYBLUE);
        nodeCircle.setStroke(Color.DARKBLUE);
        nodeCircle.setStrokeWidth(3);
        
        // Update info panel
        updateNodeInfoPanel();
    }
    
    private void updateNodeInfoPanel() {
        if (selectedNode == null) {
            nodeInfoTitle.setText("Node Information");
            nodeInfoText.setText("Click on any node to view its details");
        } else {
            nodeInfoTitle.setText("Node ID: " + selectedNode.getId());
            
            // Create detailed node information
            StringBuilder info = new StringBuilder();
            info.append("═══════════════════════════════\n");
            info.append("BASIC INFORMATION\n");
            info.append("═══════════════════════════════\n");
            info.append(String.format("• ID: %d\n", selectedNode.getId()));
            info.append(String.format("• Value: %.2f\n", selectedNode.getValue()));
            info.append(String.format("• Level: %d\n", selectedNode.getLevel()));
            info.append(String.format("• Type: %s\n", selectedNode.getType()));
            info.append(String.format("• Feasible: %s\n", selectedNode.getType().equals(GraphicSolutionType.FEASIBLE)));
            info.append(String.format("• UB: (%.2f , %.2f)\n",
                     selectedNode.getUB() == null ? Double.NEGATIVE_INFINITY : selectedNode.getUB().getX() , 
                    selectedNode.getUB() == null ? Double.NEGATIVE_INFINITY : selectedNode.getUB().getY() ));
            
            // info.append(String.format("• Feasible: %s\n", selectedNode.getType().equals(GraphicSolutionType.FEASIBLE)));
            info.append(String.format("• LB: (%.2f , %.2f)\n",
                     selectedNode.getLB() == null ? Double.NEGATIVE_INFINITY : selectedNode.getLB().getX() , 
                    selectedNode.getLB() == null ? Double.NEGATIVE_INFINITY : selectedNode.getLB().getY() ));
            info.append("objective function coeff"  + selectedNode.getObjectiveFunction() + "\n") ;             
            info.append( "constraints coeff" + selectedNode.getConstraints() + "\n") ;             
            info.append("\n═══════════════════════════════\n");
            info.append("RELATIONSHIPS\n");
            info.append("═══════════════════════════════\n");
            info.append(String.format("• Parent ID: %s\n", 
                selectedNode.getParent() != null ? String.valueOf(selectedNode.getParent().getId()) : "None (Root)"));
            info.append(String.format("• Left Child ID: %s\n", 
                selectedNode.getLeft() != null ? String.valueOf(selectedNode.getLeft().getId()) : "None"));
            info.append(String.format("• Right Child ID: %s\n", 
                selectedNode.getRight() != null ? String.valueOf(selectedNode.getRight().getId()) : "None"));
            
            info.append("\n═══════════════════════════════\n");
            info.append("STATUS\n");
            info.append("═══════════════════════════════\n");
            // if (selectedNode.getType() == GraphicSolutionType.INTEGER) {
            //     info.append("✓ INTEGER SOLUTION\n");
            //     info.append("  This node represents an integer solution\n");
            // } else if (selectedNode.getType() == GraphicSolutionType.FRACTIONAL) {
            //     info.append("ⓘ FRACTIONAL SOLUTION\n");
            //     info.append("  This node may need further branching\n");
            // }
            if (selectedNode.isSolutionInteger()) {
                info.append("✓ INTEGER SOLUTION\n");
                info.append("  This node represents an integer solution\n");
            } else  {
                info.append("ⓘ FRACTIONAL SOLUTION\n");
                info.append("  This node may need further branching\n");
            }
            
            if (!selectedNode.isFeasible()) {
                info.append("✗ PRUNED/INFEASIBLE\n");
                info.append("  This node was pruned from the search\n");
            }
            
            nodeInfoText.setText(info.toString());
        }
    }
    
    private List<Node> getAllNodes(Node root) {
        List<Node> nodes = new ArrayList<>();
        collectNodesRecursively(root, nodes);
        return nodes;
    }
    
    private void collectNodesRecursively(Node node, List<Node> nodes) {
        if (node == null) return;
        
        nodes.add(node);
        
        collectNodesRecursively(node.getLeft(), nodes);
        collectNodesRecursively(node.getRight(), nodes);
    }
    
    private void assignLevels(Node node, int level) {
        if (node == null) return;
        
        node.setLevel(level);
        
        assignLevels(node.getLeft(), level + 1);
        assignLevels(node.getRight(), level + 1);
    }
    
    private void drawConnections(List<Node> nodes, Map<Node, Point> positions) {
        for (Node node : nodes) {
            Point parentPos = positions.get(node);
            
            if (node.getLeft() != null && positions.containsKey(node.getLeft())) {
                Point leftPos = positions.get(node.getLeft());
                Line leftLine = createConnectionLine(parentPos, leftPos);
                treePane.getChildren().add(leftLine);
            }
            
            if (node.getRight() != null && positions.containsKey(node.getRight())) {
                Point rightPos = positions.get(node.getRight());
                Line rightLine = createConnectionLine(parentPos, rightPos);
                treePane.getChildren().add(rightLine);
            }
        }
    }
    
    private Line createConnectionLine(Point parentPos, Point childPos) {
        Line line = new Line(parentPos.x, parentPos.y + nodeRadius, 
                            childPos.x, childPos.y - nodeRadius);
        line.setStroke(Color.DARKGRAY);
        line.setStrokeWidth(2);
        return line;
    }
    
    private void drawNodes(List<Node> nodes, Map<Node, Point> positions) {
        for (Node node : nodes) {
            Point pos = positions.get(node);
            
            Circle circle = new Circle(pos.x, pos.y, nodeRadius);
            circle.setFill(getNodeColor(node));
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(2);
            
            // Make circle clickable
            circle.setOnMouseClicked(e -> selectNode(node, circle));
            
            String nodeText = "ID: " + node.getId() + "\nVal: " + String.format("%.2f", node.getValue());
            Text text = new Text(nodeText);
            text.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            
            double textWidth = text.getLayoutBounds().getWidth();
            double textHeight = text.getLayoutBounds().getHeight();
            text.setX(pos.x - textWidth / 2);
            text.setY(pos.y + textHeight / 4);
            
            // Make text also clickable
            text.setOnMouseClicked(e -> selectNode(node, circle));
            
            // Tooltip for hover
            Tooltip tooltip = new Tooltip(
                "Node ID: " + node.getId() + "\n" +
                "Click to select\n" +
                String.format("Level: %d", node.getLevel())
            );
            Tooltip.install(circle, tooltip);
            Tooltip.install(text, tooltip);
            
            treePane.getChildren().addAll(circle, text);
        }
    }
    
    private Color getNodeColor(Node node) {
        Color color_returned ; 
        if (node.isSolutionInteger()) {
            color_returned =  Color.GOLD;
        } else  {
            color_returned =  Color.LIGHTBLUE;
        } 
         if ( ! node.isFeasible()) {
             return Color.LIGHTCORAL;
        }

        return color_returned ; 
    }
    
    private void adjustPaneSize(Map<Node, Point> positions) {
        if (positions.isEmpty()) return;
        
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        
        for (Point p : positions.values()) {
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }
        
        // Add generous padding
        double padding = 200;
        treePane.setPrefSize(
            Math.max(1600, maxX - minX + 2 * padding),
            Math.max(1000, maxY - minY + 2 * padding)
        );
        
        // Center the tree in the pane
        double offsetX = padding - minX;
        double offsetY = padding - minY;
        
        for (var node : treePane.getChildren()) {
            if (node instanceof Circle) {
                Circle circle = (Circle) node;
                circle.setCenterX(circle.getCenterX() + offsetX);
                circle.setCenterY(circle.getCenterY() + offsetY);
            } else if (node instanceof Text) {
                Text text = (Text) node;
                text.setX(text.getX() + offsetX);
                text.setY(text.getY() + offsetY);
            } else if (node instanceof Line) {
                Line line = (Line) node;
                line.setStartX(line.getStartX() + offsetX);
                line.setStartY(line.getStartY() + offsetY);
                line.setEndX(line.getEndX() + offsetX);
                line.setEndY(line.getEndY() + offsetY);
            }
        }
    }
    
    private int getTreeDepth(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(
            getTreeDepth(node.getLeft()),
            getTreeDepth(node.getRight())
        );
    }
    
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private static class Point {
        public double x, y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}