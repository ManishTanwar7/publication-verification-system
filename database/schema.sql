-- =====================================================================
-- Research Publication Verification & Management System - Database DDL
-- Compatible with MySQL Server 8.0+ and MySQL Workbench
-- Contains 52 Pre-seeded Verified, Rejected, and Pending Research Records
-- =====================================================================

CREATE DATABASE IF NOT EXISTS research_pub_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE research_pub_db;

DROP TABLE IF EXISTS publications;

CREATE TABLE publications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(600) NOT NULL,
    doi VARCHAR(150) NULL,
    authors VARCHAR(600) NOT NULL,
    publication_year INT NOT NULL,
    journal VARCHAR(350) NOT NULL,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    verification_notes TEXT NULL,
    citations_count INT NOT NULL DEFAULT 0,
    verified_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_doi (doi),
    INDEX idx_status (verification_status),
    INDEX idx_year (publication_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed 52 Comprehensive Research Publications
INSERT INTO publications (id, title, doi, authors, publication_year, journal, verification_status, verification_notes, citations_count, verified_at)
VALUES
(1, 'Attention Is All You Need', '10.48550/arXiv.1706.03762', 'Ashish Vaswani, Noam Shazeer, Niki Parmar, Jakob Uszkoreit, Llion Jones', 2017, 'Advances in Neural Information Processing Systems (NeurIPS)', 'VERIFIED', 'Official landmark transformer architecture paper verified in literature.', 142000, NOW()),
(2, 'Deep Residual Learning for Image Recognition', '10.1109/CVPR.2016.90', 'Kaiming He, Xiangyu Zhang, Shaoqing Ren, Jian Sun', 2016, 'IEEE Conference on Computer Vision and Pattern Recognition (CVPR)', 'VERIFIED', 'Official ResNet landmark paper verified in IEEE.', 185000, NOW()),
(3, 'BERT: Pre-training of Deep Bidirectional Transformers for Language Understanding', '10.18653/v1/N19-1423', 'Jacob Devlin, Ming-Wei Chang, Kenton Lee, Kristina Toutanova', 2019, 'NAACL-HLT', 'VERIFIED', 'Official paper verified in NAACL registry.', 89000, NOW()),
(4, 'Adam: A Method for Stochastic Optimization', '10.48550/arXiv.1412.6980', 'Diederik P. Kingma, Jimmy Ba', 2015, 'International Conference on Learning Representations (ICLR)', 'VERIFIED', 'Landmark optimization paper verified.', 165000, NOW()),
(5, 'Generative Adversarial Nets', '10.1145/3422622', 'Ian Goodfellow, Jean Pouget-Abadie, Mehdi Mirza, Bing Xu, David Warde-Farley', 2014, 'Advances in Neural Information Processing Systems (NeurIPS)', 'VERIFIED', 'Original GAN architecture paper verified.', 68000, NOW()),
(6, 'Mastering the Game of Go with Deep Neural Networks and Tree Search', '10.1038/nature16961', 'David Silver, Aja Huang, Chris J. Maddison, Arthur Guez', 2016, 'Nature', 'VERIFIED', 'Landmark AlphaGo paper verified in Nature.', 15400, NOW()),
(7, 'ImageNet Classification with Deep Convolutional Neural Networks', '10.1145/3065386', 'Alex Krizhevsky, Ilya Sutskever, Geoffrey E. Hinton', 2012, 'Communications of the ACM', 'VERIFIED', 'Landmark AlexNet convolutional network paper verified.', 148000, NOW()),
(8, 'Language Models are Few-Shot Learners', '10.48550/arXiv.2005.14165', 'Tom B. Brown, Benjamin Mann, Nick Ryder, Melanie Subbiah, Jared Kaplan', 2020, 'NeurIPS', 'VERIFIED', 'Landmark GPT-3 architecture paper verified.', 34500, NOW()),
(9, 'YOLO9000: Better, Faster, Stronger', '10.1109/CVPR.2017.690', 'Joseph Redmon, Ali Farhadi', 2017, 'IEEE CVPR', 'VERIFIED', 'Real-time object detection verified.', 24500, NOW()),
(10, 'ResNeXt: Aggregated Residual Transformations for Deep Neural Networks', '10.1109/CVPR.2017.634', 'Saining Xie, Ross Girshick, Piotr Dollar, Zhuowen Tu, Kaiming He', 2017, 'IEEE CVPR', 'VERIFIED', 'Multi-branch architecture verified.', 18200, NOW()),
(11, 'An Analysis of Fault Injection in Modern Distributed Microservices', '10.1109/TSE.2026.342110', 'Manish Tanwar, Sarah Jenkins, David Kumar', 2026, 'IEEE Transactions on Software Engineering', 'VERIFIED', 'Original experimental study on cloud resiliency.', 14, NOW()),
(12, 'Deep Residual Learning for Image Recognition - 2026 Edition', '10.1109/FAKE.2026.01', 'M. Sharma, A. Gupta', 2026, 'International Conference on Artificial Intelligence', 'REJECTED', '[DUPLICATE DETECTED] (96% Match)! Exact re-submission of He et al. (CVPR 2016, 185k citations).', 0, NOW()),
(13, 'BERT: Deep Transformer Model for Language Processing', '10.18653/FAKE.2026.02', 'R. Verma, K. Patel', 2026, 'Journal of Computer Applications', 'REJECTED', '[DUPLICATE DETECTED] (88% Match)! Paraphrased submission of Devlin et al. (NAACL 2019).', 0, NOW()),
(14, 'Secure Multiparty Computation for Decentralized Medical Records', '10.1109/TDSC.2025.339820', 'Elena Rostova, Marcus Chen, Priya Nair', 2025, 'IEEE Transactions on Dependable and Secure Computing', 'VERIFIED', 'Cryptographic protocol verified in IEEE TDSC.', 86, NOW()),
(15, 'Spanner: Google''s Globally-Distributed Database', '10.1145/2491245', 'James C. Corbett, Jeffrey Dean, Michael Epstein, Andrew Fikes', 2013, 'ACM Transactions on Computer Systems (TOCS)', 'VERIFIED', 'Official landmark distributed database paper.', 5200, NOW()),
(16, 'In Search of an Understandable Consensus Algorithm (Raft)', '10.5555/2643634.2643666', 'Diego Ongaro, John Ousterhout', 2014, 'USENIX Annual Technical Conference (ATC)', 'VERIFIED', 'Landmark distributed consensus paper verified.', 6100, NOW()),
(17, 'Attention Mechanisms in Deep Learning Networks', '10.48550/FAKE.2026.03', 'V. Reddy, S. Nair', 2026, 'Open Preprint Service', 'REJECTED', '[DUPLICATE DETECTED] (78% Match)! High similarity to Vaswani et al. (NeurIPS 2017).', 0, NOW()),
(18, 'Real-Time Bug Detection in Java Applications Using Bytecode Instrumentation', '10.1145/3611643.3616280', 'Manish Tanwar, Neha Singhal, Rajesh Malhotra', 2026, 'ACM Symposium on the Foundations of Software Engineering (FSE)', 'VERIFIED', 'Original research paper on Java bytecode analysis.', 8, NOW()),
(19, 'Zero-Knowledge Proofs for Privacy-Preserving Layer-2 Blockchains', '10.1109/SP.2025.10129', 'Alexander Wright, Chen Wei, Lucas Tremblay', 2025, 'IEEE Symposium on Security and Privacy (S&P)', 'VERIFIED', 'Cryptographic zk-SNARK verification paper.', 120, NOW()),
(20, 'Edge Computing Architecture for Smart Grid Sensor Networks', '10.1109/JIOT.2024.331290', 'K. Srinivasan, Fatima Al-Zahra, John Miller', 2024, 'IEEE Internet of Things Journal', 'VERIFIED', 'IoT telemetry and edge routing paper verified.', 430, NOW()),
(21, 'Dynamic Voltage and Frequency Scaling in Multi-Core Mobile Processors', '10.1109/TVLSI.2023.328901', 'Hao Zhang, Takashi Tanaka, Brian O''Connor', 2023, 'IEEE Transactions on VLSI Systems', 'VERIFIED', 'VLSI hardware architecture paper verified.', 210, NOW()),
(22, 'Large Language Models are Few Shot Learners Re-evaluated', '10.48550/FAKE.2026.04', 'S. Chawla, P. Kulkarni', 2026, 'AI Research Forum', 'REJECTED', '[DUPLICATE DETECTED] (86% Match)! Near duplicate of Brown et al. (GPT-3, NeurIPS 2020).', 0, NOW()),
(23, 'Automated Vulnerability Scanning in Containerized CI/CD Pipelines', '10.5555/3642100.3642125', 'Rachel Foster, Kevin Zhao, Liam Henderson', 2026, 'USENIX Security Symposium', 'VERIFIED', 'DevSecOps vulnerability scanning framework verified.', 28, NOW()),
(24, 'Federated Learning for Cross-Silo Healthcare Analytics Without Patient Data Exposure', '10.1038/s41746-025-01290-x', 'Siddharth Menon, Angela Rossi, Christian Weber', 2025, 'Nature Digital Medicine', 'VERIFIED', 'Clinical federated learning trial paper.', 380, NOW()),
(25, 'Quantum Key Distribution Protocols Under High-Noise Atmospheric Turbulence', '10.1103/PhysRevApplied.21.044012', 'Niels Lindholm, Sophia Morozova, Arthur Pendelton', 2024, 'Physical Review Applied', 'VERIFIED', 'Quantum cryptography protocol verified in APS.', 95, NOW()),
(26, 'Microservice Latency Optimization via Reinforcement Learning Driven Thread Pools', '10.1145/3639100.3639140', 'Vikram Aditya, Daniel Becker, Fiona Gallagher', 2026, 'ACM SIGMETRICS Performance Evaluation Review', 'VERIFIED', 'Cloud computing performance modeling paper.', 12, NOW()),
(27, 'Generative Adversarial Networks: A Duplicate Survey', '10.1145/FAKE.2026.05', 'D. Ghosh, B. Banerjee', 2026, 'Computer Science Review Notes', 'REJECTED', '[DUPLICATE DETECTED] (82% Match)! Significant title overlap with Goodfellow et al. (2014).', 0, NOW()),
(28, 'Self-Supervised Vision Transformers for Cancer Pathology Whole-Slide Imaging', '10.1016/j.media.2025.103190', 'Guillaume Mercier, Sunita Rao, Patrick Evans', 2025, 'Medical Image Analysis', 'VERIFIED', 'Medical computational pathology paper verified.', 290, NOW()),
(29, 'High-Throughput Packet Processing Using eBPF and XDP in Linux Kernels', '10.1109/TNET.2024.338210', 'Mads Mikkelsen, Alexei Smirnov, Robert Lang', 2024, 'IEEE/ACM Transactions on Networking', 'VERIFIED', 'Linux kernel programmable networking paper verified.', 175, NOW()),
(30, 'Deep Learning for Autonomous Driving: Perception, Localization and Planning', '10.1109/TITS.2023.3276100', 'Wei Huang, Jessica Campbell, Klaus Fischer', 2023, 'IEEE Transactions on Intelligent Transportation Systems', 'VERIFIED', 'Comprehensive autonomous vehicle systems survey.', 1250, NOW()),
(31, 'Automated Refactoring of Legacy Monoliths to Event-Driven Microservices', '10.1109/MS.2026.341200', 'Manish Tanwar, Anita Desai', 2026, 'IEEE Software', 'PENDING', 'Currently under peer review for IEEE Software.', 3, NOW()),
(32, 'Detecting Phantom Read Anomalies in Highly-Concurrent SQL Isolation Levels', '10.1145/3650100.3650150', 'Girish Kulkarni, Benjamin Stone, Li Na', 2025, 'ACM SIGMOD International Conference on Management of Data', 'VERIFIED', 'Database concurrency control paper verified in SIGMOD.', 84, NOW()),
(33, 'Adam Optimizer: Re-implementation and Benchmarks in 2026', '10.48550/FAKE.2026.06', 'K. Raman, T. Joshi', 2026, 'Open Source Journal', 'REJECTED', '[DUPLICATE DETECTED] (91% Match)! Re-packaging of Kingma & Ba (ICLR 2015, 165k citations).', 0, NOW()),
(34, 'Homomorphic Encryption for Outsourced Cloud Database Computations', '10.1145/3627106.3627199', 'Carlos Mendez, Alistair Vance, Zoya Petrov', 2024, 'ACM Conference on Computer and Communications Security (CCS)', 'VERIFIED', 'Cloud cryptographic privacy paper verified in CCS.', 540, NOW()),
(35, 'Energy-Harvesting Routing Protocols in Underwater Wireless Sensor Networks', '10.1109/JSEN.2025.334190', 'Tariq Mansoor, Deborah King, Eric Thorne', 2025, 'IEEE Sensors Journal', 'VERIFIED', 'Acoustic underwater sensor network paper verified.', 115, NOW()),
(36, 'Graph Neural Networks for Drug Target Interaction Prediction', '10.1093/bioinformatics/btae120', 'Liang Shen, Rebecca Taylor, Oliver Schmidt', 2024, 'Bioinformatics (Oxford Academic)', 'VERIFIED', 'Bioinformatics molecular interaction paper verified.', 880, NOW()),
(37, 'Explainable AI for Financial Credit Risk Assessment Using SHAP and LIME', '10.1016/j.eswa.2025.121900', 'Aditi Saxena, Francois Dubois, Kenneth Cole', 2025, 'Expert Systems with Applications', 'VERIFIED', 'Interpretability in financial modeling paper verified.', 310, NOW()),
(38, 'YOLO: Real-Time Object Detection Framework', '10.1109/FAKE.2026.07', 'A. Tiwari, S. Mishra', 2026, 'Global Journal of Computer Vision', 'REJECTED', '[DUPLICATE DETECTED] (90% Match)! Direct clone of Joseph Redmon''s YOLO paper.', 0, NOW()),
(39, 'Energy-Efficient Task Offloading in 6G Heterogeneous Vehicular Edge Networks', '10.1109/TWC.2026.340112', 'Hassan Al-Nuaimi, Meera Pillai, Chen Long', 2026, 'IEEE Transactions on Wireless Communications', 'VERIFIED', 'Next-generation vehicular edge computing paper verified.', 19, NOW()),
(40, 'Formal Verification of Smart Contracts Using Symbolic Execution and SMT Solvers', '10.1145/3649800.3649850', 'Domenico Rossi, Katherine Bell, Simon Fraser', 2025, 'ACM SIGPLAN OOPSLA', 'VERIFIED', 'Ethereum bytecode symbolic verification paper.', 160, NOW()),
(41, 'Multi-Modal Fusion of EEG and fMRI Signals for Neurodegenerative Diagnosis', '10.1109/TBME.2026.341908', 'Bhavna Swaminathan, Julian Richter, Chloe Dupont', 2026, 'IEEE Transactions on Biomedical Engineering', 'VERIFIED', 'Biomedical neural signal processing paper verified.', 42, NOW()),
(42, 'Scalable Semantic Search Over Trillion-Token Codebases', '10.1145/3661100.3661150', 'Manish Tanwar, Harish Raghavan', 2026, 'ACM International Conference on Software Engineering (ICSE)', 'PENDING', 'Under technical review at ICSE 2026.', 8, NOW()),
(43, 'Mitigation of Prompt Injection Attacks in Production LLM Agents', '10.1109/SP.2026.10490', 'Zachary Hayes, Linette Gomez, Farhan Qureshi', 2026, 'IEEE Symposium on Security and Privacy (S&P)', 'VERIFIED', 'AI agent security and firewalling paper verified.', 65, NOW()),
(44, 'High-Performance Graph Analytics on NVMe Solid-State Drives', '10.14778/3641200.3641230', 'Tobias Lindqvist, Sun-Woo Park, Martin Vance', 2025, 'Proceedings of the VLDB Endowment', 'VERIFIED', 'Storage engine graph processing paper verified in VLDB.', 76, NOW()),
(45, 'Mastering Go Game via Reinforcement Learning - Resubmission', '10.1038/FAKE.2026.08', 'N. Kapoor, V. Sethi', 2026, 'Computational Game Letters', 'REJECTED', '[DUPLICATE DETECTED] (95% Match)! Re-submission of DeepMind''s AlphaGo landmark paper.', 0, NOW()),
(46, 'Privacy-Preserving Contact Tracing via Cryptographic Handshakes', '10.1145/3544548', 'Carmela Troncoso, Mathias Payer, Jean-Pierre Hubaux', 2023, 'Communications of the ACM', 'VERIFIED', 'DP-3T cryptographic decentralized tracing paper verified.', 820, NOW()),
(47, 'Real-Time 3D Gaussian Splatting for High-Fidelity Radiance Field Reconstruction', '10.1145/3592433', 'Bernhard Kerbl, Georgios Kopanas, Thomas Leimkuehler, George Drettakis', 2024, 'ACM Transactions on Graphics (SIGGRAPH)', 'VERIFIED', 'Landmark 3D reconstruction computer graphics paper verified.', 1420, NOW()),
(48, 'Compiler Optimizations for Sparse Tensor Computations on GPU Accelerators', '10.1109/CGO.2026.341100', 'Rohan Sengupta, Laura Bennett', 2026, 'IEEE/ACM International Symposium on Code Generation and Optimization', 'PENDING', 'Under final peer review at CGO 2026.', 11, NOW()),
(49, 'Zero-Day Malware Detection Using System Call Graph Embeddings', '10.1016/j.cose.2025.103980', 'Sergei Romanov, Ayesha Siddiqui, Gregory House', 2025, 'Computers & Security', 'VERIFIED', 'Behavioral host intrusion detection paper verified.', 190, NOW()),
(50, 'Transformer-Based Code Translation Across Strongly-Typed Programming Languages', '10.1109/TR.2026.342990', 'Manish Tanwar, Christopher Nolan, Pooja Bhatt', 2026, 'IEEE Transactions on Reliability', 'VERIFIED', 'Code translation verification and correctness paper.', 24, NOW()),
(51, 'Adaptive Congestion Control for Low-Latency Ultra-Reliable 5G Slices', '10.1109/JSAC.2026.341820', 'Faisal Ibrahim, Svetlana Ivanova, Neil Armstrong', 2026, 'IEEE Journal on Selected Areas in Communications', 'VERIFIED', 'URLLC cellular scheduling paper verified in JSAC.', 37, NOW()),
(52, 'Spanner Database Architecture Revisited', '10.1145/FAKE.2026.09', 'G. Narang, J. Chadha', 2026, 'National Database Conference Proceedings', 'REJECTED', '[DUPLICATE DETECTED] (89% Match)! Duplicate of Google Spanner (Corbett et al., 2013).', 0, NOW());

SELECT COUNT(*) AS total_records FROM publications;
