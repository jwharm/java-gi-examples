package io.github.jwharm.javagi.examples.logging;

import org.gnome.glib.GLib;
import org.gnome.glib.LogField;
import org.gnome.glib.LogLevelFlags;
import org.gnome.glib.LogWriterFunc;
import org.gnome.glib.LogWriterOutput;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.lang.foreign.MemorySegment;
import java.util.Set;

/**
 * Example GLogWriteFunc implementation that outputs GLib logging to SLF4J.
 * <p>
 * To be used with {@link GLib#logSetWriterFunc}.
 * <p>
 * Libraries <strong>must not</strong> use this class — only programs are
 * allowed to install a GLib log writer function, as there must be a single,
 * central point where log messages are formatted and outputted.
 */
public final class SLF4JLogWriterFunc implements LogWriterFunc {

    @Override
    public LogWriterOutput run(Set<LogLevelFlags> flags, LogField[] logFields) {
        try {
            String domain = readStringFromKey("GLIB_DOMAIN", logFields);
            String message = readStringFromKey("MESSAGE", logFields);
            Level level = convertLevel(flags.iterator().next());
            LoggerFactory.getLogger(domain).atLevel(level).log(message);
            return LogWriterOutput.HANDLED;
        } catch (Exception e) {
            return LogWriterOutput.UNHANDLED;
        }
    }

    // Map GLib log level to SLF4J log level
    private static Level convertLevel(LogLevelFlags flag) {
        return switch (flag) {
            case LEVEL_CRITICAL, LEVEL_ERROR -> Level.ERROR;
            case LEVEL_WARNING -> Level.WARN;
            case LEVEL_MESSAGE, LEVEL_INFO -> Level.INFO;
            case LEVEL_DEBUG -> Level.DEBUG;
            default -> throw new IllegalArgumentException(
                    "Unsupported LogLevelFlag: " + flag);
        };
    }

    // Find the field with the requested key and return the String value
    private static String readStringFromKey(String key, LogField[] logFields) {
        for (var field : logFields) {
            String k = field.readKey();
            if (k.equals(key))
                return readString(field);
        }
        throw new IllegalArgumentException("Cannot find key " + key);
    }

    // Read a String value from a field
    private static String readString(LogField field) {
        long length = field.readLength();
        MemorySegment value = field.readValue();
        if (length == -1) {
            value = value.reinterpret(Long.MAX_VALUE);
            return value.getString(0);
        } else {
            value = value.reinterpret(length);
            return value.getString(0);
        }
    }
}
