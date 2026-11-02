package com.research.pubmanager.controller;

import com.research.pubmanager.dao.PublicationDAO;
import com.research.pubmanager.dao.PublicationDAOImpl;
import com.research.pubmanager.model.Publication;
import com.research.pubmanager.service.CrossrefVerificationService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML private Label lblTotalCount;
    @FXML private Label lblVerifiedCount;
    @FXML private Label lblPendingCount;
    @FXML private Label lblRejectedCount;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;

    @FXML private TableView<Publication> publicationTable;
    @FXML private TableColumn<Publication, Number> colId;
    @FXML private TableColumn<Publication, String> colTitle;
    @FXML private TableColumn<Publication, String> colDoi;
    @FXML private TableColumn<Publication, String> colAuthors;
    @FXML private TableColumn<Publication, Number> colYear;
    @FXML private TableColumn<Publication, String> colJournal;
    @FXML private TableColumn<Publication, String> colStatus;
    @FXML private TableColumn<Publication, String> colNotes;

    private final ObservableList<Publication> masterList = FXCollections.observableArrayList();
    private final PublicationDAO dao = new PublicationDAOImpl();
    private final CrossrefVerificationService verificationService = new CrossrefVerificationService();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadData();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colTitle.setCellValueFactory(data -> data.getValue().titleProperty());
        colDoi.setCellValueFactory(data -> data.getValue().doiProperty());
        colAuthors.setCellValueFactory(data -> data.getValue().authorsProperty());
        colYear.setCellValueFactory(data -> data.getValue().yearProperty());
        colJournal.setCellValueFactory(data -> data.getValue().journalProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colNotes.setCellValueFactory(data -> data.getValue().verificationNotesProperty());

        // Custom status badge rendering
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toUpperCase()) {
                        case "VERIFIED" -> setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                        case "REJECTED" -> setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                        case "DISCREPANCY" -> setStyle("-fx-text-fill: #ea580c; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: #ca8a04; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void setupFilters() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList("ALL", "VERIFIED", "PENDING", "REJECTED", "DISCREPANCY"));
        statusFilterComboBox.setValue("ALL");

        FilteredList<Publication> filtered = new FilteredList<>(masterList, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filtered));
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filtered));

        publicationTable.setItems(filtered);
    }

    private void applyFilter(FilteredList<Publication> filtered) {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = statusFilterComboBox.getValue();

        filtered.setPredicate(pub -> {
            boolean matchesSearch = search.isEmpty()
                    || (pub.getTitle() != null && pub.getTitle().toLowerCase().contains(search))
                    || (pub.getAuthors() != null && pub.getAuthors().toLowerCase().contains(search))
                    || (pub.getDoi() != null && pub.getDoi().toLowerCase().contains(search))
                    || (pub.getJournal() != null && pub.getJournal().toLowerCase().contains(search));

            boolean matchesStatus = selectedStatus == null
                    || "ALL".equalsIgnoreCase(selectedStatus)
                    || pub.getStatus().equalsIgnoreCase(selectedStatus);

            return matchesSearch && matchesStatus;
        });
    }

    public void loadData() {
        try {
            masterList.setAll(dao.findAll());
            updateMetrics();
        } catch (Exception e) {
            showAlert("Database Connection Notice",
                    "Could not load publications from MySQL: " + e.getMessage() +
                    "\nPlease check if MySQL Server is running and credentials in DatabaseConnection.java match.",
                    Alert.AlertType.ERROR);
        }
    }

    private void updateMetrics() {
        try {
            int total = masterList.size();
            long verified = masterList.stream().filter(p -> "VERIFIED".equalsIgnoreCase(p.getStatus())).count();
            long pending = masterList.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getStatus())).count();
            long rejected = masterList.stream().filter(p -> "REJECTED".equalsIgnoreCase(p.getStatus()) || "DISCREPANCY".equalsIgnoreCase(p.getStatus())).count();

            lblTotalCount.setText(String.valueOf(total));
            lblVerifiedCount.setText(String.valueOf(verified));
            lblPendingCount.setText(String.valueOf(pending));
            lblRejectedCount.setText(String.valueOf(rejected));
        } catch (Exception ignored) {}
    }

    @FXML
    void handleRefresh() {
        loadData();
    }

    @FXML
    void handleQuickDoiImport() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Fast DOI Import");
        dialog.setHeaderText("Import & Verify Publication from Official Crossref Registry");
        dialog.setContentText("Enter Paper DOI (e.g. 10.1109/CVPR.2016.90):");

        dialog.showAndWait().ifPresent(doi -> {
            if (doi.trim().isEmpty()) return;

            Task<Publication> fetchTask = new Task<>() {
                @Override
                protected Publication call() throws Exception {
                    return verificationService.fetchByDoi(doi);
                }
            };

            fetchTask.setOnSucceeded(e -> {
                Publication fetched = fetchTask.getValue();
                try {
                    dao.save(fetched);
                    loadData();
                    showAlert("Import Successful",
                            "Fetched & Verified:\n\nTitle: " + fetched.getTitle() +
                            "\nAuthors: " + fetched.getAuthors() +
                            "\nYear: " + fetched.getYear() +
                            "\nJournal: " + fetched.getJournal(),
                            Alert.AlertType.INFORMATION);
                } catch (Exception ex) {
                    showAlert("Save Error", "Could not save to database: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            });

            fetchTask.setOnFailed(e -> {
                Throwable ex = fetchTask.getException();
                showAlert("Import Failed", "Failed to retrieve DOI metadata: " + ex.getMessage(), Alert.AlertType.ERROR);
            });

            new Thread(fetchTask).start();
        });
    }

    @FXML
    void handleOpenAddDialog() {
        openPublicationDialog(null);
    }

    @FXML
    void handleEditSelected() {
        Publication selected = publicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a publication from the table to edit.", Alert.AlertType.WARNING);
            return;
        }
        openPublicationDialog(selected);
    }

    private void openPublicationDialog(Publication publication) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/research/pubmanager/view/PublicationDialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(publication == null ? "Add Publication" : "Edit Publication");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            PublicationDialogController controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setPublication(publication);

            stage.showAndWait();

            if (controller.isSaveClicked()) {
                loadData();
            }
        } catch (IOException e) {
            showAlert("UI Error", "Could not open dialog: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void handleVerifySelected() {
        Publication selected = publicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a publication to verify online.", Alert.AlertType.WARNING);
            return;
        }

        Task<CrossrefVerificationService.VerificationReport> verifyTask = new Task<>() {
            @Override
            protected CrossrefVerificationService.VerificationReport call() throws Exception {
                Publication official = verificationService.fetchByDoi(selected.getDoi());
                return verificationService.compareAndVerify(selected, official);
            }
        };

        verifyTask.setOnSucceeded(event -> {
            CrossrefVerificationService.VerificationReport report = verifyTask.getValue();
            Publication official = report.getOfficialRecord();

            String status = report.isMatched() ? "VERIFIED" : "DISCREPANCY";
            String note = "Crossref Verification: " + report.getSummary();

            String dialogMsg = String.format(
                    "Registry Check Result: %s\n\nOfficial Details:\n• Title: %s\n• Authors: %s\n• Year: %d\n• Container: %s\n\nNotes:\n%s\n\nUpdate publication status to %s?",
                    report.isMatched() ? "MATCH CONFIRMED" : "DISCREPANCIES DETECTED",
                    official.getTitle(), official.getAuthors(), official.getYear(), official.getJournal(),
                    report.getSummary(), status
            );

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, dialogMsg, ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Verification Result");
            confirm.setHeaderText(report.isMatched() ? "Verified Against Official Crossref Records" : "Discrepancy Flagged");
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    try {
                        dao.updateVerificationStatus(selected.getId(), status, note);
                        loadData();
                    } catch (Exception ex) {
                        showAlert("Database Error", "Could not update status: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        });

        verifyTask.setOnFailed(event -> {
            Throwable ex = verifyTask.getException();
            showAlert("Verification Failed", "Crossref DOI check failed: " + ex.getMessage(), Alert.AlertType.ERROR);
        });

        new Thread(verifyTask).start();
    }

    @FXML
    void handleMarkVerified() {
        updateSelectedStatus("VERIFIED", "Manually verified by reviewer.");
    }

    @FXML
    void handleMarkRejected() {
        updateSelectedStatus("REJECTED", "Marked rejected by reviewer.");
    }

    private void updateSelectedStatus(String status, String notes) {
        Publication selected = publicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a publication first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            dao.updateVerificationStatus(selected.getId(), status, notes);
            loadData();
        } catch (Exception e) {
            showAlert("Error", "Could not update status: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleDeleteSelected() {
        Publication selected = publicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a publication to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete publication #" + selected.getId() + " - \"" + selected.getTitle() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    dao.delete(selected.getId());
                    loadData();
                } catch (Exception e) {
                    showAlert("Database Error", "Could not delete: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
