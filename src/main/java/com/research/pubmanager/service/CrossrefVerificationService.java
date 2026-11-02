package com.research.pubmanager.service;

import com.research.pubmanager.entity.Publication;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrossrefVerificationService {

    private final HttpClient httpClient;

    public CrossrefVerificationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String normalizeDoi(String rawDoi) {
        if (rawDoi == null) return "";
        return rawDoi.trim()
                .replaceAll("(?i)^https?://(dx\\.)?doi\\.org/", "")
                .replaceAll("(?i)^doi:", "")
                .trim();
    }

    public Publication fetchFromCrossref(String rawDoi) throws Exception {
        String cleanDoi = normalizeDoi(rawDoi);
        if (cleanDoi.isEmpty()) {
            throw new IllegalArgumentException("DOI cannot be empty.");
        }

        String encodedDoi = URLEncoder.encode(cleanDoi, StandardCharsets.UTF_8);
        String endpoint = "https://api.crossref.org/works/" + encodedDoi;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("User-Agent", "ResearchPubVerificationWeb/1.0 (mailto:admin@university.edu)")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            throw new IllegalArgumentException("DOI not found in the official Crossref registry: " + cleanDoi);
        } else if (response.statusCode() != 200) {
            throw new RuntimeException("Crossref API returned HTTP status: " + response.statusCode());
        }

        JSONObject root = new JSONObject(response.body());
        JSONObject message = root.getJSONObject("message");

        // 1. Title
        String title = "Unknown Title";
        JSONArray titles = message.optJSONArray("title");
        if (titles != null && titles.length() > 0) {
            title = titles.getString(0).trim();
        }

        // 2. Authors
        List<String> authorsList = new ArrayList<>();
        JSONArray authorArray = message.optJSONArray("author");
        if (authorArray != null) {
            for (int i = 0; i < authorArray.length(); i++) {
                JSONObject a = authorArray.getJSONObject(i);
                String family = a.optString("family", "");
                String given = a.optString("given", "");
                String full = (given + " " + family).trim();
                if (!full.isEmpty()) {
                    authorsList.add(full);
                }
            }
        }
        String authors = authorsList.isEmpty() ? "Unknown Authors" : String.join(", ", authorsList);

        // 3. Year
        int year = 0;
        JSONObject published = message.optJSONObject("published");
        if (published == null) published = message.optJSONObject("published-print");
        if (published == null) published = message.optJSONObject("published-online");
        if (published == null) published = message.optJSONObject("created");

        if (published != null) {
            JSONArray dateParts = published.optJSONArray("date-parts");
            if (dateParts != null && dateParts.length() > 0) {
                JSONArray firstDate = dateParts.getJSONArray(0);
                if (firstDate.length() > 0) {
                    year = firstDate.getInt(0);
                }
            }
        }

        // 4. Journal / Conference
        String journal = "N/A";
        JSONArray containers = message.optJSONArray("container-title");
        if (containers != null && containers.length() > 0) {
            journal = containers.getString(0).trim();
        } else {
            String publisher = message.optString("publisher", "");
            if (!publisher.isEmpty()) {
                journal = publisher;
            }
        }

        return new Publication(title, cleanDoi, authors, year, journal, "VERIFIED", "Fetched & Verified from Crossref Registry");
    }

    public VerificationResult compareAndVerify(Publication local, Publication official) {
        List<String> discrepancies = new ArrayList<>();

        if (local.getPublicationYear() > 0 && official.getPublicationYear() > 0 && local.getPublicationYear() != official.getPublicationYear()) {
            discrepancies.add(String.format("Year mismatch: Local entered %d vs Official registry %d", local.getPublicationYear(), official.getPublicationYear()));
        }

        double similarity = calculateSimilarity(local.getTitle().toLowerCase(), official.getTitle().toLowerCase());
        if (similarity < 0.70) {
            discrepancies.add(String.format("Title similarity is low (%.0f%% match).", similarity * 100));
        }

        boolean matched = discrepancies.isEmpty();
        String summary = matched
                ? "Perfect match found in official Crossref registry."
                : "Discrepancy detected:\n- " + String.join("\n- ", discrepancies);

        return new VerificationResult(matched, official, summary);
    }

    public static class VerificationResult {
        private final boolean matched;
        private final Publication officialRecord;
        private final String summary;

        public VerificationResult(boolean matched, Publication officialRecord, String summary) {
            this.matched = matched;
            this.officialRecord = officialRecord;
            this.summary = summary;
        }

        public boolean isMatched() { return matched; }
        public Publication getOfficialRecord() { return officialRecord; }
        public String getSummary() { return summary; }
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int longer = Math.max(s1.length(), s2.length());
        if (longer == 0) return 1.0;
        return (longer - computeLevenshtein(s1, s2)) / (double) longer;
    }

    private int computeLevenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}
