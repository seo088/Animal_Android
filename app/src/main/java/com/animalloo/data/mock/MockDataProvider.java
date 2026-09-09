package com.animalloo.data.mock;

import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.AlertType;
import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.Facility;
import com.animalloo.data.model.FacilityCategory;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.model.Hospital;
import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.data.model.Symptom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central mock data source for all MockRepository implementations.
 * 가상 데이터임을 전제로 한국어 시연용 샘플을 제공합니다.
 */
public final class MockDataProvider {

    private static MockDataProvider instance;

    private final List<Facility> facilities;
    private final List<Hospital> hospitals;
    private final List<RescuedAnimal> rescuedAnimals;
    private final List<AlertNotification> alerts;
    private final List<Symptom> symptoms;
    private final List<DiagnosisResult> diagnosisResults;
    private final HomeStats homeStats;
    private final Map<String, List<String>> symptomToDiseaseMap;

    private MockDataProvider() {
        facilities = createFacilities();
        hospitals = createHospitals();
        rescuedAnimals = createRescuedAnimals();
        alerts = createAlerts();
        symptoms = createSymptoms();
        diagnosisResults = createDiagnosisResults();
        symptomToDiseaseMap = createSymptomToDiseaseMap();
        homeStats = new HomeStats(128, 7, 23, facilities.size());
    }

    public static synchronized MockDataProvider getInstance() {
        if (instance == null) {
            instance = new MockDataProvider();
        }
        return instance;
    }

    public List<Facility> getFacilities() {
        return Collections.unmodifiableList(facilities);
    }

    public List<Hospital> getHospitals() {
        return Collections.unmodifiableList(hospitals);
    }

    public List<RescuedAnimal> getRescuedAnimals() {
        return Collections.unmodifiableList(rescuedAnimals);
    }

    public List<AlertNotification> getAlerts() {
        return Collections.unmodifiableList(alerts);
    }

    public List<Symptom> getSymptoms() {
        return Collections.unmodifiableList(symptoms);
    }

    public List<DiagnosisResult> getDiagnosisResults() {
        return Collections.unmodifiableList(diagnosisResults);
    }

    public HomeStats getHomeStats() {
        return homeStats;
    }

    public Map<String, List<String>> getSymptomToDiseaseMap() {
        return Collections.unmodifiableMap(symptomToDiseaseMap);
    }

    public Facility findFacilityById(String id) {
        for (Facility facility : facilities) {
            if (facility.getId().equals(id)) {
                return facility;
            }
        }
        return null;
    }

    public Hospital findHospitalById(String id) {
        for (Hospital hospital : hospitals) {
            if (hospital.getId().equals(id)) {
                return hospital;
            }
        }
        return null;
    }

    public RescuedAnimal findRescuedAnimalById(String id) {
        for (RescuedAnimal animal : rescuedAnimals) {
            if (animal.getId().equals(id)) {
                return animal;
            }
        }
        return null;
    }

    public AlertNotification findAlertById(String id) {
        for (AlertNotification alert : alerts) {
            if (alert.getId().equals(id)) {
                return alert;
            }
        }
        return null;
    }

    public DiagnosisResult findDiagnosisById(String id) {
        for (DiagnosisResult result : diagnosisResults) {
            if (result.getId().equals(id)) {
                return result;
            }
        }
        return null;
    }

