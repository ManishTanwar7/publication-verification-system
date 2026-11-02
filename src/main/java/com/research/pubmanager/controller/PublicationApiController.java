package com.research.pubmanager.controller;

import com.research.pubmanager.entity.Publication;
import com.research.pubmanager.repository.PublicationRepository;
import com.research.pubmanager.service.CrossrefVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/publications")
@CrossOrigin(origins = "*")
public class PublicationApiController {

    private final PublicationRepository repository;
    private final CrossrefVerificationService verificationService;

    public PublicationApiController(PublicationRepository repository, CrossrefVerificationService verificationService) {
        this.repository = repository;
        this.verificationService = verificationService;
    }

    @GetMapping
    public List<Publication> getPublications(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "ALL") String status) {
        return repository.searchPublications(query, status);
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        long total = repository.count();
        long verified = repository.countByVerificationStatus("VERIFIED");
        long pending = repository.countByVerificationStatus("PENDING");
        long rejected = repository.countByVerificationStatus("REJECTED") + repository.countByVerificationStatus("DISCREPANCY");

        metrics.put("total", total);
        metrics.put("verified", verified);
        metrics.put("pending", pending);
        metrics.put("rejected", rejected);
        return metrics;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Publication> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Publication publication) {
        if (publication.getDoi() == null || publication.getDoi().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "DOI is required."));
        }
        if (publication.getTitle() == null || publication.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title is required."));
        }

        publication.setDoi(verificationService.normalizeDoi(publication.getDoi()));

        if (repository.findByDoiIgnoreCase(publication.getDoi()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A publication with this DOI already exists."));
        }

        Publication saved = repository.save(publication);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Publication updated) {
        return repository.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setDoi(verificationService.normalizeDoi(updated.getDoi()));
            existing.setAuthors(updated.getAuthors());
            existing.setPublicationYear(updated.getPublicationYear());
            existing.setJournal(updated.getJournal());
            existing.setVerificationStatus(updated.getVerificationStatus());
            existing.setVerificationNotes(updated.getVerificationNotes());
            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/quick-import")
    public ResponseEntity<?> quickImport(@RequestParam String doi) {
        try {
            String cleanDoi = verificationService.normalizeDoi(doi);
            if (repository.findByDoiIgnoreCase(cleanDoi).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "This DOI is already registered in the system."));
            }

            Publication fetched = verificationService.fetchFromCrossref(cleanDoi);
            fetched.setVerifiedAt(LocalDateTime.now());
            Publication saved = repository.save(fetched);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyOnline(@PathVariable Long id) {
        return repository.findById(id).map(local -> {
            try {
                Publication official = verificationService.fetchFromCrossref(local.getDoi());
                CrossrefVerificationService.VerificationResult result = verificationService.compareAndVerify(local, official);

                String status = result.isMatched() ? "VERIFIED" : "DISCREPANCY";
                local.setVerificationStatus(status);
                local.setVerificationNotes(result.getSummary());
                local.setVerifiedAt(LocalDateTime.now());
                repository.save(local);

                Map<String, Object> response = new HashMap<>();
                response.put("matched", result.isMatched());
                response.put("summary", result.getSummary());
                response.put("officialRecord", official);
                response.put("updatedPublication", local);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Verification failed: " + e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false, defaultValue = "") String notes) {
        return repository.findById(id).map(pub -> {
            pub.setVerificationStatus(status.toUpperCase());
            if (!notes.isBlank()) {
                pub.setVerificationNotes(notes);
            }
            pub.setVerifiedAt(LocalDateTime.now());
            return ResponseEntity.ok(repository.save(pub));
        }).orElse(ResponseEntity.notFound().build());
    }
}
