package com.project;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * Aquest exemple mostra com fer una 
 * connexió a SQLite amb Java
 * 
 * A la primera crida, crea l'arxiu 
 * de base de dades hi posa dades,
 * després les modifica
 * 
 * A les següent crides ja estan
 * originalment modificades
 * (tot i que les sobreescriu cada vegada)
 */

public class Main {

    public static void main(String[] args) throws SQLException {
        String basePath = System.getProperty("user.dir") + "/data/";
        String filePath = basePath + "forhonor.db";
        ResultSet rs = null;

        File fDatabase = new File(filePath);
        if (!fDatabase.exists()) {
            initDatabase(filePath);
        }

        // Connectar (crea la BBDD si no existeix)
        Connection conn = UtilsSQLite.connect(filePath);

        // Llistar les taules
        ArrayList<String> taules = UtilsSQLite.listTables(conn);
        System.out.println(taules);

        Scanner scanner = new Scanner(System.in);

        int option;
        do {
            System.out.println("\n----- Menú For Honor -----");
            System.out.println("1. Mostrar una taula");
            System.out.println("2. Mostrar personatges per facció");
            System.out.println("3. Mostrar el millor atacant per facció");
            System.out.println("4. Mostrar el millor defensor per facció");
            System.out.println("5. Sortir");
            System.out.print("Selecciona una opció: ");
            option = scanner.nextInt();
            System.out.println();

            switch (option) {
                case 1:
                    showTable(conn, rs);
                    break;
                case 2:
                    showCharactersByFaction(conn, rs);
                    break;
                case 3:
                    showBestAttackerByFaction(conn, rs);
                    break;
                case 4:
                    showBestDefenderByFaction(conn, rs);
                    break;
                case 5:
                    System.out.println("Adéu! Fins aviat!");
                    break;
                default:
                    System.out.println("Opció no vàlida. Torna a provar.");
            }
        } while (option != 5);
        UtilsSQLite.disconnect(conn);
    }

