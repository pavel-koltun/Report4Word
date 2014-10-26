package local.koltun.reportfw.gui.resources;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizedResourceBundle {
    private static Logger logger = Logger.getLogger(LocalizedResourceBundle.class);
    private LocalizedResourceBundle() {}
    private static HashMap<String, String> words = new HashMap<>();

    private static class ResourceBundleHolder {
        private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages", new Locale("ru"));
    }

    public static String getStringFromBundle(String key) {
        String value = null;
        if (words.containsKey(key)) {
            value = words.get(key);
            logger.info("Return cached value: key=" + key + "; value=" + value);
        } else {
            try {
                value = new String(ResourceBundleHolder.RESOURCE_BUNDLE.getString(key).getBytes("ISO-8859-1"), "UTF-8");
                words.put(key, value);
            } catch (UnsupportedEncodingException e) {
                logger.error("Exception is:", e);
            }
        }
        return value;
    }
}
