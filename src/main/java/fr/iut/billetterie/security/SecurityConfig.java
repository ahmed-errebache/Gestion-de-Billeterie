package fr.iut.billetterie.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * API sans état en HTTP Basic : lectures, inscription, documentation et santé publiques,
 * opérations de production et d'administration protégées par rôle.
 * La sécurité par méthode (@PreAuthorize) vérifie les règles qui dépendent du corps de la requête,
 * comme le client pour lequel un compte CLIENT achète.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, ControleAccesClient controleAccesClient)
            throws Exception {
        http
                // API sans session ni cookie : le jeton CSRF n'apporte rien et bloquerait tous les POST
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/concerts", "/api/concerts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/clients").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/", "/concerts", "/concerts/**", "/inscription", "/inscription/**", "/css/**").permitAll()
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
