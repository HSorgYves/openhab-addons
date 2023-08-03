/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.connectedcar.internal.api.weconnect;

import static org.openhab.binding.connectedcar.internal.BindingConstants.API_REQUEST_TIMEOUT_SEC;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_REQUEST_QUEUED;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_REQUEST_STARTED;

import java.util.ArrayList;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * {@link WeConnectApiJsonDTO} defines the We Connect API data formats
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
public class WeConnectApiJsonDTO {
    public static final String WCAPI_BASE_URL = "https://emea.bff.cariad.digital/vehicle/v1";

    public static final String WCSERVICE_STATUS = "status";
    public static final String WCSERVICE_CLIMATISATION = "climatisation";
    public static final String WCSERVICE_CHARGING = "charging";

    public static final String WCCAPABILITY_PARKINGPOS = "parkingPosition";

    public static class WCCapability {
        /*
         * {
         * "id": "automation",
         * "expirationDate": "2024-05-09T00:00:00Z",
         * "userDisablingAllowed": true
         * }
         */
        public String id;
        public String expirationDate;
        public Boolean userDisablingAllowed;
        public ArrayList<Integer> status;
    }

    public static class WCVehicleList {
        public static class WCVehicle {
            /*
             * "vin": "WVWZZZE1ZMP053898",
             * "role": "PRIMARY_USER",
             * "enrollmentStatus": "COMPLETED",
             * "model": "ID.3",
             * "nickname": "ID.3",
             * "capabilities": []
             */

            public String vin;
            public String role;
            public String enrollmentStatus;
            public String vehicle;
            public String model;
            public String nickname;
            public ArrayList<WCCapability> capabilities;
        }

        public ArrayList<WCVehicle> data;
    }

    public static class WCVehicleStatusData {
        public static class WCSingleStatusItem {
            /*
             * {
             * "name": "left",
             * "status": "on"
             * },
             */
            public String name;
            public String status;
        }

        public static class WCMultiStatusItem {
            /*
             * {
             * "name": "bonnet",
             * "status": [
             * "closed"
             * ]
             * },
             */
            public String name;
            public ArrayList<String> status;
        }

        // public class WCVehicleStatus {
        public static class WCAccessStatus {

            public String carCapturedTimestamp;
            public String overallStatus;
            public ArrayList<WCMultiStatusItem> doors;
            public ArrayList<WCMultiStatusItem> windows;
        }

        public static class WCBatteryStatus {
            /*
             * "batteryStatus": {
             * "carCapturedTimestamp": "2021-06-25T14:01:35Z",
             * "currentSOC_pct": 52,
             * "cruisingRangeElectric_km": 221
             * },
             */
            public String carCapturedTimestamp;
            public Integer currentSOC_pct;
            public Integer cruisingRangeElectric_km;
        }

        public static class WCChargingStatus {
            /*
             * "chargingStatus": {
             * "carCapturedTimestamp": "2021-06-25T14:01:35Z",
             * "remainingChargingTimeToComplete_min": 0,
             * "chargingState": "readyForCharging",
             * "chargeMode": "manual",
             * "chargePower_kW": 0,
             * "chargeRate_kmph": 0
             * },
             */
            public String carCapturedTimestamp;
            public Integer remainingChargingTimeToComplete_min;
            public String chargingState;
            public String chargeMode;
            public Integer chargePower_kW;
            public Integer chargeRate_kmph;
        }

        public static class WCChargingSettings {
            /*
             * "chargingSettings": {
             * "carCapturedTimestamp": "2021-06-25T23:06:41Z",
             * "maxChargeCurrentAC": "maximum",
             * "autoUnlockPlugWhenCharged": "permanent",
             * "targetSOC_pct": 90
             * },
             */
            public String carCapturedTimestamp;
            public String maxChargeCurrentAC;
            public String autoUnlockPlugWhenCharged;
            public Integer targetSOC_pct;
        }

