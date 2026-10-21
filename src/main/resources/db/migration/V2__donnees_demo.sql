-- Jeu de données de démonstration : 2 artistes, 2 salles, 3 concerts, 2 clients.

INSERT INTO artiste (nom, genre_musical) VALUES
    ('Les Rives du Rhin', 'Folk'),
    ('Nova Pulse', 'Électro');

INSERT INTO salle (nom, ville, capacite) VALUES
    ('Le Petit Théâtre', 'Nancy', 50),
    ('Zénith Grand Est', 'Strasbourg', 5000);

INSERT INTO concert (titre, artiste_id, salle_id, date_heure, prix_unitaire)
SELECT 'Soirée folk intimiste', a.id, s.id, '2027-03-12 20:30:00', 25.00
FROM artiste a, salle s WHERE a.nom = 'Les Rives du Rhin' AND s.nom = 'Le Petit Théâtre';

INSERT INTO concert (titre, artiste_id, salle_id, date_heure, prix_unitaire)
SELECT 'Nova Pulse - Tournée 2027', a.id, s.id, '2027-04-03 21:00:00', 50.00
FROM artiste a, salle s WHERE a.nom = 'Nova Pulse' AND s.nom = 'Zénith Grand Est';

INSERT INTO concert (titre, artiste_id, salle_id, date_heure, prix_unitaire)
SELECT 'Rives du Rhin en plein air', a.id, s.id, '2027-06-20 18:00:00', 35.00
FROM artiste a, salle s WHERE a.nom = 'Les Rives du Rhin' AND s.nom = 'Zénith Grand Est';

-- Ids explicites : le compte de démonstration « client » est rattaché au client 1
-- (constante ComptesDemonstration.CLIENT_ID_DU_COMPTE_CLIENT, côté sécurité).
INSERT INTO client (id, nom, email, adresse_rue, adresse_ville) VALUES
    (1, 'Camille Martin', 'camille.martin@example.org', '12 rue des Lilas', 'Nancy'),
    (2, 'Yanis Benali', 'yanis.benali@example.org', '5 avenue de la Gare', 'Saint-Dié-des-Vosges');
