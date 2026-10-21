-- Schéma initial de la billetterie. Hibernate ne crée rien : il valide seulement (ddl-auto=validate).

CREATE TABLE artiste (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nom           VARCHAR(120) NOT NULL,
    genre_musical VARCHAR(80),
    CONSTRAINT pk_artiste PRIMARY KEY (id),
    CONSTRAINT uk_artiste_nom UNIQUE (nom)
) ENGINE = InnoDB;

CREATE TABLE salle (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    nom      VARCHAR(120) NOT NULL,
    ville    VARCHAR(80)  NOT NULL,
    capacite INT          NOT NULL,
    CONSTRAINT pk_salle PRIMARY KEY (id),
    CONSTRAINT uk_salle_nom_ville UNIQUE (nom, ville),
    CONSTRAINT ck_salle_capacite CHECK (capacite BETWEEN 50 AND 50000)
) ENGINE = InnoDB;

CREATE TABLE concert (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    titre         VARCHAR(160)  NOT NULL,
    artiste_id    BIGINT        NOT NULL,
    salle_id      BIGINT        NOT NULL,
    date_heure    DATETIME(6)   NOT NULL,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_concert PRIMARY KEY (id),
    CONSTRAINT fk_concert_artiste FOREIGN KEY (artiste_id) REFERENCES artiste (id),
    CONSTRAINT fk_concert_salle FOREIGN KEY (salle_id) REFERENCES salle (id),
    CONSTRAINT ck_concert_prix CHECK (prix_unitaire > 0)
) ENGINE = InnoDB;
CREATE INDEX idx_concert_date_heure ON concert (date_heure);

CREATE TABLE client (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nom           VARCHAR(120) NOT NULL,
    email         VARCHAR(190) NOT NULL,
    adresse_rue   VARCHAR(160) NOT NULL,
    adresse_ville VARCHAR(80)  NOT NULL,
    CONSTRAINT pk_client PRIMARY KEY (id),
    CONSTRAINT uk_client_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE carte_fidelite (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    client_id   BIGINT      NOT NULL,
    achetee_le  DATETIME(6) NOT NULL,
    expire_le   DATETIME(6) NOT NULL,
    CONSTRAINT pk_carte_fidelite PRIMARY KEY (id),
    CONSTRAINT fk_carte_client FOREIGN KEY (client_id) REFERENCES client (id),
    CONSTRAINT ck_carte_duree CHECK (expire_le > achetee_le)
) ENGINE = InnoDB;
CREATE INDEX idx_carte_client_dates ON carte_fidelite (client_id, achetee_le, expire_le);

CREATE TABLE achat_billets (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    code          VARCHAR(40)   NOT NULL,
    concert_id    BIGINT        NOT NULL,
    client_id     BIGINT        NOT NULL,
    quantite      INT           NOT NULL,
    montant_total DECIMAL(10,2) NOT NULL,
    cree_le       DATETIME(6)   NOT NULL,
    CONSTRAINT pk_achat_billets PRIMARY KEY (id),
    CONSTRAINT uk_achat_code UNIQUE (code),
    CONSTRAINT fk_achat_concert FOREIGN KEY (concert_id) REFERENCES concert (id),
    CONSTRAINT fk_achat_client FOREIGN KEY (client_id) REFERENCES client (id),
    CONSTRAINT ck_achat_quantite CHECK (quantite BETWEEN 1 AND 6),
    CONSTRAINT ck_achat_montant CHECK (montant_total >= 0)
) ENGINE = InnoDB;
CREATE INDEX idx_achat_concert ON achat_billets (concert_id);
