package com.research.pubmanager.dao;

import com.research.pubmanager.model.Publication;
import java.sql.SQLException;
import java.util.List;

public interface PublicationDAO {

    /**
     * Retrieve all publications from the database.
     */
    List<Publication> findAll() throws SQLException;

    /**
     * Filter and search publications by text query and verification status.
     */
    List<Publication> search(String query, String status) throws SQLException;

    /**
     * Find a publication by its ID.
     */
    Publication findById(int id) throws SQLException;

    /**
     * Find a publication by DOI.
     */
    Publication findByDoi(String doi) throws SQLException;

    /**
     * Insert a new publication record.
     */
    boolean save(Publication publication) throws SQLException;

    /**
     * Update an existing publication's details.
     */
    boolean update(Publication publication) throws SQLException;

    /**
     * Update the verification status and notes of a publication.
     */
    boolean updateVerificationStatus(int id, String status, String notes) throws SQLException;

    /**
     * Delete a publication by ID.
     */
    boolean delete(int id) throws SQLException;

    /**
     * Count publications by status.
     */
    int countByStatus(String status) throws SQLException;
}
