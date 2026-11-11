package hiou.hicham.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entree du micro-service de gestion des comptes bancaires.
 * <p>
 * Projet realise dans le cadre du module Systemes Distribues et DevOps,
 * Master SDIA 2eme annee, ENSET Mohammedia.
 */
@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