        public static class WCChargeMode {
            /*
             * "chargeMode":{
             * "preferredChargeMode":"manual",
             * "availableChargeModes":[
             * "invalid"
             * ]
             * },
             *
             */
            public String preferredChargeMode;
            public ArrayList<String> availableChargeModes;
        }

        public static class WCPlugStatus {
            /*
             * "plugStatus": {
             * "carCapturedTimestamp": "2021-06-25T23:06:41Z",
             * "plugConnectionState": "disconnected",
             * "plugLockState": "unlocked"
             * },
             */
            public String carCapturedTimestamp;
            public String plugConnectionState;
            public String plugLockState;
        }

        public static class WCClimatisationStatus {
            /*
             * "climatisationStatus": {
             * "carCapturedTimestamp": "2021-06-25T23:06:40Z",
             * "remainingClimatisationTime_min": 0,
             * "climatisationState": "off"
             * },
             */
            public String carCapturedTimestamp;
            public Integer remainingClimatisationTime_min;
            public String climatisationState;
        }

        public static class WCClimatisationSettings {
            /*
             * "climatisationSettings": {
             * "carCapturedTimestamp": "2021-06-25T23:06:47Z",
             * "targetTemperature_K": 295.15,
             * "targetTemperature_C": 22,
             * "climatisationWithoutExternalPower": true,
             * "climatizationAtUnlock": false,
             * "windowHeatingEnabled": false,
             * "zoneFrontLeftEnabled": false,
             * "zoneFrontRightEnabled": false,
             * "unitInCar": "celsius"
             * },
             */
            public String carCapturedTimestamp;
            public Double targetTemperature_K;
            public Double targetTemperature_C;
            public Boolean climatisationWithoutExternalPower;
            public Boolean climatizationAtUnlock;
            public Boolean windowHeatingEnabled;
            public Boolean zoneFrontLeftEnabled;
            public Boolean zoneFrontRightEnabled;
            public String unitInCar;
        }

        public static class WCClimatisationTimer {
            /*
             * {
             * "id": 1,
             * "enabled": false,
             * "singleTimer": {
             * "startDateTime": "1999-12-31T22:00:00Z"
             * }
             */
            public static class WCClimaTimer {
                public static class WCSingleTimer {
                    public String startDateTime;
                }

                public String id;
                public Boolean enabled;
                public WCSingleTimer singleTimer;
            }

            public ArrayList<WCClimaTimer> timers;
            public String carCapturedTimestamp;
            public String timeInCar;
        }

        public static class WCWindowHeatingStatus {
            /*
             * "windowHeatingStatus": {
             * "carCapturedTimestamp": "2021-06-25T14:01:37Z",
             * "windowHeatingStatus": [
             * {
             * "windowLocation": "front",
             * "windowHeatingState": "off"
             * },
             * {
             * "windowLocation": "rear",
             * "windowHeatingState": "off"
             * }
             * ]
             * },
             */

            public static class WCHeatingStatus {
                public String windowLocation;
                public String windowHeatingState;
            }

            public String carCapturedTimestamp;
            public ArrayList<WCHeatingStatus> windowHeatingStatus;
        }

        public static class WCLightStatus {
            /*
             * "lightsStatus": {
             * "carCapturedTimestamp": "2021-09-04T16:59:11Z",
             * "lights": [
             * {
             * "name": "right",
             * "status": "off"
             * },
             * {
             * "name": "left",
             * "status": "off"
             * }
             * ]
             * },
             */
            public String carCapturedTimestamp;
            public ArrayList<WCSingleStatusItem> lights;
        }

        public static class WCRangeStatus {
            /*
             * "rangeStatus":
             * {
             * "carCapturedTimestamp": "2021-06-25T14:01:35Z",
             * "carType": "electric",
             * "primaryEngine": {
             * "type": "electric",
             * "currentSOC_pct": 52,
             * "remainingRange_km": 221
             * },
             * "totalRange_km": 221
             * },
             */
            public static class WCEngine {
                public String type;
                public Integer currentSOC_pct;
                public Integer remainingRange_km;
                public Integer currentFuelLevel_pct;
            }