    private List<Facility> createFacilities() {
        List<Facility> list = new ArrayList<>();
        list.add(createFacility("fac_001", "행복동물병원(가상)", FacilityCategory.HOSPITAL,
                "서울 송파구 올림픽로 120", "02-1234-5678", "09:00-20:00",
                37.5145, 127.1060, 0.8, "https://picsum.photos/seed/fac001/400/300",
                "24시간 응급 진료 가능(가상)", false, "개", "전체", true, false, true));
        list.add(createFacility("fac_002", "서울반려동물센터(가상)", FacilityCategory.HOSPITAL,
                "서울 강남구 테헤란로 88", "02-2345-6789", "10:00-19:00",
                37.5012, 127.0396, 1.2, "https://picsum.photos/seed/fac002/400/300",
                "정형외과·내과 전문(가상)", false, "개", "전체", true, false, true));
        list.add(createFacility("fac_003", "사랑약국(가상)", FacilityCategory.PHARMACY,
                "서울 마포구 월드컵로 45", "02-3456-7890", "09:00-21:00",
                37.5663, 126.9019, 2.1, "https://picsum.photos/seed/fac003/400/300",
                "동물용 의약품 취급(가상)", false, "개", "전체", true, false, false));
        list.add(createFacility("fac_004", "희망동물보호소(가상)", FacilityCategory.SHELTER,
                "경기 성남시 분당구 정자로 55", "031-123-4567", "10:00-18:00",
                37.3595, 127.1052, 5.4, "https://picsum.photos/seed/fac004/400/300",
                "유기동물 보호·입양 상담(가상)", false, "개", "전체", true, false, true));
        list.add(createFacility("fac_005", "멍멍식당(가상)", FacilityCategory.RESTAURANT,
                "서울 송파구 백제고분로 30", "02-4567-8901", "11:00-22:00",
                37.5088, 127.1002, 1.0, "https://picsum.photos/seed/fac005/400/300",
                "반려견 동반 식사 가능(가상)", true, "개", "소형 (5kg 이하)", true, false, true));
        list.add(createFacility("fac_006", "냥냥카페(가상)", FacilityCategory.CAFE,
                "서울 강남구 역삼로 101", "02-5678-9012", "10:00-22:00",
                37.4998, 127.0364, 1.5, "https://picsum.photos/seed/fac006/400/300",
                "소형견·고양이 동반 가능(가상)", true, "전체", "소형 (5kg 이하)", true, true, true));
        list.add(createFacility("fac_007", "펫프렌들리호텔(가상)", FacilityCategory.HOTEL,
                "경기 수원시 영통구 광교로 200", "031-234-5678", "24시간",
                37.2892, 127.0473, 8.2, "https://picsum.photos/seed/fac007/400/300",
                "반려동물 동반 숙박(가상)", true, "개", "중형 (5~15kg)", true, false, true));
        list.add(createFacility("fac_008", "반려나들길공원(가상)", FacilityCategory.TOURISM,
                "서울 마포구 하늘공원로 86", "02-6789-0123", "06:00-22:00",
                37.5695, 126.8760, 3.5, "https://picsum.photos/seed/fac008/400/300",
                "반려동물 산책 명소(가상)", true, "개", "전체", false, false, true));
        list.add(createFacility("fac_009", "우리동물병원(가상)", FacilityCategory.HOSPITAL,
                "서울 송파구 가락로 150", "02-7890-1234", "09:00-18:00",
                37.4923, 127.1188, 2.3, "https://picsum.photos/seed/fac009/400/300",
                "예방접종·건강검진(가상)", false, "개", "전체", true, false, true));
        list.add(createFacility("fac_010", "건강약국(가상)", FacilityCategory.PHARMACY,
                "서울 강남구 논현로 200", "02-8901-2345", "09:00-20:00",
                37.5045, 127.0260, 1.8, "https://picsum.photos/seed/fac010/400/300",
                "처방전 조제 가능(가상)", false, "개", "전체", true, false, false));
        list.add(createFacility("fac_011", "두리보호소(가상)", FacilityCategory.SHELTER,
                "인천 연수구 센트럴로 80", "032-123-4567", "10:00-17:00",
                37.3886, 126.6626, 12.0, "https://picsum.photos/seed/fac011/400/300",
                "입양·봉사 프로그램 운영(가상)", false, "개", "전체", true, false, true));
        list.add(createFacility("fac_012", "강아지랑(가상)", FacilityCategory.RESTAURANT,
                "서울 마포구 연남로 50", "02-9012-3456", "12:00-21:00",
                37.5612, 126.9235, 2.8, "https://picsum.photos/seed/fac012/400/300",
                "펫 메뉴 제공(가상)", true, "개", "소형 (5kg 이하)", true, false, true));
        list.add(createFacility("fac_013", "포근카페(가상)", FacilityCategory.CAFE,
                "경기 성남시 정자동 100", "031-345-6789", "09:00-21:00",
                37.3675, 127.1140, 6.1, "https://picsum.photos/seed/fac013/400/300",
                "실내 반려동물 동반(가상)", true, "전체", "소형 (5kg 이하)", true, true, true));
        list.add(createFacility("fac_014", "펫글램핑(가상)", FacilityCategory.HOTEL,
                "경기 가평군 가평읍 200", "031-456-7890", "15:00-11:00",
                37.8315, 127.5103, 35.0, "https://picsum.photos/seed/fac014/400/300",
                "자연 속 반려동물 글램핑(가상)", true, "개", "중형 (5~15kg)", false, false, true));
        list.add(createFacility("fac_015", "한강펫공원(가상)", FacilityCategory.TOURISM,
                "서울 영등포구 여의동로 330", "02-0123-4567", "05:00-24:00",
                37.5285, 126.9320, 4.2, "https://picsum.photos/seed/fac015/400/300",
                "한강 반려견 산책 코스(가상)", true, "개", "전체", false, false, true));
        list.add(createFacility("fac_016", "24시동물병원(가상)", FacilityCategory.HOSPITAL,
                "서울 강남구 선릉로 300", "02-1111-2222", "24시간",
                37.5040, 127.0490, 2.0, "https://picsum.photos/seed/fac016/400/300",
                "응급·수술 전문(가상)", false, "개", "전체", true, false, true));
        list.add(createFacility("fac_017", "반려동물약국(가상)", FacilityCategory.PHARMACY,
                "서울 송파구 문정로 80", "02-2222-3333", "09:00-19:00",
                37.4850, 127.1220, 3.1, "https://picsum.photos/seed/fac017/400/300",
                "동물용 영양제 전문(가상)", false, "개", "전체", true, false, false));
        list.add(createFacility("fac_018", "초코카페(가상)", FacilityCategory.CAFE,
                "서울 강남구 압구정로 50", "02-3333-4444", "11:00-23:00",
                37.5270, 127.0280, 2.5, "https://picsum.photos/seed/fac018/400/300",
                "디저트·음료 동반 가능(가상)", true, "전체", "소형 (5kg 이하)", true, true, true));
        return list;
    }

