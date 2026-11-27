package com.hrms.seed;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemoDataSeederTest {

    @Test
    void workingDaysSkipWeekends() {
        // January 2024 starts on a Monday: 31 days, 8 of them weekend days
        List<LocalDate> days = DemoDataSeeder.workingDays(YearMonth.of(2024, 1));

        assertThat(days).hasSize(23);
        assertThat(days).noneMatch(d -> d.getDayOfWeek() == DayOfWeek.SATURDAY
                || d.getDayOfWeek() == DayOfWeek.SUNDAY);
        assertThat(days.get(0)).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    void demoEmployeeHasEnoughWorkingDaysForTheSeededAbsences() {
        // the seeder picks working days at index 2, 3 and 9, so every month must have at least 10
        for (int month = 1; month <= 12; month++) {
            assertThat(DemoDataSeeder.workingDays(YearMonth.of(2026, month)).size()).isGreaterThan(9);
        }
    }
}
