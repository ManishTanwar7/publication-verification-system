package com.research.pubmanager.model;

import javafx.beans.property.*;

/**
 * Model representing a scholarly research publication with JavaFX observable properties.
 */
public class Publication {

    private final IntegerProperty id = new SimpleIntegerProperty(0);
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty doi = new SimpleStringProperty("");
    private final StringProperty authors = new SimpleStringProperty("");
    private final IntegerProperty year = new SimpleIntegerProperty(0);
    private final StringProperty journal = new SimpleStringProperty("");
    private final StringProperty status = new SimpleStringProperty("PENDING");
    private final StringProperty verificationNotes = new SimpleStringProperty("");

    public Publication() {}

    public Publication(int id, String title, String doi, String authors, int year, String journal, String status, String verificationNotes) {
        this.id.set(id);
        this.title.set(title);
        this.doi.set(doi);
        this.authors.set(authors);
        this.year.set(year);
        this.journal.set(journal);
        this.status.set(status != null ? status : "PENDING");
        this.verificationNotes.set(verificationNotes != null ? verificationNotes : "");
    }

    // JavaFX Observable Properties (for TableView and Form Binding)
    public IntegerProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty doiProperty() { return doi; }
    public StringProperty authorsProperty() { return authors; }
    public IntegerProperty yearProperty() { return year; }
    public StringProperty journalProperty() { return journal; }
    public StringProperty statusProperty() { return status; }
    public StringProperty verificationNotesProperty() { return verificationNotes; }

    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }

    public String getDoi() { return doi.get(); }
    public void setDoi(String doi) { this.doi.set(doi); }

    public String getAuthors() { return authors.get(); }
    public void setAuthors(String authors) { this.authors.set(authors); }

    public int getYear() { return year.get(); }
    public void setYear(int year) { this.year.set(year); }

    public String getJournal() { return journal.get(); }
    public void setJournal(String journal) { this.journal.set(journal); }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public String getVerificationNotes() { return verificationNotes.get(); }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes.set(verificationNotes); }

    @Override
    public String toString() {
        return "Publication{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", doi='" + getDoi() + '\'' +
                ", year=" + getYear() +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
