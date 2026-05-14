package com.soulsurf.backend.modules.weather.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SurfConditionsDTO {
    private String requestedAt;
    private LocationDTO location;
    private MarineDTO marine;
    private WindDTO wind;
    private SurfQualityDTO surfQuality;
    private BalneabilityDTO balneability;
    private TideDTO tide;
    private List<String> sources;

    @Data
    @Builder
    public static class LocationDTO {
        private Double latitude;
        private Double longitude;
        private String timezone;
    }

    @Data
    @Builder
    public static class MarineDTO {
        private Double waveHeightMeters;
        private Double waveDirectionDegrees;
        private Double wavePeriodSeconds;
        private Double seaSurfaceTemperatureC;
        private Double oceanCurrentVelocityKmh;
        private Double oceanCurrentDirectionDegrees;
    }

    @Data
    @Builder
    public static class WindDTO {
        private Double windSpeedKmh;
        private Double windDirectionDegrees;
        private Double windGustKmh;
        private Integer weatherCode;
    }

    @Data
    @Builder
    public static class SurfQualityDTO {
        private String label;
        private Integer score;
        private List<String> reasons;
    }

    @Data
    @Builder
    public static class BalneabilityDTO {
        private String provider;
        private String reportUrl;
        private String period;
        private String beachQuery;
        private String overallStatus;
        private Integer totalPoints;
        private Integer properPoints;
        private Integer alertPoints;
        private Integer improperPoints;
        private List<BalneabilityPointDTO> matchedPoints;
        private String observation;
    }

    @Data
    @Builder
    public static class BalneabilityPointDTO {
        private String pointCode;
        private String description;
        private String status;
    }

    @Data
    @Builder
    public static class TideDTO {
        private String provider;
        private String station;
        private String sourceUrl;
        private String timezone;
        private String updatedAt;
        private String expiresAt;
        private String currentStatus;
        private String currentLabel;
        private Double currentHeightMeters;
        private Integer fillPercent;
        private String nextTurnLabel;
        private TideEventDTO previousEvent;
        private TideEventDTO nextEvent;
        private List<TideEventDTO> nextEvents;
        private List<TideSurfWindowDTO> bestSurfWindows;
        private String recommendationLabel;
        private String recommendation;
        private String observation;
    }

    @Data
    @Builder
    public static class TideEventDTO {
        private String type;
        private String dateTime;
        private String timeLabel;
        private Double heightMeters;
    }

    @Data
    @Builder
    public static class TideSurfWindowDTO {
        private String startsAt;
        private String endsAt;
        private String label;
        private Integer score;
        private Boolean activeNow;
        private String reason;
    }
}
