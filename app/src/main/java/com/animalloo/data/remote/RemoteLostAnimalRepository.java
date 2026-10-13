package com.animalloo.data.remote;

import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.data.model.MatchResult;
import com.animalloo.data.remote.dto.LostReportMatchRequest;
import com.animalloo.data.repository.LostAnimalRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * POST /lost-reports (multipart with the cached photo), POST /lost-reports/matches,
 * GET /lost-reports/{id}. The saved report's photoPath is the server image URL.
 */
public class RemoteLostAnimalRepository implements LostAnimalRepository {

    private static final MediaType TEXT = MediaType.get("text/plain; charset=utf-8");

    private final AnimalLooApi api;

    public RemoteLostAnimalRepository(AnimalLooApi api) {
        this.api = api;
    }

    @Override
    public void submitReport(LostAnimalReport report, RepositoryCallback<LostAnimalReport> callback) {
        Map<String, RequestBody> fields = new HashMap<>();
        putField(fields, "species", report.getSpecies());
        putField(fields, "name", report.getName());
        putField(fields, "breed", report.getBreed());
        putField(fields, "gender", report.getGender());
        putField(fields, "region", report.getRegion());
        putField(fields, "lostDate", report.getLostDate());
        putField(fields, "features", report.getFeatures());
        putField(fields, "contactInfo", report.getContactInfo());

        api.submitLostReport(fields, photoPart(report.getPhotoPath()))
                .enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void findMatches(LostAnimalReport report, RepositoryCallback<List<MatchResult>> callback) {
        api.findMatches(new LostReportMatchRequest(report)).enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void getReportById(String id, RepositoryCallback<LostAnimalReport> callback) {
        api.getLostReport(id).enqueue(ApiCallbacks.of(callback));
    }

    private static void putField(Map<String, RequestBody> fields, String name, String value) {
        if (value != null && !value.trim().isEmpty()) {
            fields.put(name, RequestBody.create(value.trim(), TEXT));
        }
    }

    private static MultipartBody.Part photoPart(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return null;
        }
        File file = new File(photoPath);
        if (!file.exists()) {
            return null;
        }
        RequestBody body = RequestBody.create(file, MediaType.get(imageMimeType(file.getName())));
        return MultipartBody.Part.createFormData("photo", file.getName(), body);
    }

    private static String imageMimeType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".heic")) {
            return "image/heic";
        }
        return "image/jpeg";
    }
}