    private Facility createFacility(String id, String name, FacilityCategory category,
                                    String address, String phone, String hours,
                                    double lat, double lng, double distanceKm,
                                    String imageUrl, String description,
                                    boolean petFriendly, String allowedPetType,
                                    String sizeLimit, boolean indoorAllowed,
                                    boolean carrierRequired, boolean leashRequired) {
        return new Facility(id, name, category, address, phone, hours,
                lat, lng, distanceKm, imageUrl, description,
                petFriendly, allowedPetType, sizeLimit,
                indoorAllowed, carrierRequired, leashRequired);
    }

    private List<Hospital> createHospitals() {
        List<Hospital> list = new ArrayList<>();
        list.add(new Hospital("hos_001", "행복동물병원(가상)", "서울 송파구 올림픽로 120",
                "02-1234-5678", "09:00-20:00", 37.5145, 127.1060, 0.8, true,
                "내과·외과·응급 진료(가상)"));
        list.add(new Hospital("hos_002", "서울반려동물센터(가상)", "서울 강남구 테헤란로 88",
                "02-2345-6789", "10:00-19:00", 37.5012, 127.0396, 1.2, true,
                "MRI·CT 보유(가상)"));
        list.add(new Hospital("hos_003", "우리동물병원(가상)", "서울 송파구 가락로 150",
                "02-7890-1234", "09:00-18:00", 37.4923, 127.1188, 2.3, false,
                "예방접종·건강검진(가상)"));
        list.add(new Hospital("hos_004", "24시동물병원(가상)", "서울 강남구 선릉로 300",
                "02-1111-2222", "24시간", 37.5040, 127.0490, 2.0, true,
                "야간 응급 전문(가상)"));
        list.add(new Hospital("hos_005", "마포펫클리닉(가상)", "서울 마포구 월드컵북로 400",
                "02-4444-5555", "09:00-21:00", 37.5700, 126.9100, 3.0, true,
                "피부과·치과(가상)"));
        list.add(new Hospital("hos_006", "분당동물메디컬(가상)", "경기 성남시 분당구 판교로 100",
                "031-555-6666", "10:00-20:00", 37.3947, 127.1110, 7.5, true,
                "종합 검진센터(가상)"));
        list.add(new Hospital("hos_007", "수원반려병원(가상)", "경기 수원시 영통구 200",
                "031-666-7777", "09:00-19:00", 37.2870, 127.0450, 9.0, false,
                "재활치료 전문(가상)"));
        list.add(new Hospital("hos_008", "연수동물병원(가상)", "인천 연수구 센트럴로 150",
                "032-777-8888", "09:00-18:00", 37.3900, 126.6650, 11.5, true,
                "고양이 전문 진료(가상)"));
        list.add(new Hospital("hos_009", "잠실펫케어(가상)", "서울 송파구 올림픽로 300",
                "02-8888-9999", "09:00-20:00", 37.5130, 127.1020, 1.5, true,
                "중형견 수술 경험 풍부(가상)"));
        list.add(new Hospital("hos_010", "강남애니멀(가상)", "서울 강남구 강남대로 500",
                "02-9999-0000", "10:00-19:00", 37.4980, 127.0280, 2.2, false,
                "노령견 케어(가상)"));
        list.add(new Hospital("hos_011", "송파24시(가상)", "서울 송파구 중대로 50",
                "02-1010-2020", "24시간", 37.5020, 127.1150, 2.8, true,
                "응급 수술 24시(가상)"));
        list.add(new Hospital("hos_012", "마포헬스펫(가상)", "서울 마포구 독막로 80",
                "02-3030-4040", "09:00-18:00", 37.5480, 126.9150, 4.5, true,
                "영양·다이어트 상담(가상)"));
        return list;
    }

