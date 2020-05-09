package tool.common.config;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author kober
 * @date created on 2020/5/8
 */
public class ConfigFile {
    private static final Logger logger = LoggerFactory.getLogger(ConfigFile.class);
    private final String name;
    private final Path localPath;
    private Map<String, String> items = Collections.emptyMap();
    private boolean parsed = false;
    private static final byte[] NULL_BYTES = new byte[0];

    public ConfigFile(String path, String name) {
        this.name = name;
        this.localPath = Paths.get(scanRootPath(path), name);
    }

    private static String scanRootPath(String resource) {
        try {
            Enumeration ps = Thread.currentThread().getContextClassLoader().getResources(resource);

            while(ps.hasMoreElements()) {
                URL url = (URL)ps.nextElement();
                String s = url.toString();
                if (s.startsWith("file:/")) {
                    String os_name = System.getProperty("os.name");
                    if (os_name != null && os_name.toLowerCase().contains("windows")) {
                        return s.substring(6);
                    }

                    return s.substring(5);
                }
            }
        } catch (IOException var5) {
            logger.error("scan root path", var5);
        }

        return "";
    }

    public byte[] getContent() {
        try {
            return Files.readAllBytes(localPath);
        } catch (NoSuchFileException ignored) {
            logger.error("NotFound {}", localPath);
        } catch (IOException e) {
            logger.error("CannotRead {}", localPath, e);
        }
        return NULL_BYTES;
    }

    public String getString(Charset charset) {
        byte[] data = getContent();
        if (data == null) {
            return null;
        }
        return new String(data, charset);
    }

    public List<String> getLines(Charset charset, boolean removeComment) {
        List<String> raw = Splitter.on('\n').trimResults().omitEmptyStrings().splitToList(getString(charset));
        if (!removeComment) return raw;

        List<String> clean = Lists.newArrayList();
        for (String i : raw) {
            if (i.charAt(0) == '#' || i.startsWith("//")) continue;
            clean.add(i);
        }
        return clean;
    }

    public List<String> getLines() {
        return getLines(StandardCharsets.UTF_8, true);
    }

    public String get(String key) {
        if (!parsed) {
            synchronized (this) {
                if (!parsed) {
                    Map<String, String> m = Maps.newHashMap();
                    for (String i : getLines()) {
                        int pos = i.indexOf('=');
                        if (pos != -1 && (pos + 1) < i.length()) {
                            m.put(i.substring(0, pos).trim(), i.substring(pos + 1).trim());
                        }
                    }
                    items.clear();
                    items = m;
                    parsed = true;
                }
            }
        }
        return items.get(key);
    }

    public int getInt(String key, int defaultVal) {
        String val = get(key);
        if (!Strings.isNullOrEmpty(val)) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultVal;
    }
}
