package io.github.java_lan_multiplayer.client;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Manages application language settings and localization.
 * Supports switching between locales and accessing localized messages.
 */
public class LanguageManager {

    private static Locale currentLocale = Locale.ENGLISH;
    private static Locale pendingLocale = currentLocale;
    private static ResourceBundle bundle = ResourceBundle.getBundle("lang.messages", currentLocale);

    /**
     * Sets the pending locale to be applied on the next update.
     * Does not immediately reload resources.
     *
     * @param locale the new locale to be used
     */
    public static void setPendingLocale(Locale locale) {
        pendingLocale = locale;
    }
    /**
     * Cycles between supported locales (English and Italian).
     * Updates the pending locale but does not apply it immediately.
     */
    public static void cycleLanguages() {
        if (pendingLocale.equals(Locale.ENGLISH)) {
            setPendingLocale(Locale.ITALIAN);
        } else {
            setPendingLocale(Locale.ENGLISH);
        }
    }

    /**
     * Applies the pending locale, if it differs from the current one.
     * This reloads the resource bundle to reflect the new language.
     */
    public static void applyPendingLocale() {
        if (pendingLocale != currentLocale) {
            currentLocale = pendingLocale;
            bundle = ResourceBundle.getBundle("lang.messages", currentLocale);
        }
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static Locale getPendingLocale() {
        return pendingLocale;
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Retrieves the localized string for the given key.
     *
     * @param key the message key
     * @return the localized message string
     */
    public static String get(String key) {
        return bundle.getString(key);
    }
    /**
     * Retrieves and formats the localized string for the given key using arguments.
     *
     * @param key the message key
     * @param args arguments to insert into the message
     * @return the formatted localized message
     */
    public static String get(String key, String... args) {
        return MessageFormat.format(LanguageManager.get(key), (Object[]) args);
    }
}