    private List<RescuedAnimal> createRescuedAnimals() {
        List<RescuedAnimal> list = new ArrayList<>();
        list.add(createRescued("res_001", "초코", "개", "말티즈", "수컷", "서울 송파구",
                "2026-09-01", "보호 중", "https://picsum.photos/seed/res001/300/300",
                "흰색 털, 빨간 목줄 착용 흔적", 37.5050, 127.1100));
        list.add(createRescued("res_002", "루이", "개", "푸들", "암컷", "서울 강남구",
                "2026-09-02", "보호 중", "https://picsum.photos/seed/res002/300/300",
                "갈색 곱슬 털, 귀 끝 하얀색", 37.5000, 127.0400));
        list.add(createRescued("res_003", "바다", "개", "진돗개", "수컷", "경기 성남시",
                "2026-08-28", "입양 대기", "https://picsum.photos/seed/res003/300/300",
                "황갈색 털, 꼬리 끝 흰색", 37.3600, 127.1100));
        list.add(createRescued("res_004", "모찌", "고양이", "코리안숏헤어", "암컷", "서울 마포구",
                "2026-09-03", "보호 중", "https://picsum.photos/seed/res004/300/300",
                "치즈 태비, 왼쪽 귀 접힘", 37.5650, 126.9000));
        list.add(createRescued("res_005", "별이", "개", "시바견", "암컷", "서울 송파구",
                "2026-08-30", "보호 중", "https://picsum.photos/seed/res005/300/300",
                "적갈색 털, 꼬리 말림", 37.5100, 127.1050));
        list.add(createRescued("res_006", "몽이", "개", "믹스견", "수컷", "경기 수원시",
                "2026-08-25", "입양 대기", "https://picsum.photos/seed/res006/300/300",
                "검정·흰색 반반 털", 37.2900, 127.0500));
        list.add(createRescued("res_007", "하늘", "개", "골든 리트리버", "수컷", "서울 강남구",
                "2026-09-04", "보호 중", "https://picsum.photos/seed/res007/300/300",
                "금색 긴 털, 왼쪽 앞발 흰 반점", 37.5020, 127.0350));
        list.add(createRescued("res_008", "두부", "고양이", "Russian Blue", "수컷", "인천 연수구",
                "2026-08-27", "보호 중", "https://picsum.photos/seed/res008/300/300",
                "회색 털, 초록 눈", 37.3890, 126.6600));
        list.add(createRescued("res_009", "콩이", "개", "말티즈", "암컷", "서울 송파구",
                "2026-09-05", "보호 중", "https://picsum.photos/seed/res009/300/300",
                "크림색 털, 분홍 리본 흔적", 37.5080, 127.1120));
        list.add(createRescued("res_010", "마루", "개", "진돗개", "수컷", "경기 성남시",
                "2026-08-22", "입양 진행 중", "https://picsum.photos/seed/res010/300/300",
                "진한 갈색, 귀 세움", 37.3650, 127.1080));
        list.add(createRescued("res_011", "나비", "고양이", "페르시안", "암컷", "서울 마포구",
                "2026-09-06", "보호 중", "https://picsum.photos/seed/res011/300/300",
                "흰색 긴 털, flat face", 37.5620, 126.9200));
        list.add(createRescued("res_012", "밤톨", "개", "푸들", "수컷", "서울 강남구",
                "2026-08-29", "보호 중", "https://picsum.photos/seed/res012/300/300",
                "검은색 푸들, 토끼 컷", 37.4980, 127.0420));
        list.add(createRescued("res_013", "해피", "개", "믹스견", "암컷", "서울 송파구",
                "2026-09-07", "보호 중", "https://picsum.photos/seed/res013/300/300",
                "갈색 중형견, 꼬리 짧음", 37.5060, 127.1080));
        list.add(createRescued("res_014", "구름", "고양이", "코리안숏헤어", "수컷", "경기 수원시",
                "2026-08-24", "입양 대기", "https://picsum.photos/seed/res014/300/300",
                "흰색·회색 줄무늬", 37.2850, 127.0480));
        list.add(createRescued("res_015", "달이", "개", "시바견", "수컷", "서울 강남구",
                "2026-09-08", "보호 중", "https://picsum.photos/seed/res015/300/300",
                "적색, 가슴 흰색", 37.5030, 127.0380));
        list.add(createRescued("res_016", "솜이", "개", "말티즈", "암컷", "인천 연수구",
                "2026-09-04", "보호 중", "https://picsum.photos/seed/res016/300/300",
                "순백색, 눈가 갈색 반점", 37.3870, 126.6630));
        return list;
    }

