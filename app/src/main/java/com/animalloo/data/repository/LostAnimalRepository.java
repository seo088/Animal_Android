package com.animalloo.data.repository;

import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.data.model.MatchResult;

import java.util.List;

public interface LostAnimalRepository {

    void submitReport(LostAnimalReport report, RepositoryCallback<LostAnimalReport> callback);

    void findMatches(LostAnimalReport report, RepositoryCallback<List<MatchResult>> callback);

    void getReportById(String id, RepositoryCallback<LostAnimalReport> callback);
}
