package com.animalloo.ui.detail;

import android.content.Context;

import com.animalloo.R;
import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.AlertType;
import com.animalloo.data.model.DetailField;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.DetailUiModel;
import com.animalloo.data.model.Facility;
import com.animalloo.data.model.Hospital;
import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.data.model.RescuedAnimal;

import java.util.ArrayList;
import java.util.List;

public final class DetailUiMapper {

    private DetailUiMapper() {
    }

    public static DetailUiModel fromFacility(Context context, Facility facility) {
        List<DetailField> fields = new ArrayList<>();
        fields.add(new DetailField(context.getString(R.string.detail_category),
                facility.getCategory().getDisplayName()));
        fields.add(new DetailField(context.getString(R.string.detail_address), facility.getAddress()));
        fields.add(new DetailField(context.getString(R.string.detail_phone), facility.getPhone()));
        fields.add(new DetailField(context.getString(R.string.detail_hours), facility.getHours()));
        fields.add(new DetailField(context.getString(R.string.detail_distance),
                context.getString(R.string.map_distance_format, facility.getDistanceKm())));

        if (facility.isPetFriendly()) {
            fields.add(new DetailField(context.getString(R.string.detail_pet_type),
                    facility.getAllowedPetType()));
            fields.add(new DetailField(context.getString(R.string.detail_size_limit),
                    facility.getSizeLimit()));
            fields.add(new DetailField(context.getString(R.string.detail_indoor_allowed),
                    facility.isIndoorAllowed()
                            ? context.getString(R.string.filter_yes)
                            : context.getString(R.string.filter_no)));
            fields.add(new DetailField(context.getString(R.string.detail_carrier_required),
                    facility.isCarrierRequired()
                            ? context.getString(R.string.filter_yes)
                            : context.getString(R.string.filter_no)));
            fields.add(new DetailField(context.getString(R.string.detail_leash_required),
                    facility.isLeashRequired()
                            ? context.getString(R.string.filter_yes)
                            : context.getString(R.string.filter_no)));
        }

        return new DetailUiModel(
                DetailType.FACILITY,
                facility.getName(),
                facility.getCategory().getDisplayName(),
                facility.getImageUrl(),
                facility.getDescription(),
                fields,
                null,
                null
        );
    }

    public static DetailUiModel fromHospital(Context context, Hospital hospital) {
        List<DetailField> fields = new ArrayList<>();
        fields.add(new DetailField(context.getString(R.string.detail_address), hospital.getAddress()));
        fields.add(new DetailField(context.getString(R.string.detail_phone), hospital.getPhone()));
        fields.add(new DetailField(context.getString(R.string.detail_hours), hospital.getHours()));
        fields.add(new DetailField(context.getString(R.string.detail_distance),
                context.getString(R.string.map_distance_format, hospital.getDistanceKm())));
        fields.add(new DetailField(context.getString(R.string.detail_open_status),
                context.getString(hospital.isOpenNow() ? R.string.hospital_open : R.string.hospital_closed)));

        return new DetailUiModel(
                DetailType.HOSPITAL,
                hospital.getName(),
                context.getString(hospital.isOpenNow() ? R.string.hospital_open : R.string.hospital_closed),
                null,
                hospital.getDescription(),
                fields,
                null,
                null
        );
    }

    public static DetailUiModel fromRescuedAnimal(Context context, RescuedAnimal animal) {
        List<DetailField> fields = new ArrayList<>();
        fields.add(new DetailField(context.getString(R.string.detail_species), animal.getSpecies()));
        fields.add(new DetailField(context.getString(R.string.detail_breed), animal.getBreed()));
        fields.add(new DetailField(context.getString(R.string.detail_gender), animal.getGender()));
        fields.add(new DetailField(context.getString(R.string.detail_region), animal.getRegion()));
        fields.add(new DetailField(context.getString(R.string.detail_rescued_date), animal.getRescuedDate()));
        fields.add(new DetailField(context.getString(R.string.detail_protection_status),
                animal.getProtectionStatus()));

        return new DetailUiModel(
                DetailType.RESCUED_ANIMAL,
                animal.getName(),
                animal.getProtectionStatus(),
                animal.getImageUrl(),
                animal.getFeatures(),
                fields,
                null,
                null
        );
    }

    public static DetailUiModel fromAlert(Context context, AlertNotification alert) {
        List<DetailField> fields = new ArrayList<>();
        fields.add(new DetailField(context.getString(R.string.detail_alert_type),
                alert.getType().getDisplayName()));
        fields.add(new DetailField(context.getString(R.string.detail_species), alert.getAnimalType()));
        fields.add(new DetailField(context.getString(R.string.detail_region), alert.getRegion()));
        fields.add(new DetailField(context.getString(R.string.detail_occurred_at), alert.getOccurredAt()));
        fields.add(new DetailField(context.getString(R.string.detail_status), alert.getStatus()));

        DetailType relatedType = null;
        if (alert.getType() == AlertType.RESCUE) {
            relatedType = DetailType.RESCUED_ANIMAL;
        } else if (alert.getType() == AlertType.LOST) {
            relatedType = DetailType.LOST_ANIMAL;
        }

        String title = context.getString(
                R.string.detail_alert_title_format,
                alert.getAnimalType(),
                alert.getType().getDisplayName());

        return new DetailUiModel(
                DetailType.ALERT,
                title,
                alert.getStatus(),
                alert.getImageUrl(),
                null,
                fields,
                relatedType,
                alert.getRelatedItemId()
        );
    }

    public static DetailUiModel fromLostReport(Context context, LostAnimalReport report) {
        List<DetailField> fields = new ArrayList<>();
        fields.add(new DetailField(context.getString(R.string.detail_species), report.getSpecies()));
        fields.add(new DetailField(context.getString(R.string.detail_breed), report.getBreed()));
        fields.add(new DetailField(context.getString(R.string.detail_gender), report.getGender()));
        fields.add(new DetailField(context.getString(R.string.detail_lost_region), report.getRegion()));
        fields.add(new DetailField(context.getString(R.string.detail_lost_date), report.getLostDate()));
        fields.add(new DetailField(context.getString(R.string.detail_contact), report.getContactInfo()));

        return new DetailUiModel(
                DetailType.LOST_ANIMAL,
                report.getName(),
                report.getRegion(),
                report.getPhotoPath(),
                report.getFeatures(),
                fields,
                null,
                null
        );
    }
}
