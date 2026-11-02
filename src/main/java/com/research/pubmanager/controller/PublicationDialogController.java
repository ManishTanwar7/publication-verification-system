package com.research.pubmanager.controller;

import com.research.pubmanager.dao.PublicationDAO;
import com.research.pubmanager.dao.PublicationDAOImpl;
import com.research.pubmanager.model.Publication;
import com.research.pubmanager.service.CrossrefVerificationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class PublicationDialogController {

    @FXML private Label dialogTitleLabel;
    @FXML private TextField doiField;
    @FXML private TextField titleField;
    @FXML private TextField authorsField;
    @FXML private TextField yearField;
    @FXML private TextField journalField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextArea notesArea;

    private final PublicationDAO dao = new PublicationDAOImpl();
    private final CrossrefVerificationService verificationService = new CrossrefVerificationService();

    private Publication currentPublication;
    private boolean saveClicked = false;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList("PENDING", "VERIFIED", "REJECTED", "DISCREPANCY"));
        statusComboBox.setValue("PENDING");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPublication(Publication publication) {
        this.currentPublication = publication;

        if (publication != null) {
            dialogTitleLabel.setText("Edit Publication #" + publication.getId());
            doiField.setText(publication.getDoi());
            titleField.setText(publication.getTitle());
            authorsField.setText(publication.getAuthors());
            yearField.setText(publication.getYear() > 0 ? String.valueOf(publication.getYear()) : "");
            journalField.setText(publication.getJournal());
            statusComboBox.setValue(publication.getStatus());
            notesArea.setText(publication.getVerificationNotes());
        } else {
            dialogTitleLabel.setText("Add New Publication");
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    void handleAutoFetch() {
        String doi = doiField.getText();
        if (doi == null || doi.trim().isEmpty()) {
            showAlert("Input Required", "Please enter a DOI to fetch metadata from Crossref.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Publication fetched = verificationService.fetchByDoi(doi);
            doiField.setText(fetched.getDoi());
            titleField.setText(fetched.getTitle());
            authorsField.setText(fetched.getAuthors());
            yearField.setText(String.valueOf(fetched.getYear()));
            journalField.setText(fetched.getJournal());
            statusComboBox.setValue("VERIFIED");
            notesArea.setText("Verified & metadata fetched from Crossref Registry.");
            showAlert("Metadata Retrieved", "Successfully fetched details for: " + fetched.getTitle(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Fetch Error", "Could not fetch metadata: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleSave() {
        String doi = doiField.getText() == null ? "" : doiField.getText().trim();
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String authors = authorsField.getText() == null ? "" : authorsField.getText().trim();
        String yearText = yearField.getText() == null ? "" : yearField.getText().trim();
        String journal = journalField.getText() == null ? "" : journalField.getText().trim();
        String status = statusComboBox.getValue();
        String notes = notesArea.getText() == null ? "" : notesArea.getText().trim();

        if (doi.isEmpty() || title.isEmpty()) {
            showAlert("Validation Error", "DOI and Title are required fields.", Alert.AlertType.WARNING);
            return;
        }

        int year = 0;
        if (!yearText.isEmpty()) {
            try {
                year = Integer.parseInt(yearText);
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Publication Year must be a valid number.", Alert.AlertType.WARNING);
                return;
            }
        }

        try {
            if (currentPublication == null) {
                // New Publication
                Publication newPub = new Publication(0, title, doi, authors, year, journal, status, notes);
                dao.save(newPub);
            } else {
                // Update Existing
                currentPublication.setDoi(doi);
                currentPublication.setTitle(title);
                currentPublication.setAuthors(authors);
                currentPublication.setYear(year);
                currentPublication.setJournal(journal);
                currentPublication.setStatus(status);
                currentPublication.setVerificationNotes(notes);
                dao.update(currentPublication);
            }

            saveClicked = true;
            if (dialogStage != null) {
                dialogStage.close();
            }
        } catch (Exception e) {
            showAlert("Database Error", "Failed to save publication: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
