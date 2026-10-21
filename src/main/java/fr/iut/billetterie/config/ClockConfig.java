package fr.iut.billetterie.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge injectable : les règles de dates (carte de fidélité, concert futur)
 * se testent en remplaçant ce bean par une horloge fixe, sans jamais utiliser sleep.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