    private RescuedAnimal createRescued(String id, String name, String species, String breed,
                                        String gender, String region, String rescuedDate,
                                        String status, String imageUrl, String features,
                                        double lat, double lng) {
        return new RescuedAnimal(id, name, species, breed, gender, region,
                rescuedDate, status, imageUrl, features, lat, lng);
    }

    private List<AlertNotification> createAlerts() {
        List<AlertNotification> list = new ArrayList<>();
        list.add(createAlert("alt_001", AlertType.RESCUE, "말티즈", "서울 송파구",
                "2026-09-09 08:30", "보호 중", "https://picsum.photos/seed/alt001/200/200", "res_001"));
        list.add(createAlert("alt_002", AlertType.LOST, "푸들", "서울 강남구",
                "2026-09-09 07:15", "실종 신고", "https://picsum.photos/seed/alt002/200/200", "lost_001"));
        list.add(createAlert("alt_003", AlertType.RESCUE, "진돗개", "경기 성남시",
                "2026-09-08 22:00", "입양 대기", "https://picsum.photos/seed/alt003/200/200", "res_003"));
        list.add(createAlert("alt_004", AlertType.LOST, "시바견", "서울 송파구",
                "2026-09-08 19:45", "실종 신고", "https://picsum.photos/seed/alt004/200/200", "lost_002"));
        list.add(createAlert("alt_005", AlertType.RESCUE, "코리안숏헤어", "서울 마포구",
                "2026-09-08 16:20", "보호 중", "https://picsum.photos/seed/alt005/200/200", "res_004"));
        list.add(createAlert("alt_006", AlertType.LOST, "골든 리트리버", "서울 강남구",
                "2026-09-08 14:10", "실종 신고", "https://picsum.photos/seed/alt006/200/200", "lost_003"));
        list.add(createAlert("alt_007", AlertType.RESCUE, "믹스견", "경기 수원시",
                "2026-09-08 11:30", "입양 대기", "https://picsum.photos/seed/alt007/200/200", "res_006"));
        list.add(createAlert("alt_008", AlertType.LOST, "말티즈", "인천 연수구",
                "2026-09-07 20:00", "실종 신고", "https://picsum.photos/seed/alt008/200/200", "lost_004"));
        list.add(createAlert("alt_009", AlertType.RESCUE, "푸들", "서울 강남구",
                "2026-09-07 15:40", "보호 중", "https://picsum.photos/seed/alt009/200/200", "res_012"));
        list.add(createAlert("alt_010", AlertType.LOST, "진돗개", "경기 성남시",
                "2026-09-07 09:20", "실종 신고", "https://picsum.photos/seed/alt010/200/200", "lost_005"));
        list.add(createAlert("alt_011", AlertType.RESCUE, "시바견", "서울 강남구",
                "2026-09-06 18:00", "보호 중", "https://picsum.photos/seed/alt011/200/200", "res_015"));
        list.add(createAlert("alt_012", AlertType.LOST, "믹스견", "서울 마포구",
                "2026-09-06 12:30", "실종 신고", "https://picsum.photos/seed/alt012/200/200", "lost_006"));
        return list;
    }

