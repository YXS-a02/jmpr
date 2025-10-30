package yxs.a02.mper.util;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

public class ResourceBundleManager {
    private static ResourceBundle bundle;

    static {
        try {
            bundle = ResourceBundle.getBundle("messages", new Locale("zh", "CN"));
        } catch (MissingResourceException e) {
            System.err.println("无法加载资源文件，使用默认文本: " + e.getMessage());
            // 使用空的ResourceBundle作为后备
            bundle = new ResourceBundle() {
                @Override
                protected Object handleGetObject(String key) {
                    return key; // 返回键本身作为默认值
                }

                @Override
                public java.util.Enumeration<String> getKeys() {
                    return java.util.Collections.emptyEnumeration();
                }
            };
        }
    }

    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key; // 如果找不到键，返回键本身
        }
    }

    public static void setLocale(Locale locale) {
        try {
            bundle = ResourceBundle.getBundle("messages", locale);
        } catch (MissingResourceException e) {
            System.err.println("无法加载资源文件: " + e.getMessage());
        }
    }
}