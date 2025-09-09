package com.example.application.service;

import com.example.application.domain.*;
import com.example.application.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analyzing payment data and applying ranking rules.
 */
@Service
public class PaymentAnalysisService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final RankConfigurationRepository rankConfigurationRepository;
    private final HouseholdRepository householdRepository;

    public PaymentAnalysisService(PaymentRecordRepository paymentRecordRepository,
                                RankConfigurationRepository rankConfigurationRepository,
                                HouseholdRepository householdRepository) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.rankConfigurationRepository = rankConfigurationRepository;
        this.householdRepository = householdRepository;
    }

    /**
     * Get households with outstanding balances above specified threshold for the latest month
     */
    @Transactional(readOnly = true)
    public List<HouseholdPaymentInfo> getHouseholdsAboveThreshold(BigDecimal threshold) {
        List<PaymentRecord> latestRecords = paymentRecordRepository.findLatestRecords();
        
        return latestRecords.stream()
            .filter(record -> record.getOutstandingBalance().compareTo(threshold) >= 0)
            .map(this::createHouseholdPaymentInfo)
            .sorted((a, b) -> b.outstandingBalance.compareTo(a.outstandingBalance))
            .collect(Collectors.toList());
    }

    /**
     * Get households with outstanding balances above specified threshold for a specific month
     */
    @Transactional(readOnly = true)
    public List<HouseholdPaymentInfo> getHouseholdsAboveThreshold(BigDecimal threshold, LocalDate month) {
        List<PaymentRecord> records = paymentRecordRepository.findByMonthAndBalanceThreshold(month, threshold);
        
        return records.stream()
            .map(this::createHouseholdPaymentInfo)
            .sorted((a, b) -> b.outstandingBalance.compareTo(a.outstandingBalance))
            .collect(Collectors.toList());
    }

    /**
     * Categorize households by rank based on their outstanding balances
     */
    @Transactional(readOnly = true)
    public Map<String, List<HouseholdPaymentInfo>> categorizeHouseholdsByRank() {
        List<RankConfiguration> ranks = rankConfigurationRepository.findActiveRanksOrderByThresholdDesc();
        List<PaymentRecord> latestRecords = paymentRecordRepository.findLatestRecords();

        Map<String, List<HouseholdPaymentInfo>> categorized = new LinkedHashMap<>();

        // Initialize categories
        for (RankConfiguration rank : ranks) {
            categorized.put(rank.getRankName(), new ArrayList<>());
        }
        categorized.put("Normal", new ArrayList<>());

        // Categorize households
        for (PaymentRecord record : latestRecords) {
            HouseholdPaymentInfo info = createHouseholdPaymentInfo(record);
            String category = determineRankCategory(info.outstandingBalance, ranks);
            categorized.get(category).add(info);
        }

        // Sort each category by balance descending
        categorized.values().forEach(list ->
            list.sort((a, b) -> b.outstandingBalance.compareTo(a.outstandingBalance)));

        return categorized;
    }

    /**
     * Categorize households by rank for a specific month
     */
    @Transactional(readOnly = true)
    public Map<String, List<HouseholdPaymentInfo>> categorizeHouseholdsByRank(LocalDate month) {
        List<RankConfiguration> ranks = rankConfigurationRepository.findActiveRanksOrderByThresholdDesc();
        List<PaymentRecord> monthRecords = paymentRecordRepository.findByRecordMonth(month);

        Map<String, List<HouseholdPaymentInfo>> categorized = new LinkedHashMap<>();

        // Initialize categories
        for (RankConfiguration rank : ranks) {
            categorized.put(rank.getRankName(), new ArrayList<>());
        }
        categorized.put("Normal", new ArrayList<>());

        // Categorize households
        for (PaymentRecord record : monthRecords) {
            HouseholdPaymentInfo info = createHouseholdPaymentInfo(record);
            String category = determineRankCategory(info.outstandingBalance, ranks);
            categorized.get(category).add(info);
        }

        // Sort each category by balance descending
        categorized.values().forEach(list ->
            list.sort((a, b) -> b.outstandingBalance.compareTo(a.outstandingBalance)));

        return categorized;
    }

    /**
     * Get payment statistics by building
     */
    @Transactional(readOnly = true)
    public List<BuildingStatistics> getBuildingStatistics() {
        List<PaymentRecord> latestRecords = paymentRecordRepository.findLatestRecords();

        Map<String, List<PaymentRecord>> recordsByBuilding = latestRecords.stream()
            .collect(Collectors.groupingBy(record ->
                record.getHousehold().getApartment().getEntrance().getBuilding().getBuildingNumber()));

        return recordsByBuilding.entrySet().stream()
            .map(entry -> createBuildingStatistics(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(BuildingStatistics::getBuildingNumber))
            .collect(Collectors.toList());
    }

    /**
     * Get payment statistics by building for a specific month
     */
    @Transactional(readOnly = true)
    public List<BuildingStatistics> getBuildingStatistics(LocalDate month) {
        List<PaymentRecord> monthRecords = paymentRecordRepository.findByRecordMonth(month);

        Map<String, List<PaymentRecord>> recordsByBuilding = monthRecords.stream()
            .collect(Collectors.groupingBy(record ->
                record.getHousehold().getApartment().getEntrance().getBuilding().getBuildingNumber()));

        return recordsByBuilding.entrySet().stream()
            .map(entry -> createBuildingStatistics(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(BuildingStatistics::getBuildingNumber))
            .collect(Collectors.toList());
    }

    /**
     * Get payment statistics by entrance within a building
     */
    @Transactional(readOnly = true)
    public List<EntranceStatistics> getEntranceStatistics(String buildingNumber) {
        List<PaymentRecord> latestRecords = paymentRecordRepository.findLatestRecords();
        
        Map<Integer, List<PaymentRecord>> recordsByEntrance = latestRecords.stream()
            .filter(record -> record.getHousehold().getApartment().getEntrance()
                .getBuilding().getBuildingNumber().equals(buildingNumber))
            .collect(Collectors.groupingBy(record -> 
                record.getHousehold().getApartment().getEntrance().getEntranceNumber()));

        return recordsByEntrance.entrySet().stream()
            .map(entry -> createEntranceStatistics(buildingNumber, entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(EntranceStatistics::getEntranceNumber))
            .collect(Collectors.toList());
    }

    private HouseholdPaymentInfo createHouseholdPaymentInfo(PaymentRecord record) {
        Household household = record.getHousehold();
        Apartment apartment = household.getApartment();
        Entrance entrance = apartment.getEntrance();
        Building building = entrance.getBuilding();

        return new HouseholdPaymentInfo(
            household.getId(),
            household.getHouseholdName(),
            building.getBuildingNumber(),
            entrance.getEntranceNumber(),
            apartment.getDoorNumber(),
            apartment.getFloorNumber(),
            record.getOutstandingBalance(),
            record.getRecordMonth(),
            determineRankCategory(record.getOutstandingBalance(), 
                rankConfigurationRepository.findActiveRanksOrderByThresholdDesc())
        );
    }

    private String determineRankCategory(BigDecimal balance, List<RankConfiguration> ranks) {
        for (RankConfiguration rank : ranks) {
            if (balance.compareTo(rank.getThresholdAmount()) >= 0) {
                return rank.getRankName();
            }
        }
        return "Normal";
    }

    private BuildingStatistics createBuildingStatistics(String buildingNumber, List<PaymentRecord> records) {
        int totalHouseholds = records.size();
        BigDecimal totalOutstanding = records.stream()
            .map(PaymentRecord::getOutstandingBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long householdsWithDebt = records.stream()
            .filter(record -> record.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
            .count();

        BigDecimal averageDebt = totalHouseholds > 0 ? 
            totalOutstanding.divide(BigDecimal.valueOf(totalHouseholds), 2, BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;

        return new BuildingStatistics(buildingNumber, totalHouseholds, (int) householdsWithDebt, 
                                    totalOutstanding, averageDebt);
    }

    private EntranceStatistics createEntranceStatistics(String buildingNumber, Integer entranceNumber, 
                                                       List<PaymentRecord> records) {
        int totalHouseholds = records.size();
        BigDecimal totalOutstanding = records.stream()
            .map(PaymentRecord::getOutstandingBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long householdsWithDebt = records.stream()
            .filter(record -> record.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
            .count();

        return new EntranceStatistics(buildingNumber, entranceNumber, totalHouseholds, 
                                    (int) householdsWithDebt, totalOutstanding);
    }

    // Data classes
    public static class HouseholdPaymentInfo {
        public final Long householdId;
        public final String householdName;
        public final String buildingNumber;
        public final Integer entranceNumber;
        public final Integer doorNumber;
        public final Integer floorNumber;
        public final BigDecimal outstandingBalance;
        public final LocalDate recordMonth;
        public final String rankCategory;

        public HouseholdPaymentInfo(Long householdId, String householdName, String buildingNumber,
                                  Integer entranceNumber, Integer doorNumber, Integer floorNumber,
                                  BigDecimal outstandingBalance, LocalDate recordMonth, String rankCategory) {
            this.householdId = householdId;
            this.householdName = householdName;
            this.buildingNumber = buildingNumber;
            this.entranceNumber = entranceNumber;
            this.doorNumber = doorNumber;
            this.floorNumber = floorNumber;
            this.outstandingBalance = outstandingBalance;
            this.recordMonth = recordMonth;
            this.rankCategory = rankCategory;
        }

        public String getFullAddress() {
            return buildingNumber + "-" + entranceNumber + "-" + doorNumber;
        }
    }

    public static class BuildingStatistics {
        private final String buildingNumber;
        private final int totalHouseholds;
        private final int householdsWithDebt;
        private final BigDecimal totalOutstanding;
        private final BigDecimal averageDebt;

        public BuildingStatistics(String buildingNumber, int totalHouseholds, int householdsWithDebt,
                                BigDecimal totalOutstanding, BigDecimal averageDebt) {
            this.buildingNumber = buildingNumber;
            this.totalHouseholds = totalHouseholds;
            this.householdsWithDebt = householdsWithDebt;
            this.totalOutstanding = totalOutstanding;
            this.averageDebt = averageDebt;
        }

        // Getters
        public String getBuildingNumber() { return buildingNumber; }
        public int getTotalHouseholds() { return totalHouseholds; }
        public int getHouseholdsWithDebt() { return householdsWithDebt; }
        public BigDecimal getTotalOutstanding() { return totalOutstanding; }
        public BigDecimal getAverageDebt() { return averageDebt; }
    }

    public static class EntranceStatistics {
        private final String buildingNumber;
        private final Integer entranceNumber;
        private final int totalHouseholds;
        private final int householdsWithDebt;
        private final BigDecimal totalOutstanding;

        public EntranceStatistics(String buildingNumber, Integer entranceNumber, int totalHouseholds,
                                int householdsWithDebt, BigDecimal totalOutstanding) {
            this.buildingNumber = buildingNumber;
            this.entranceNumber = entranceNumber;
            this.totalHouseholds = totalHouseholds;
            this.householdsWithDebt = householdsWithDebt;
            this.totalOutstanding = totalOutstanding;
        }

        // Getters
        public String getBuildingNumber() { return buildingNumber; }
        public Integer getEntranceNumber() { return entranceNumber; }
        public int getTotalHouseholds() { return totalHouseholds; }
        public int getHouseholdsWithDebt() { return householdsWithDebt; }
        public BigDecimal getTotalOutstanding() { return totalOutstanding; }
    }

    /**
     * Get payment history for a specific household
     */
    @Transactional(readOnly = true)
    public List<PaymentHistoryInfo> getHouseholdPaymentHistory(Long householdId) {
        List<PaymentRecord> records = paymentRecordRepository.findByHouseholdIdOrderByRecordMonth(householdId);

        return records.stream()
            .map(record -> new PaymentHistoryInfo(
                record.getRecordMonth(),
                record.getOutstandingBalance(),
                determineRankCategory(record.getOutstandingBalance(),
                    rankConfigurationRepository.findActiveRanksOrderByThresholdDesc())
            ))
            .collect(Collectors.toList());
    }

    /**
     * Payment history information for a household
     */
    public static class PaymentHistoryInfo {
        public final LocalDate month;
        public final BigDecimal outstandingBalance;
        public final String rankCategory;

        public PaymentHistoryInfo(LocalDate month, BigDecimal outstandingBalance, String rankCategory) {
            this.month = month;
            this.outstandingBalance = outstandingBalance;
            this.rankCategory = rankCategory;
        }

        public LocalDate getMonth() {
            return month;
        }

        public BigDecimal getOutstandingBalance() {
            return outstandingBalance;
        }

        public String getRankCategory() {
            return rankCategory;
        }
    }
}