    static void showTable(Connection conn, ResultSet rs) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Quina taula vols visualitzar?");
        System.out.println("1. Faccions");
        System.out.println("2. Personatges");
        System.out.print("Selecciona una tabla: ");
        int option = scanner.nextInt();
        System.out.println();
        switch (option) {
            case 1:
                try {
                    rs = UtilsSQLite.querySelect(conn, "SELECT * FROM faccions");
                    while (rs.next()) {
                        System.out.println(
                                "    " + rs.getInt("id") + ", " + rs.getString("name") + ", " + rs.getString("resum"));

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    rs = UtilsSQLite.querySelect(conn, "SELECT * FROM personatges");
                    while (rs.next()) {
                        System.out.println("    " + rs.getString("name") + ", " + rs.getDouble("atac") + ", "
                                + rs.getDouble("defensa") + ", " + rs.getInt("idFaccio"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Opció no vàlida");
        }
    }

    static void showCharactersByFaction(Connection conn, ResultSet rs) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Faccions disponibles:");
        showAvailableFactions(conn, rs);
        System.out.print("Selecciona una facció (introduïr l'id): ");

        int factionId = scanner.nextInt();
        scanner.nextLine();
        System.out.println();

        String factionName = getFactionNameById(conn, factionId);
        if (factionName == null) {
            System.out.println("Facció no trobada.");
            return;
        }

        try {
            rs = UtilsSQLite.querySelect(conn, "SELECT * FROM personatges WHERE idFaccio = " + factionId);
            System.out.println();
            System.out.println("Personatges de la facció '" + factionName + "':");

            while (rs.next()) {
                System.out.println("    " + rs.getString("name") + ", Atac: " + rs.getDouble("atac") + ", Defensa: "
                        + rs.getDouble("defensa"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void showBestAttackerByFaction(Connection conn, ResultSet rs) {
        Scanner scanner = new Scanner(System.in);

        showAvailableFactions(conn, rs);
        System.out.print("Selecciona una facció (introduïr l'id): ");
        int factionId = scanner.nextInt();
        scanner.nextLine();
        String factionName = getFactionNameById(conn, factionId);
        if (factionName == null) {
            System.out.println("Facció no trobada.");
            return;
        }
        try {
            rs = UtilsSQLite.querySelect(conn,
                    "SELECT * FROM personatges WHERE idFaccio = " + factionId + " ORDER BY atac DESC LIMIT 1");
            if (rs.next()) {
                System.out.println();
                System.out.println("Millor atacant de la facció '" + factionName + "':");
                System.out.println("    " + rs.getString("name") + ", Atac: " + rs.getDouble("atac") + ", Defensa: "
                        + rs.getDouble("defensa"));
            } else {
                System.out.println("No hi ha personatges en aquesta facció.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void showBestDefenderByFaction(Connection conn, ResultSet rs) {
        Scanner scanner = new Scanner(System.in);
        showAvailableFactions(conn, rs);
        System.out.print("Selecciona una facció (introduïr l'id): ");
        int factionId = scanner.nextInt();
        scanner.nextLine();
        String factionName = getFactionNameById(conn, factionId);
        if (factionName == null) {
            System.out.println("Facció no trobada.");
            return;
        }
        try {
            rs = UtilsSQLite.querySelect(conn,
                    "SELECT * FROM personatges WHERE idFaccio = " + factionId + " ORDER BY defensa DESC LIMIT 1");
            if (rs.next()) {
                System.out.println();
                System.out.println("Millor defensor de la facció '" + factionName + "':");
                System.out.println("    " + rs.getString("name") + ", Atac: " + rs.getDouble("atac") + ", Defensa: "
                        + rs.getDouble("defensa"));
            } else {
                System.out.println("No hi ha personatges en aquesta facció.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static String getFactionNameById(Connection conn, int factionId) {
        String factionName = null;
        try {
            ResultSet rs = UtilsSQLite.querySelect(conn, "SELECT name FROM faccions WHERE id = " + factionId);
            if (rs.next()) {
                factionName = rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factionName;
    }

    static void initDatabase(String filePath) {
        Connection conn = UtilsSQLite.connect(filePath);

        UtilsSQLite.queryUpdate(conn, "DROP TABLE IF EXISTS personatges;");
        UtilsSQLite.queryUpdate(conn, "CREATE TABLE IF NOT EXISTS personatges ("
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "    name VARCHAR(15) NOT NULL,"
                + "    atac REAL,"
                + "    defensa REAL,"
                + "    idFaccio INTEGER,"
                + "    FOREIGN KEY (idFaccio) REFERENCES faccions(id));");

        UtilsSQLite.queryUpdate(conn, "DROP TABLE IF EXISTS faccions;");
        UtilsSQLite.queryUpdate(conn, "CREATE TABLE IF NOT EXISTS faccions ("
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "    name VARCHAR(15),"
                + "    resum VARCHAR(500));");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO faccions (id, name, resum) VALUES (1, 'Caballeros', 'Facción valiente y noble que defiende con honor.');");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO faccions (id, name, resum) VALUES (2, 'Vikingos', 'Facción salvaje y fuerte que busca la gloria en la batalla.');");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO faccions (id, name, resum) VALUES (3, 'Samuráis', 'Facción disciplinada y hábil en el arte de la guerra.');");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO personatges (name, atac, defensa, idFaccio) VALUES ('Warden', 90.2, 80.5, " + 1 + ");");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO personatges (name, atac, defensa, idFaccio) VALUES ('Lawbringer', 85.7, 75.2, " + 1
                        + ");");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO personatges (name, atac, defensa, idFaccio) VALUES ('Peacekeeper', 88.6, 72.3, " + 1
                        + ");");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO personatges (name, atac, defensa, idFaccio) VALUES ('Raider', 82.4, 78.1, " + 2 + ");");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO personatges (name, atac, defensa, idFaccio) VALUES ('Warlord', 79.1, 74.3, " + 2 + ");");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO personatges (name, atac, defensa, idFaccio) VALUES ('Valkyrie', 84.7, 76.5, " + 2 + ");");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO personatges (name, atac, defensa, idFaccio) VALUES ('Kensei', 87.3, 68.5, " + 3 + ");");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO personatges (name, atac, defensa, idFaccio) VALUES ('Orochi', 83.6, 66.2, " + 3 + ");");
        UtilsSQLite.queryUpdate(conn,
                "INSERT INTO personatges (name, atac, defensa, idFaccio) VALUES ('Nobushi', 86.8, 70.1, " + 3 + ");");

        // Desconnectar
        UtilsSQLite.disconnect(conn);
    }

    static void showAvailableFactions(Connection conn, ResultSet rs) {
        System.out.println("Faccions disponibles:");
        try {
            rs = UtilsSQLite.querySelect(conn, "SELECT * FROM faccions");
            while (rs.next()) {
                System.out.println("    " + rs.getInt("id") + ". " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}