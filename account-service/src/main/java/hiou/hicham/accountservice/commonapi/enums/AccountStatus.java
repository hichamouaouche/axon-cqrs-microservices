package hiou.hicham.accountservice.commonapi.enums;

/**
 * Cycle de vie d'un compte bancaire.
 * CREATED   : le compte vient d'etre cree, aucune operation n'est encore autorisee.
 * ACTIVATED : le compte est actif, les operations de credit et de debit sont autorisees.
 * SUSPENDED : le compte est suspendu, les operations sont refusees.
 * CLOSED    : le compte est definitivement ferme.
 */
public enum AccountStatus {
    CREATED,
    ACTIVATED,
    SUSPENDED,
    CLOSED
}
