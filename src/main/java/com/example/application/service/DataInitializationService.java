package com.example.application.service;

import com.example.application.domain.*;
import com.example.application.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * Initializes the Happy Town apartment complex data structure.
 * Creates 4 buildings with their entrances and apartments.
 */
@Service
public class DataInitializationService implements CommandLineRunner {

    private final BuildingRepository buildingRepository;
    private final EntranceRepository entranceRepository;
    private final ApartmentRepository apartmentRepository;
    private final HouseholdRepository householdRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final RankConfigurationRepository rankConfigurationRepository;
    private final AppConfigurationRepository appConfigurationRepository;

    public DataInitializationService(BuildingRepository buildingRepository,
                                   EntranceRepository entranceRepository,
                                   ApartmentRepository apartmentRepository,
                                   HouseholdRepository householdRepository,
                                   PaymentRecordRepository paymentRecordRepository,
                                   RankConfigurationRepository rankConfigurationRepository,
                                   AppConfigurationRepository appConfigurationRepository) {
        this.buildingRepository = buildingRepository;
        this.entranceRepository = entranceRepository;
        this.apartmentRepository = apartmentRepository;
        this.householdRepository = householdRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.rankConfigurationRepository = rankConfigurationRepository;
        this.appConfigurationRepository = appConfigurationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (buildingRepository.count() > 0) {
            return; // Data already initialized
        }

        initializeBuildings();
        initializeDefaultRankConfigurations();
        initializeHouseholdsAndPaymentData();
    }

    private void initializeBuildings() {
        // Building 71 - 2 entrances
        Building building71 = new Building("71", 2);
        buildingRepository.save(building71);
        createEntrancesAndApartments(building71, 2);

        // Building 72 - 3 entrances
        Building building72 = new Building("72", 3);
        buildingRepository.save(building72);
        createEntrancesAndApartments(building72, 3);

        // Building 73 - 3 entrances
        Building building73 = new Building("73", 3);
        buildingRepository.save(building73);
        createEntrancesAndApartments(building73, 3);

        // Building 72А - 3 entrances
        Building building72A = new Building("72А", 3);
        buildingRepository.save(building72A);
        createEntrancesAndApartments(building72A, 3);
    }

    private void createEntrancesAndApartments(Building building, int entranceCount) {
        for (int entranceNum = 1; entranceNum <= entranceCount; entranceNum++) {
            Entrance entrance = new Entrance(entranceNum, building);
            entranceRepository.save(entrance);

            // Create 80 apartments per entrance (16 floors × 6 apartments per floor)
            int doorNumber = 1;
            for (int floor = 1; floor <= 16; floor++) {
                for (int aptOnFloor = 1; aptOnFloor <= 6; aptOnFloor++) {
                    Apartment apartment = new Apartment(doorNumber, floor, entrance);
                    apartmentRepository.save(apartment);
                    doorNumber++;
                }
            }
        }
    }

    private void initializeDefaultRankConfigurations() {
        // Default rank: "Хувалз" for amounts over 1,000,000 MNT - Red
        RankConfiguration huvalsRank = new RankConfiguration("Хувалз", new BigDecimal("1000000"), "#FF0000");
        huvalsRank.setDescription("Төлбөр 1 сая төгргөөс илүү айлууд");
        rankConfigurationRepository.save(huvalsRank);

        // Additional default ranks
        RankConfiguration highRisk = new RankConfiguration("Өндөр эрсдэлтэй", new BigDecimal("500000"), "#FF8C00");
        highRisk.setDescription("Төлбөр 500,000-999,999 төгрөг");
        rankConfigurationRepository.save(highRisk);

        RankConfiguration mediumRisk = new RankConfiguration("Дунд эрсдэлтэй", new BigDecimal("100000"), "#FFD700");
        mediumRisk.setDescription("Төлбөр 100,000-499,999 төгрөг");
        rankConfigurationRepository.save(mediumRisk);

        RankConfiguration normalRank = new RankConfiguration("Normal", new BigDecimal("0"), "#008000");
        normalRank.setDescription("Хэвийн төлбөр");
        rankConfigurationRepository.save(normalRank);
    }

