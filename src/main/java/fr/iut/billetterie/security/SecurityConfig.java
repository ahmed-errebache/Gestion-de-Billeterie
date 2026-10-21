package fr.iut.billetterie.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Deux chaînes de sécurité, une par type de client :
 * <ul>
 *   <li>API, documentation et Actuator : sans état en HTTP Basic, CSRF inutile (aucune session ni cookie) ;</li>
 *   <li>pages HTML : connexion par formulaire et session, donc CSRF actif sur tous les formulaires.</li>
 * </ul>
 * La sécurité par méthode (@PreAuthorize) vérifie les règles qui dépendent du corps de la requête,
 * comme le client pour lequel un compte CLIENT achète.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, ControleAccesClient controleAccesClient)
            throws Exception {
        http
                .securityMatcher("/api/**", "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**",
                        "/actuator/**")
                // API sans session ni cookie : le jeton CSRF n'apporte rien et bloquerait tous les POST
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/concerts", "/api/concerts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/clients").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/production/**").hasAnyRole("PRODUCTEUR", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/clients/{id}/carte-fidelite").access((authentification, contexte) ->
                                new AuthorizationDecision(controleAccesClient.peutAgirPour(
                                        authentification.get(), idClient(contexte.getVariables().get("id")))))
                        .requestMatchers(HttpMethod.POST, "/api/achats").hasRole("CLIENT")
                        .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain pagesSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/", "/concerts", "/concerts/*").permitAll()
                        .requestMatchers("/inscription", "/inscription/**", "/connexion", "/css/**", "/error").permitAll()
                        // Achat de billets et de carte : réservé aux comptes rattachés à un client métier
                        .requestMatchers(HttpMethod.POST, "/concerts/*/achat").hasRole("CLIENT")
                        .requestMatchers("/achats/**", "/espace", "/espace/**").hasRole("CLIENT")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/connexion")
                        .defaultSuccessUrl("/concerts")
                        .failureUrl("/connexion?erreur")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/deconnexion")
                        .logoutSuccessUrl("/concerts?deconnecte")
                        .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static Long idClient(String valeur) {
        try {
            return Long.valueOf(valeur);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
