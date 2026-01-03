package fake.justLog.manager;

import fake.justLog.JustLog;
import org.bukkit.configuration.file.FileConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static fake.justLog.manager.TextManager.doDebug;

public class ConfigManager {

    private final JustLog plugin;

    private final File logFolder;
    private final File configFolder;

    public static int Debug = 1;
    public static String Database = "text";

    public ConfigManager(JustLog plugin) {
        this.plugin = plugin;
        this.configFolder = plugin.getDataFolder();
        this.logFolder = new File(configFolder, "logs");

        loadConfig();
    }

    public void loadConfig() {

        doDebug("  &eLoading Config...",1);

        if (!configFolder.exists() && configFolder.mkdirs()) {
            doDebug("  &cFolder Created!",1);
        }
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveDefaultConfig();
            doDebug("  &cConfig Created!",1);
        }

        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();

        Debug = config.getInt("Config.Debug", 1);
        Database = config.getString("Config.Database", "text");

        doDebug("Setting Loaded...",1);

    }


    public void SaveInFile(String fileName, String message) {

        message = message.replace("%value%", "");

        switch (Database) {
            case "text", "txt":
                saveToText(fileName,message);
                break;
            case "json":
                saveToJson(fileName,message);
                break;
            case "csv":
                saveToCsv(fileName,message);
                break;
            case "yaml", "yml":
                saveToYaml(fileName,message);
                break;
            case "sqlite":
                saveToSqlite(message);
                break;
//            case "mysql":
//                saveToText(fileName,message);
//                break;

            default:
                saveToText(fileName,message);
                break;
        }

    }
    public void saveToText(String fileName, String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateFolder = dateFormat.format(new Date());
        File dateDir = new File(logFolder, dateFolder);

        if (!dateDir.exists() && !dateDir.mkdirs()) {
            doDebug("&cError Creating Date Folder!", 1);
            return;
        }

        File logFile = new File(dateDir, fileName + ".txt");
        try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
            String timestamp = getTimestamp();

            out.println("[" + timestamp + "] " + message);
        } catch (IOException e) {
            doDebug("&cError Saving To Files: " + e.getMessage(), 0);
        }
    }
    public void saveToYaml(String fileName, String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateFolder = dateFormat.format(new Date());
        File dateDir = new File(logFolder, dateFolder);

        if (!dateDir.exists() && !dateDir.mkdirs()) {
            doDebug("&cError Creating Date Folder!", 1);
            return;
        }
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Use block style for better readability
        Yaml yaml = new Yaml(options);

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put(getTimestamp(), message);

        File logFile = new File(dateDir, fileName + ".yaml");

        try (FileWriter writer = new FileWriter(logFile, true)) {
            yaml.dump(logEntry, writer);
        } catch (IOException e) {
            doDebug("&cError Saving to YAML file: " + e.getMessage(), 0);
        }
    }
    public void saveToJson(String fileName, String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateFolder = dateFormat.format(new Date());
        File dateDir = new File(logFolder, dateFolder);

        if (!dateDir.exists() && !dateDir.mkdirs()) {
            doDebug("&cError Creating Date Folder!", 1);
            return;
        }
        File logFile = new File(dateDir, fileName + ".json");
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

        try {
            boolean exists = logFile.exists() && logFile.length() > 0;
            StringBuilder json = new StringBuilder();

            if (exists) {
                String content = new String(Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8);
                content = content.trim();
                json.append(content, 0, content.length() - 1).append(",");
            } else {
                json.append("[");
            }

            json.append("{")
                    .append("\"timestamp\":\"").append(timestamp).append("\",")
                    .append("\"message\":\"").append(message.replace("\"", "\\\"")).append("\"")
                    .append("}]");

            Files.write(logFile.toPath(), json.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            doDebug("&cError Saving to JSON file", 0);
        }
    }
    public void saveToCsv(String fileName, String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateFolder = dateFormat.format(new Date());
        File dateDir = new File(logFolder, dateFolder);

        if (!dateDir.exists() && !dateDir.mkdirs()) {
            doDebug("&cError Creating Date Folder!", 1);
            return;
        }
        File logFile = new File(dateDir, fileName + ".csv");

        try (FileWriter writer = new FileWriter(logFile, true)) {
            String timestamp = getTimestamp();

            // If it's the first line, write the header
            if (logFile.length() == 0) {
                writer.append("timestamp,message\n");
            }

            // Write the log entry
            writer.append(timestamp + "," + message.replace(",", "\\,") + "\n");
        } catch (IOException e) {
            doDebug("&cError Saving to CSV file", 0);
        }
    }

    public void saveToSqlite(String message) {
        String dbPath = "logs.db";
        File dbFile = new File(dbPath);

        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                doDebug("&cError creating SQLite database file: " + e.getMessage(), 0);
                return;
            }
        }
        String dbUrl = "jdbc:sqlite:logs.db";

        String createTableSql = """
        CREATE TABLE IF NOT EXISTS logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            timestamp TEXT NOT NULL,
            message TEXT NOT NULL
        )
        """;

        String sql = "INSERT INTO logs (timestamp, message) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSql);
                }

                String timestamp = getTimestamp();
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, timestamp);
                    pstmt.setString(2, message);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            doDebug("&cError Saving to Database: " + e.getMessage(), 0);
        }
    }
    private String getTimestamp() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
}