    private AlertNotification createAlert(String id, AlertType type, String animalType,
                                          String region, String occurredAt, String status,
                                          String imageUrl, String relatedItemId) {
        return new AlertNotification(id, type, animalType, region, occurredAt,
                status, imageUrl, relatedItemId);
    }

    private List<Symptom> createSymptoms() {
        List<Symptom> list = new ArrayList<>();
        list.add(new Symptom("sym_001", "식욕 저하"));
        list.add(new Symptom("sym_002", "구토"));
        list.add(new Symptom("sym_003", "설사"));
        list.add(new Symptom("sym_004", "기침"));
        list.add(new Symptom("sym_005", "발열"));
        list.add(new Symptom("sym_006", "피부 발진"));
        list.add(new Symptom("sym_007", "절뚝거림"));
        list.add(new Symptom("sym_008", "무기력"));
        return list;
    }

    private List<DiagnosisResult> createDiagnosisResults() {
        List<DiagnosisResult> list = new ArrayList<>();
        list.add(new DiagnosisResult("diag_001", "급성 장염", 78,
                Arrays.asList("구토", "설사", "식욕 저하"),
                "장 점막 염증으로 인한 소화기 증상(가상 Mock 추론)",
                "탈수 예방을 위해 수분 섭취를 늘리고 즉시 수의사 진료를 받으세요."));
        list.add(new DiagnosisResult("diag_002", "급성 위염", 65,
                Arrays.asList("구토", "식욕 저하", "무기력"),
                "위 점막 자극으로 인한 구토 증상(가상 Mock 추론)",
                "금식 후 소량 식사를 권장하며, 구토가 지속되면 병원 방문이 필요합니다."));
        list.add(new DiagnosisResult("diag_003", "기관지염", 72,
                Arrays.asList("기침", "무기력", "발열"),
                "기관지 염증으로 인한 호흡기 증상(가상 Mock 추론)",
                "따뜻한 환경을 유지하고 자극성 연기를 피하세요."));
        list.add(new DiagnosisResult("diag_004", "알레르기성 피부염", 70,
                Arrays.asList("피부 발진", "무기력"),
                "알레르기 반응으로 인한 피부 가려움(가상 Mock 추론)",
                "긁는 행동을 방지하고 알레르기 유발 요인을 확인하세요."));
        list.add(new DiagnosisResult("diag_005", "슬개골 탈구", 68,
                Arrays.asList("절뚝거림", "무기력"),
                "관절 문제로 인한 보행 이상(가상 Mock 추론)",
                "격한 운동을 피하고 정형외과 진료를 권장합니다."));
        list.add(new DiagnosisResult("diag_006", "패혈증 의심", 55,
                Arrays.asList("발열", "무기력", "구토"),
                "전신 염증 반응 가능성(가상 Mock 추론)",
                "응급 상황일 수 있으므로 즉시 동물병원을 방문하세요."));
        list.add(new DiagnosisResult("diag_007", "기생충 감염", 60,
                Arrays.asList("설사", "식욕 저하", "무기력"),
                "장내 기생충으로 인한 소화기 증상(가상 Mock 추론)",
                "기생충 예방약 접종 이력을 확인하고 검사를 받으세요."));
        list.add(new DiagnosisResult("diag_008", "호흡기 감염", 74,
                Arrays.asList("기침", "발열", "무기력"),
                "바이러스성 호흡기 질환 가능성(가상 Mock 추론)",
                "실내 온도를 적절히 유지하고 충분한 휴식을 취하세요."));
        return list;
    }

    private Map<String, List<String>> createSymptomToDiseaseMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("sym_001", Arrays.asList("diag_001", "diag_002", "diag_007"));
        map.put("sym_002", Arrays.asList("diag_001", "diag_002", "diag_006"));
        map.put("sym_003", Arrays.asList("diag_001", "diag_007"));
        map.put("sym_004", Arrays.asList("diag_003", "diag_008"));
        map.put("sym_005", Arrays.asList("diag_003", "diag_006", "diag_008"));
        map.put("sym_006", Arrays.asList("diag_004"));
        map.put("sym_007", Arrays.asList("diag_005"));
        map.put("sym_008", Arrays.asList("diag_002", "diag_003", "diag_004", "diag_005", "diag_006", "diag_007", "diag_008"));
        return map;
    }
}