    private void initializeHouseholdsAndPaymentData() {
        if (householdRepository.count() > 0) {
            return; // Data already initialized
        }

        Random random = new Random(42); // Fixed seed for consistent data
        List<Apartment> apartments = apartmentRepository.findAll();

        String[] mongolianNames = {
            "Батбаяр", "Оюунчимэг", "Төмөрбаатар", "Сарангэрэл", "Энхбаяр",
            "Цэцэгмаа", "Болдбаатар", "Алтанцэцэг", "Мөнхбаяр", "Наранцэцэг",
            "Гантөмөр", "Оюунтуяа", "Батсайхан", "Цэцэгдулам", "Энхтуяа",
            "Пүрэвбаатар", "Сарантуяа", "Мөнхтуяа", "Баттөмөр", "Цэцэгжаргал",
            "Баярсайхан", "Алтантуяа", "Гантулга", "Оюунбилэг", "Энхжаргал",
            "Төмөрсүх", "Сарангоо", "Мөнхсайхан", "Батжаргал", "Цэцэгсүрэн"
        };

        for (Apartment apartment : apartments) {
            // Create household for each apartment
            String householdName = mongolianNames[random.nextInt(mongolianNames.length)] + "-ийн гэр бүл";
            Household household = new Household(householdName, apartment);

            // Add some contact info randomly
            if (random.nextBoolean()) {
                household.setContactInfo("Утас: " + (88000000 + random.nextInt(10000000)));
            }

            householdRepository.save(household);

            // Create payment records for July, August, September 2024
            createPaymentRecordsForHousehold(household, random);
        }
    }

    private void createPaymentRecordsForHousehold(Household household, Random random) {
        // Create records for July, August, September 2024
        LocalDate[] months = {
            LocalDate.of(2024, 7, 1),
            LocalDate.of(2024, 8, 1),
            LocalDate.of(2024, 9, 1)
        };

        BigDecimal previousBalance = generateRandomBalance(random);

        for (LocalDate month : months) {
            // Simulate payment behavior - some pay, some don't, some accumulate more debt
            BigDecimal currentBalance = simulateMonthlyPaymentBehavior(previousBalance, random);

            PaymentRecord paymentRecord = new PaymentRecord(household, month, currentBalance);
            paymentRecordRepository.save(paymentRecord);

            previousBalance = currentBalance;
        }
    }

    private BigDecimal simulateMonthlyPaymentBehavior(BigDecimal previousBalance, Random random) {
        int behavior = random.nextInt(100);

        if (behavior < 30) {
            // 30% - Paid some amount, balance decreased
            BigDecimal payment = previousBalance.multiply(BigDecimal.valueOf(0.1 + random.nextDouble() * 0.8));
            BigDecimal newBalance = previousBalance.subtract(payment);
            return newBalance.max(BigDecimal.ZERO);
        } else if (behavior < 60) {
            // 30% - No payment, balance stayed same or increased slightly
            BigDecimal increase = BigDecimal.valueOf(random.nextInt(50000));
            return previousBalance.add(increase);
        } else {
            // 40% - New debt accumulated
            BigDecimal newDebt = generateRandomBalance(random).multiply(BigDecimal.valueOf(0.1 + random.nextDouble() * 0.3));
            return previousBalance.add(newDebt);
        }
    }

    private BigDecimal generateRandomBalance(Random random) {
        // Generate realistic payment balances with different risk levels
        int balanceType = random.nextInt(100);

        if (balanceType < 60) {
            // 60% - Normal range (0 - 99,999)
            return BigDecimal.valueOf(random.nextInt(100000));
        } else if (balanceType < 80) {
            // 20% - Medium risk (100,000 - 499,999)
            return BigDecimal.valueOf(100000 + random.nextInt(400000));
        } else if (balanceType < 95) {
            // 15% - High risk (500,000 - 999,999)
            return BigDecimal.valueOf(500000 + random.nextInt(500000));
        } else {
            // 5% - Very high risk "Хувалз" (1,000,000+)
            return BigDecimal.valueOf(1000000 + random.nextInt(2000000));
        }
    }
}
