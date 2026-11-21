package com.campusconnect.analytics.util;

import com.campusconnect.analytics.dto.SalaryPackageStats;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SalaryPackageParserTest {

    @Test
    void parseLpa_SingleLpa_ReturnsParsedValue() {
        Optional<Double> val1 = SalaryPackageParser.parseLpa("14.5 LPA");
        assertTrue(val1.isPresent());
        assertEquals(14.5, val1.get());

        Optional<Double> val2 = SalaryPackageParser.parseLpa("8 LPA CTC");
        assertTrue(val2.isPresent());
        assertEquals(8.0, val2.get());

        Optional<Double> val3 = SalaryPackageParser.parseLpa("12 Lakhs");
        assertTrue(val3.isPresent());
        assertEquals(12.0, val3.get());
    }

    @Test
    void parseLpa_RangeLpa_ReturnsAverageOfRange() {
        Optional<Double> val = SalaryPackageParser.parseLpa("10 - 14 LPA");
        assertTrue(val.isPresent());
        assertEquals(12.0, val.get());

        Optional<Double> val2 = SalaryPackageParser.parseLpa("6 to 8 Lakhs");
        assertTrue(val2.isPresent());
        assertEquals(7.0, val2.get());
    }

    @Test
    void parseLpa_MonthlySalary_ConvertsToAnnualLpa() {
        Optional<Double> val = SalaryPackageParser.parseLpa("50,000 / month");
        assertTrue(val.isPresent());
        assertEquals(6.0, val.get());

        Optional<Double> val2 = SalaryPackageParser.parseLpa("40000 pm");
        assertTrue(val2.isPresent());
        assertEquals(4.8, val2.get());
    }

    @Test
    void parseLpa_RawNumber_ConvertsToLpa() {
        Optional<Double> val = SalaryPackageParser.parseLpa("800000");
        assertTrue(val.isPresent());
        assertEquals(8.0, val.get());

        Optional<Double> val2 = SalaryPackageParser.parseLpa("12");
        assertTrue(val2.isPresent());
        assertEquals(12.0, val2.get());
    }

    @Test
    void parseLpa_InvalidOrBlank_ReturnsEmpty() {
        assertTrue(SalaryPackageParser.parseLpa(null).isEmpty());
        assertTrue(SalaryPackageParser.parseLpa("").isEmpty());
        assertTrue(SalaryPackageParser.parseLpa("   ").isEmpty());
        assertTrue(SalaryPackageParser.parseLpa("Competitive / Best in Industry").isEmpty());
    }

    @Test
    void computeStats_NullOrEmptyList_ReturnsEmptyStats() {
        SalaryPackageStats stats1 = SalaryPackageParser.computeStats(null);
        assertNull(stats1.getHighestPackageLpa());
        assertEquals(0, stats1.getTotalAnalyzedOffers());

        SalaryPackageStats stats2 = SalaryPackageParser.computeStats(List.of());
        assertNull(stats2.getHighestPackageLpa());
        assertEquals(0, stats2.getTotalAnalyzedOffers());
    }

    @Test
    void computeStats_ValidList_ComputesHighestAverageMedianAndTiers() {
        List<String> rawSalaries = List.of(
                "4.5 LPA",
                "8.0 LPA",
                "12.0 LPA",
                "15.5 LPA",
                "20.0 LPA"
        );

        SalaryPackageStats stats = SalaryPackageParser.computeStats(rawSalaries);

        assertNotNull(stats);
        assertEquals(20.0, stats.getHighestPackageLpa());
        assertEquals(12.0, stats.getMedianPackageLpa());
        assertEquals(12.0, stats.getAveragePackageLpa()); // (4.5 + 8 + 12 + 15.5 + 20) / 5 = 60 / 5 = 12.0
        assertEquals(3, stats.getTierCountAbove10Lpa()); // 12, 15.5, 20
        assertEquals(1, stats.getTierCount6To10Lpa());   // 8.0
        assertEquals(1, stats.getTierCountBelow6Lpa());  // 4.5
        assertEquals(5, stats.getTotalAnalyzedOffers());
    }

    @Test
    void computeStats_EvenNumberOfOffers_ComputesMedianAverage() {
        List<String> rawSalaries = List.of(
                "6.0 LPA",
                "10.0 LPA"
        );

        SalaryPackageStats stats = SalaryPackageParser.computeStats(rawSalaries);

        assertNotNull(stats);
        assertEquals(10.0, stats.getHighestPackageLpa());
        assertEquals(8.0, stats.getAveragePackageLpa());
        assertEquals(8.0, stats.getMedianPackageLpa()); // (6 + 10) / 2 = 8.0
    }
}
