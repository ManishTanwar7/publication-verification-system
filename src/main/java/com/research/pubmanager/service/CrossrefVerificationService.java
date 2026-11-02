package com.research.pubmanager.service;

import com.research.pubmanager.entity.Publication;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrossrefVerificationService {

    public CrossrefVerificationService() {}

    public String normalizeDoi(String rawDoi) {
        if (rawDoi == null) return "";
        return rawDoi.trim()
                .replaceAll("(?i)^https?://(dx\\.)?doi\\.org/", "")
                .replaceAll("(?i)^doi:", "")
                .trim();
    }

    /**
     * Searches global academic literature (Crossref / Google Scholar indexed records)
     * by paper title to check whether a publication already exists in the academic literature.
     */
    public PriorArtCheckResult checkExistingPublications(String titleToCheck) throws Exception {
        if (titleToCheck == null || titleToCheck.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }

        String encodedTitle = URLEncoder.encode(titleToCheck.trim(), StandardCharsets.UTF_8);
        String endpoint = "https://api.crossref.org/works?query.title=" + encodedTitle + "&rows=5";

        URL url = URI.create(endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "ResearchPubVerification/1.0 (mailto:admin@university.edu)");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("Academic registry search failed with HTTP status: " + status);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        JSONObject root = new JSONObject(response.toString());
        JSONObject message = root.getJSONObject("message");
        JSONArray items = message.optJSONArray("items");

        List<ExistingPaperMatch> matches = new ArrayList<>();
        double highestSimilarity = 0.0;
        ExistingPaperMatch topMatch = null;

        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                // Title
                String matchTitle = "Unknown Title";
                JSONArray titles = item.optJSONArray("title");
                if (titles != null && titles.length() > 0) {
                    matchTitle = titles.getString(0).trim();
                }

                // DOI
                String matchDoi = item.optString("DOI", "N/A");

                // Authors
                List<String> authorsList = new ArrayList<>();
                JSONArray authorArray = item.optJSONArray("author");
                if (authorArray != null) {
                    for (int j = 0; j < authorArray.length(); j++) {
                        JSONObject a = authorArray.getJSONObject(j);
                        String family = a.optString("family", "");
                        String given = a.optString("given", "");
                        String full = (given + " " + family).trim();
                        if (!full.isEmpty()) {
                            authorsList.add(full);
                        }
                    }
                }
                String authors = authorsList.isEmpty() ? "Unknown Authors" : String.join(", ", authorsList);

                // Year
                int year = 0;
                JSONObject published = item.optJSONObject("published");
                if (published == null) published = item.optJSONObject("created");
                if (published != null) {
                    JSONArray dateParts = published.optJSONArray("date-parts");
                    if (dateParts != null && dateParts.length() > 0) {
                        year = dateParts.getJSONArray(0).optInt(0, 0);
                    }
                }

                // Journal / Publisher
                String journal = "N/A";
                JSONArray containers = item.optJSONArray("container-title");
                if (containers != null && containers.length() > 0) {
                    journal = containers.getString(0).trim();
                } else {
                    journal = item.optString("publisher", "Scholarly Publisher");
                }

                // Calculate Similarity with query
                double sim = calculateSimilarity(titleToCheck.toLowerCase(), matchTitle.toLowerCase());
                ExistingPaperMatch match = new ExistingPaperMatch(matchTitle, matchDoi, authors, year, journal, Math.round(sim * 100));
                matches.add(match);

                if (sim > highestSimilarity) {
                    highestSimilarity = sim;
                    topMatch = match;
                }
            }
        }

        // Evaluate if paper already exists
        boolean alreadyExists = highestSimilarity >= 0.70;
        String verdict;
        if (highestSimilarity >= 0.90) {
            verdict = String.format("EXACT DUPLICATE FOUND (%d%% match). A paper titled \"%s\" is already published in %s (%d)!",
                    Math.round(highestSimilarity * 100),
                    topMatch != null ? topMatch.getTitle() : "",
                    topMatch != null ? topMatch.getJournal() : "",
                    topMatch != null ? topMatch.getYear() : 0);
        } else if (highestSimilarity >= 0.70) {
            verdict = String.format("HIGH SIMILARITY DETECTED (%d%% match). Similar research already exists by %s (%d).",
                    Math.round(highestSimilarity * 100),
                    topMatch != null ? topMatch.getAuthors() : "",
                    topMatch != null ? topMatch.getYear() : 0);
        } else if (highestSimilarity >= 0.40) {
            verdict = String.format("PARTIAL TOPIC OVERLAP (%d%% match). Related literature exists, but paper title appears distinct.",
                    Math.round(highestSimilarity * 100));
        } else {
            verdict = "ORIGINAL RESEARCH: No matching existing publication found in global scholarly databases. Eligible for publishing!";
        }

        return new PriorArtCheckResult(titleToCheck, alreadyExists, Math.round(highestSimilarity * 100), verdict, topMatch, matches);
    }

    public static class PriorArtCheckResult {
        private final String queryTitle;
        private final boolean alreadyExists;
        private final long maxSimilarityPercentage;
        private final String verdict;
        private final ExistingPaperMatch topMatch;
        private final List<ExistingPaperMatch> matches;

        public PriorArtCheckResult(String queryTitle, boolean alreadyExists, long maxSimilarityPercentage, String verdict, ExistingPaperMatch topMatch, List<ExistingPaperMatch> matches) {
            this.queryTitle = queryTitle;
            this.alreadyExists = alreadyExists;
            this.maxSimilarityPercentage = maxSimilarityPercentage;
            this.verdict = verdict;
            this.topMatch = topMatch;
            this.matches = matches;
        }

        public String getQueryTitle() { return queryTitle; }
        public boolean isAlreadyExists() { return alreadyExists; }
        public long getMaxSimilarityPercentage() { return maxSimilarityPercentage; }
        public String getVerdict() { return verdict; }
        public ExistingPaperMatch getTopMatch() { return topMatch; }
        public List<ExistingPaperMatch> getMatches() { return matches; }
    }

    public static class ExistingPaperMatch {
        private final String title;
        private final String doi;
        private final String authors;
        private final int year;
        private final String journal;
        private final long similarityPercentage;

        public ExistingPaperMatch(String title, String doi, String authors, int year, String journal, long similarityPercentage) {
            this.title = title;
            this.doi = doi;
            this.authors = authors;
            this.year = year;
            this.journal = journal;
            this.similarityPercentage = similarityPercentage;
        }

        public String getTitle() { return title; }
        public String getDoi() { return doi; }
        public String getAuthors() { return authors; }
        public int getYear() { return year; }
        public String getJournal() { return journal; }
        public long getSimilarityPercentage() { return similarityPercentage; }
    }

    public Publication fetchFromCrossref(String rawDoi) throws Exception {
        String cleanDoi = normalizeDoi(rawDoi);
        if (cleanDoi.isEmpty()) {
            throw new IllegalArgumentException("DOI cannot be empty.");
        }

        String encodedDoi = URLEncoder.encode(cleanDoi, StandardCharsets.UTF_8);
        String endpoint = "https://api.crossref.org/works/" + encodedDoi;

        URL url = URI.create(endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "ResearchPubVerification/1.0 (mailto:admin@university.edu)");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        int status = conn.getResponseCode();
        if (status == 404) {
            throw new IllegalArgumentException("DOI not found in the official registry: " + cleanDoi);
        } else if (status != 200) {
            throw new RuntimeException("Crossref API returned HTTP status: " + status);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        JSONObject root = new JSONObject(response.toString());
        JSONObject message = root.getJSONObject("message");

        String title = "Unknown Title";
        JSONArray titles = message.optJSONArray("title");
        if (titles != null && titles.length() > 0) {
            title = titles.getString(0).trim();
        }

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

        int year = 0;
        JSONObject published = message.optJSONObject("published");
        if (published == null) published = message.optJSONObject("created");
        if (published != null) {
            JSONArray dateParts = published.optJSONArray("date-parts");
            if (dateParts != null && dateParts.length() > 0) {
                year = dateParts.getJSONArray(0).optInt(0, 0);
            }
        }

        String journal = "N/A";
        JSONArray containers = message.optJSONArray("container-title");
        if (containers != null && containers.length() > 0) {
            journal = containers.getString(0).trim();
        } else {
            journal = message.optString("publisher", "Publisher");
        }

        return new Publication(title, cleanDoi, authors, year, journal, "VERIFIED", "Fetched & Verified from Official Registry");
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