            public String carCapturedTimestamp;
            public String carType;
            public WCEngine primaryEngine;
            public WCEngine secondaryEngine;
            public Integer totalRange_km;
        }

        public static class WCMaintenanceStatus {
            public String carCapturedTimestamp;
            @SerializedName("inspectionDue_days")
            public Integer inspectionDueDays;
            @SerializedName("inspectionDue_km")
            public Integer inspectionDueKm;
            @SerializedName("mileage_km")
            public Integer mileageKm;
            @SerializedName("oilServiceDue_days")
            public Integer oilServiceDueDays;
            @SerializedName("oilServiceDue_km")
            public Integer oilServiceDueKm;
        }

        public static class WCCapabilityStatus {
            /*
             * "capabilityStatus":
             * {
             * "capabilities": [
             * {
             * "id": "automation",
             * "expirationDate": "2024-05-09T00:00:00Z",
             * "userDisablingAllowed": true
             * },
             */
            public ArrayList<WCCapability> capabilities;
        }

        public static class WCAccessStatusValue {
            public static class WCAccessStatusStatus {
                public WCAccessStatus value;
            }

            public WCAccessStatusStatus accessStatus;
        }

        public static class WCVehicleLightsValue {
            public static class WCVehicleLightsStatus {
                public WCLightStatus value;
            }

            public WCVehicleLightsStatus lightsStatus;
        }

        public static class WCFuelStatus {
            public static class WCFuelStatusValue {
                public WCRangeStatus value;
            }

            public WCFuelStatusValue rangeStatus;
        }

        public static class WCUserCapabilities {
            public static class WCCapcabilitiesStatus {
                public ArrayList<WCCapability> value;
            }

            public WCCapcabilitiesStatus capabilitiesStatus;
        }

        public static class WCVehicleHealthInspection {
            public static class WCVehicleHealthInspectionStatus {
                public WCMaintenanceStatus value;
            }

            public WCVehicleHealthInspectionStatus maintenanceStatus;
        }

        public static class WCCharging {
            public static class WCBatteryStatusValue {
                public WCBatteryStatus value;
            }

            public static class WCChargeModeStatus {
                public WCChargeMode value;
            }

            public static class WCChargingSettingsValue {
                WCChargingSettings value;
            }

            public static class WCChargingStatusValue {
                public WCChargingStatus value;
            }

            public static class WCPlugStatusValue {
                public WCPlugStatus value;
            }

            public static class WCChargingCareSettingsValue {
                public static class WCChargingCareSettings {
                    /*
                     * "batteryCareMode": "activated"
                     */
                    public String batteryCareMode;
                }

                public WCChargingCareSettings value;
            }

            public WCBatteryStatusValue batteryStatus;
            public WCChargingCareSettingsValue chargingCareSettings;
            public WCChargeModeStatus chargeMode;
            public WCChargingSettingsValue chargingSettings;
            public WCChargingStatusValue chargingStatus;
            public WCPlugStatusValue plugStatus;
        }

        public static class WCClimatistation {
            public static class WCClimatisationSettingsValue {
                public WCClimatisationSettings value;
            }

            public static class WCClimatisationStatusValue {
                public WCClimatisationStatus value;
            }

            public static class WCClimatisationTimerValue {
                public WCClimatisationTimer value;
            }

            public static class WCWindowHeatingStatusValue {
                public WCWindowHeatingStatus value;
            }

            public WCClimatisationSettingsValue climatisationSettings;
            public WCClimatisationStatusValue climatisationStatus;
            public WCClimatisationTimerValue climatisationTimer;
            public WCWindowHeatingStatusValue windowHeatingStatus;
        }

