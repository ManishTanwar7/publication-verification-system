-- =====================================================================
-- Research Publication Verification & Management System - Database DDL
-- Compatible with MySQL Server 8.0+ and MySQL Workbench
-- =====================================================================

CREATE DATABASE IF NOT EXISTS research_pub_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE research_pub_db;

-- Drop table if re-running
DROP TABLE IF EXISTS publications;

CREATE TABLE publications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    doi VARCHAR(150) NOT NULL UNIQUE,
    authors VARCHAR(500) NOT NULL,
    publication_year INT NOT NULL,
    journal VARCHAR(300) NOT NULL,
    verification_status ENUM('PENDING', 'VERIFIED', 'REJECTED', 'DISCREPANCY') NOT NULL DEFAULT 'PENDING',
    verification_notes TEXT NULL,
    verified_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes for fast search and filtering
    INDEX idx_doi (doi),
    INDEX idx_status (verification_status),
    INDEX idx_year (publication_year),
    FULLTEXT INDEX idx_fulltext_search (title, authors, journal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample Seed Publications for Testing
INSERT INTO publications (title, doi, authors, publication_year, journal, verification_status, verification_notes, verified_at)
VALUES 
(
    'Attention Is All You Need',
    '10.48550/arXiv.1706.03762',
    'Ashish Vaswani, Noam Shazeer, Niki Parmar, Jakob Uszkoreit, Llion Jones, Aidan N. Gomez, Łukasz Kaiser, Illia Polosukhin',
    2017,
    'Advances in Neural Information Processing Systems (NeurIPS)',
    'VERIFIED',
    'Verified via Official Crossref Registry match.',
    NOW()
),
(
    'Deep Residual Learning for Image Recognition',
    '10.1109/CVPR.2016.90',
    'Kaiming He, Xiangyu Zhang, Shaoqing Ren, Jian Sun',
    2016,
    'IEEE Conference on Computer Vision and Pattern Recognition (CVPR)',
    'VERIFIED',
    'Official DOI verified.',
    NOW()
),
(
    'BERT: Pre-training of Deep Bidirectional Transformers for Language Understanding',
    '10.18653/v1/N19-1423',
    'Jacob Devlin, Ming-Wei Chang, Kenton Lee, Kristina Toutanova',
    2019,
    'NAACL-HLT',
    'PENDING',
    'Awaiting review against Crossref/Scholar records.',
    NULL
);
