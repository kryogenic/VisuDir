package org.kryogenic.visudir;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;
import org.kryogenic.visudir.wrappers.VisuPath;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author: Kale
 * @date: 12/10/13
 */
public class Config {
    private static final File CONF_FILE = new File(VisuDir.VISU_HOME + VisuDir.FS + "VisuDir.yml");
    private static final YamlConfig YAML_CONFIG = new YamlConfig();
    static {
        YAML_CONFIG.setPrivateFields(true);
        YAML_CONFIG.writeConfig.setAlwaysWriteClassname(true);
        YAML_CONFIG.setScalarSerializer(VisuPath.class, new ScalarSerializer() {
            @Override
            public String write(Object object) throws YamlException {
                if(object instanceof VisuPath) {
                    return object.toString();
                } else {
                    throw new YamlException("not a visupath?");
                }
            }

            @Override
            public Object read(String value) throws YamlException {
                return new VisuPath(value);
            }
        });
    }
    private static final Config CONF = new Config();

    static {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    saveConfig();
                    try {
                        Thread.sleep(90000);
                    } catch(InterruptedException ignored) {}
                }
            }
        }).start();
    }

    private HashMap<String, Object> config;
    private boolean synced = false; // synced with the disk

    public Config() {
        if(canWrite()) {
            FileReader configReader;
            try {
                configReader = new FileReader(CONF_FILE);
                this.config = new YamlReader(configReader, YAML_CONFIG).read(HashMap.class);
            } catch (FileNotFoundException ignored) { ignored.printStackTrace(); // can't happen
            } catch (YamlException e) { e.printStackTrace(); // config file is invalid... most likely the user messed with it ._.
                // copy the current config to a backup and delete the old one
                int i = 0;
                File configBackup;
                do {
                    configBackup = new File(CONF_FILE.getAbsolutePath() + VisuDir.FS + ".bak." + i);
                    i++;
                } while (!configBackup.exists());
                try {
                    Files.copy(CONF_FILE.toPath(), configBackup.toPath());
                    CONF_FILE.delete();
                } catch(IOException e1) {e1.printStackTrace();} // well, we tried.
            }
        }
        validate();
    }

    private void validate() {
        if(config == null)
            config = new HashMap<>();
        if(!(config.get("ColorScheme") instanceof String)) {
            config.put("ColorScheme", "Classic");
        }

        if(!(config.get("Focus") instanceof VisuPath)) {
            config.put("Focus", new VisuPath(VisuDir.HOME));
        }

        if(config.get("NumViews") instanceof String) {
            try {
                config.put("NumViews", Integer.valueOf((String)config.get("NumViews")));
            } catch (NumberFormatException e) {
                config.put("NumViews", 3);
            }
        } else {
            config.put("NumViews", 3);
        }

        if(config.get("SI") instanceof String) {
            config.put("SI", Boolean.valueOf((String)config.get("SI")));
        } else {
            config.put("SI", false);
        }

        if(config.get("ViewHidden") instanceof String) {
            config.put("ViewHidden", Boolean.valueOf((String)config.get("ViewHidden")));
        } else {
            config.put("ViewHidden", false);
        }
    }

    public static boolean canWrite() {
        return CONF_FILE.exists();
    }

    public static Object get(String key) {
        return CONF.config.get(key);
    }

    public static <T> T get(String key, Class<T> type) {
        return (T) get(key);
    }

    public static void saveConfig() {
        if(!CONF.synced && canWrite()) {
            try {
                YamlWriter writer = new YamlWriter(new FileWriter(CONF_FILE), YAML_CONFIG);
                writer.write(CONF.config);

                writer.close();
                CONF.synced = true;
            } catch(IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    public static <T> T put(String key, T value) {
        T newValue = (T) CONF.config.put(key, value);
        if(CONF.synced) {
            T oldValue = (T) get(key, value.getClass());
            if(!oldValue.equals(newValue)) {
                CONF.synced = false;
            }
        }
        System.out.println(key + ": " + newValue);
        return newValue;
    }

    public static void touchConfig() {
        try {
            CONF_FILE.getParentFile().mkdirs();
            CONF_FILE.createNewFile();
        } catch(IOException ignored) {}
    }
}
