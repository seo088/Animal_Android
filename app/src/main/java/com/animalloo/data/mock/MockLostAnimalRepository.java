package com.animalloo.data.mock;

import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.data.model.MatchGrade;
import com.animalloo.data.model.MatchResult;
import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.data.repository.LostAnimalRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MockLostAnimalRepository implements LostAnimalRepository {

    private final MockDataProvider dataProvider;
    private final MockAsyncHelper asyncHelper;
    private final Map<String, LostAnimalReport> submittedReports = new HashMap<>();

    public MockLostAnimalRepository() {
        dataProvider = MockDataProvider.getInstance();
        asyncHelper = MockAsyncHelper.getInstance();
    }

    @Override
    public void submitReport(LostAnimalReport report, RepositoryCallback<LostAnimalReport> callback) {
        asyncHelper.execute(() -> {
            String id = report.getId() != null ? report.getId() : "lost_" + UUID.randomUUID().toString().substring(0, 8);
            LostAnimalReport saved = new LostAnimalReport(
                    id,
                    report.getName(),
                    report.getSpecies(),
                    report.getBreed(),
                    report.getGender(),
                    report.getRegion(),
                    report.getLostDate(),
                    report.getFeatures(),
                    report.getContactInfo(),
                    report.getPhotoPath(),
                    System.currentTimeMillis()
            );
            submittedReports.put(id, saved);
            return saved;
        }, callback);
    }

    @Override
    public void findMatches(LostAnimalReport report, RepositoryCallback<List<MatchResult>> callback) {
        asyncHelper.execute(() -> {
            List<MatchResult> matches = new ArrayList<>();

            for (RescuedAnimal animal : dataProvider.getRescuedAnimals()) {
                int score = calculateSimilarity(report, animal);
                if (score < 40) {
                    continue;
                }

                MatchGrade grade;
                if (score >= 75) {
                    grade = MatchGrade.A;
                } else if (score >= 55) {
                    grade = MatchGrade.B;
                } else {
                    grade = MatchGrade.C;
                }

                matches.add(new MatchResult(
                        animal.getId(),
                        animal.getBreed(),
                        animal.getGender(),
                        animal.getRegion(),
                        animal.getRescuedDate(),
                        animal.getImageUrl(),
                        grade,
                        score
                ));
            }

            Collections.sort(matches, Comparator.comparingInt(MatchResult::getSimilarityPercent).reversed());
            if (matches.size() > 6) {
                return matches.subList(0, 6);
            }
            return matches;
        }, callback);
    }

    @Override
    public void getReportById(String id, RepositoryCallback<LostAnimalReport> callback) {
        asyncHelper.execute(() -> {
            LostAnimalReport report = submittedReports.get(id);
            if (report == null) {
                throw new IllegalStateException("분실 신고 정보를 찾을 수 없습니다.");
            }
            return report;
        }, callback);
    }

    private int calculateSimilarity(LostAnimalReport report, RescuedAnimal animal) {
        int score = 30;

        if (report.getBreed() != null && report.getBreed().equals(animal.getBreed())) {
            score += 30;
        }
        if (report.getGender() != null && report.getGender().equals(animal.getGender())) {
            score += 10;
        }
        if (report.getRegion() != null && animal.getRegion() != null
                && animal.getRegion().contains(report.getRegion().replace("전체", ""))) {
            score += 20;
        }
        if (report.getSpecies() != null && report.getSpecies().equals(animal.getSpecies())) {
            score += 10;
        }
        if (report.getFeatures() != null && animal.getFeatures() != null
                && hasCommonKeyword(report.getFeatures(), animal.getFeatures())) {
            score += 10;
        }

        return Math.min(score, 95);
    }

    private boolean hasCommonKeyword(String a, String b) {
        String[] keywordsA = a.split("[\\s,]+");
        for (String keyword : keywordsA) {
            if (keyword.length() >= 2 && b.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
