package com.graphhopper;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FakerSmokeTest {
    
    @Test
    void fakerGeneratesNonEmptyValues() {
        Faker faker = new Faker();
        String city = faker.address().city();
        String road = faker.address().streetName();
        assertAll(
            () -> assertNotNull(city),
            () -> assertFalse(city.isBlank()),
            () -> assertNotNull(road),
            () -> assertFalse(road.isBlank())
        );
    }
}
