package com.research.pubmanager.dao;

import com.research.pubmanager.config.DatabaseConnection;
import com.research.pubmanager.model.Publication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationDAOImpl implements PublicationDAO {

    @Override
    public List<Publication> findAll() throws SQLException {
        List<Publication> list = new ArrayList<>();
        String sql = "SELECT id, title, doi, authors, publication_year, journal, verification_status, verification_notes FROM publications ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<Publication> search(String query, String status) throws SQLException {
        List<Publication> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, title, doi, authors, publication_year, journal, verification_status, verification_notes FROM publications WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND verification_status = ?");
            params.add(status.toUpperCase());
        }

        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(authors) LIKE ? OR LOWER(doi) LIKE ? OR LOWER(journal) LIKE ?)");
            String searchPattern = "%" + query.toLowerCase().trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        sql.append(" ORDER BY id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Publication findById(int id) throws SQLException {
        String sql = "SELECT id, title, doi, authors, publication_year, journal, verification_status, verification_notes FROM publications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Publication findByDoi(String doi) throws SQLException {
        String sql = "SELECT id, title, doi, authors, publication_year, journal, verification_status, verification_notes FROM publications WHERE doi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doi.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean save(Publication p) throws SQLException {
        String sql = "INSERT INTO publications (title, doi, authors, publication_year, journal, verification_status, verification_notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDoi());
            ps.setString(3, p.getAuthors());
            ps.setInt(4, p.getYear());
            ps.setString(5, p.getJournal());
            ps.setString(6, p.getStatus());
            ps.setString(7, p.getVerificationNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        p.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(Publication p) throws SQLException {
        String sql = "UPDATE publications SET title = ?, doi = ?, authors = ?, publication_year = ?, journal = ?, verification_status = ?, verification_notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDoi());
            ps.setString(3, p.getAuthors());
            ps.setInt(4, p.getYear());
            ps.setString(5, p.getJournal());
            ps.setString(6, p.getStatus());
            ps.setString(7, p.getVerificationNotes());
            ps.setInt(8, p.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateVerificationStatus(int id, String status, String notes) throws SQLException {
        String sql = "UPDATE publications SET verification_status = ?, verification_notes = ?, verified_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, notes);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM publications WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM publications WHERE verification_status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Publication mapRow(ResultSet rs) throws SQLException {
        return new Publication(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("doi"),
                rs.getString("authors"),
                rs.getInt("publication_year"),
                rs.getString("journal"),
                rs.getString("verification_status"),
                rs.getString("verification_notes")
        );
    }
}
