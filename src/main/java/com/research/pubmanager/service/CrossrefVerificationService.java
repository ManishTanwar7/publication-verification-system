package com.research.pubmanager.service;

import com.research.pubmanager.model.Publication;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for querying the official Crossref Registry via REST API
 * to verify and fetch scholarly publication metadata by DOI.
 */
public class CrossrefVerificationService {

    private final HttpClient httpClient;

    public CrossrefVerificationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Cleans and formats a raw DOI string into standard format (e.g., 10.1109/CVPR.2016.90).
     */
    public String normalizeDoi(String rawDoi) {
        if (rawDoi == null) return "";
        return rawDoi.trim()
                .replaceAll("(?i)^https?://(dx\\.)?doi\\.org/", "")
                .replaceAll("(?i)^doi:", "")
                .trim();
    }

    /**
     * Queries Crossref REST API for a given DOI.
     *
     * @param rawDoi DOI string
     * @return Publication object populated with official metadata
     * @throws Exception if not found, network fails, or parse error
     */
    public Publication fetchByDoi(String rawDoi) throws Exception {
        String cleanDoi = normalizeDoi(rawDoi);
        if (cleanDoi.isEmpty()) {
            throw new IllegalArgumentException("DOI cannot be empty.");
        }

        String encodedDoi = URLEncoder.encode(cleanDoi, StandardCharsets.UTF_8);
        String endpoint = "https://api.crossref.org/works/" + encodedDoi;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("User-Agent", "ResearchPubVerificationSystem/1.0 (mailto:support@researchhub.org)")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            throw new IllegalArgumentException("DOI not found in the official Crossref registry: " + cleanDoi);
        } else if (response.statusCode() != 200) {
            throw new RuntimeException("Registry responded with HTTP error code " + response.statusCode());
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
        List<String> authorList = new ArrayList<>();
        JSONArray authorArray = message.optJSONArray("author");
        if (authorArray != null) {
            for (int i = 0; i < authorArray.length(); i++) {
                JSONObject a = authorArray.getJSONObject(i);
                String family = a.optString("family", "");
                String given = a.optString("given", "");
                String fullName = (given + " " + family).trim();
                if (!fullName.isEmpty()) {
                    authorList.add(fullName);
                }
            }
        }
        String authors = authorList.isEmpty() ? "Not specified" : String.join(", ", authorList);

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

        // 4. Journal / Container Title
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

        return new Publication(0, title, cleanDoi, authors, year, journal, "VERIFIED", "Fetched from Crossref Registry");
    }

    /**
     * Compares local publication data against official registry record.
     */
    public VerificationReport compareAndVerify(Publication local, Publication official) {
        List<String> discrepancies = new ArrayList<>();

        // Check Year
        if (local.getYear() > 0 && official.getYear() > 0 && local.getYear() != official.getYear()) {
            discrepancies.add(String.format("Year mismatch: Local entered %d vs Official registry %d", local.getYear(), official.getYear()));
        }

        // Check Title similarity
        double similarity = calculateStringSimilarity(local.getTitle().toLowerCase(), official.getTitle().toLowerCase());
        if (similarity < 0.70) {
            discrepancies.add(String.format("Title similarity is low (%.0f%% match).", similarity * 100));
        }

        boolean matched = discrepancies.isEmpty();
        String summary = matched
                ? "Perfect match found in official Crossref registry."
                : "Discrepancy detected:\n - " + String.join("\n - ", discrepancies);

        return new VerificationReport(matched, official, summary);
    }

    public static class VerificationReport {
        private final boolean matched;
        private final Publication officialRecord;
        private final String summary;

        public VerificationReport(boolean matched, Publication officialRecord, String summary) {
            this.matched = matched;
            this.officialRecord = officialRecord;
            this.summary = summary;
        }

        public boolean isMatched() { return matched; }
        public Publication getOfficialRecord() { return officialRecord; }
        public String getSummary() { return summary; }
    }

    private double calculateStringSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int longer = Math.max(s1.length(), s2.length());
        if (longer == 0) return 1.0;
        return (longer - computeLevenshteinDistance(s1, s2)) / (double) longer;
    }

    private int computeLevenshteinDistance(String a, String b) {
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