        public static class WCMeasurementsRangeStatus {
            /*
             * ID.3 Pro Performance:
             * "electricRange": 166
             *
             * Audi A7 Sportback:
             * "adBlueRange": 6000
             * "dieselRange": 660
             *
             * Audi e-tron:
             * "electricRange": 166
             *
             * Audi Q3 Sportback:
             * "gasolineRange": 65
             */

            public Integer electricRange;
            public Integer adBlueRange;
            public Integer dieselRange;
            public Integer gasolineRange;
        }

        public static class WCMeasurementsOdometerStatus {
            /*
             * ID.3 Pro Performance:
             * "odometer": 6848
             *
             * Audi A7 Sportback:
             * "odometer": 123388
             *
             * Audi e-tron:
             * "odometer": 40096
             */

            public Integer odometer;
        }

        public static class WCMeasurements {
            public static class WCMeasurementsRangeStatusValue {
                public WCMeasurementsRangeStatus value;
            }

            public static class WCMeasurementsOdometerStatusValue {
                public WCMeasurementsOdometerStatus value;
            }

            public WCMeasurementsRangeStatusValue rangeStatus;
            public WCMeasurementsOdometerStatusValue odometerStatus;
        }

        public static class WCOilLevelStatus {
            public Boolean value;
        }

        public static class WCOilLevel {
            public static class WCOilLevelStatusValue {
                public WCOilLevelStatus value;
            }

            public WCOilLevelStatusValue oilLevelStatus;
        }

        public WCAccessStatusValue access;
        public WCVehicleLightsValue vehicleLights;
        public WCFuelStatus fuelStatus;
        public WCUserCapabilities userCapabilities;
        public WCVehicleHealthInspection vehicleHealthInspection;
        public WCCharging charging;
        public WCClimatistation climatisation;
        public WCMeasurements measurements;
        public WCOilLevel oilLevel;
    }

    public static class WCActionResponse {
        public static class WCApiError {
            /*
             * "error": {
             * "code": 2105,
             * "message": "The provided request is incorrect",
             * "group": 2,
             * "info": "Internal error, please try again later. If the problem persists, please contact our support.",
             * "retry": true
             * }
             */
            public static class WCApiErrorDetails {
                public Integer code;
                public String message;
                public Integer group;
                public String info;
                public Boolean retry;
            }

            public WCApiErrorDetails error;
        }

        public static class WCApiError2 {
            public String uri;
            public String status;
            public String message;
        }

        /*
         * {
         * "data": {
         * "requestID": "a1d78f9c-90be-4adb-8ce8-b0d196c239d8"
         * }
         * }
         */
        public static class WCActionResponseData {
            public String requestID;
        }

        public WCActionResponseData data;
        public WCApiError error;
    }

    public static class WCParkingPosition {
        /*
         * {
         * "data": {
         * "lon": 6.83788,
         * "lat": 50.960526,
         * "carCapturedTimestamp": "2021-09-07T08:53:28Z"
         * }
         * }
         */
        public static class WeConnectParkingPosition {
            public String carCapturedTimestamp;
            public String lon, lat;
        }

        WeConnectParkingPosition data;
    }

    public static class WCPendingRequest {
        public String vin = "";
        public String service = "";
        public String action = "";
        public String requestId = "";
        public String status = "";
        public String checkUrl = "";
        public Date creationTime = new Date();
        public long timeout = API_REQUEST_TIMEOUT_SEC;

        public WCPendingRequest(String vin, String service, String action, String requestId) {
            this.vin = vin;
            this.service = service;
            this.action = action;
            this.requestId = requestId;
            this.checkUrl = WCAPI_BASE_URL + "/vehicles/{2}/requests/" + requestId + "/status";
        }

        public static boolean isInProgress(String status) {
            String st = status.toLowerCase();
            return API_REQUEST_QUEUED.equals(st) || API_REQUEST_STARTED.equals(st);
        }

        public boolean isExpired() {
            Date currentTime = new Date();
            long diff = currentTime.getTime() - creationTime.getTime();
            return (diff / 1000) > timeout;
        }
    }
}
