package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.MissionRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepo, MissionRepository missionRepo, PasswordEncoder encoder) {
        return args -> {
            User admin = userRepo.findByEmail("admin@sidequest.it").orElseGet(() ->
                userRepo.save(new User("admin@sidequest.it", encoder.encode("Admin123!"), "AdminSQ", Role.ADMIN))
            );
            User mario = userRepo.findByEmail("mario@sidequest.it").orElseGet(() ->
                userRepo.save(new User("mario@sidequest.it", encoder.encode("Mario123!"), "MarioExplorer", Role.USER))
            );

            if (missionRepo.count() == 0) {
                Mission m1 = new Mission(
                    "Passeggiata a Castel dell'Ovo",
                    "Esplora il lungomare di Napoli e completa una breve sfida fotografica al tramonto.",
                    MissionCategory.PHOTOGRAPHY,
                    MissionType.ORDINARY,
                    null,
                    150,
                    "Nessun prerequisito richiesto",
                    40.8282, 14.2476, "Lungomare Caracciolo, Napoli",
                    mario
                );
                m1.addObjective("Raggiungi l'ingresso del castello", "Descrivi cosa vedi all'arco d'ingresso", 1, false);
                m1.addObjective("Scatta una foto al panorama", "Carica una breve descrizione o immagine della vista", 2, true);
                m1.publish();
                missionRepo.save(m1);

                Mission m2 = new Mission(
                    "L'Enigma di Piazza Plebiscito",
                    "Una missione segreta a tempo! Accettala per scoprire le tappe e risolverla in 20 minuti.",
                    MissionCategory.ENIGMA,
                    MissionType.SECRET_TIMED,
                    20,
                    300,
                    "Solo per utenti con spirito d'osservazione",
                    40.8359, 14.2488, "Piazza del Plebiscito, Napoli",
                    admin
                );
                m2.addObjective("Conta le colonne del colonnato", "Inserisci il numero corretto nella prova", 1, true);
                m2.addObjective("Trova le due statue equestri", "Indica i nomi dei sovrani rappresentati", 2, true);
                m2.publish();
                missionRepo.save(m2);
            }
        };
    }
}