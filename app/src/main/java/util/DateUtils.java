package util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.Timestamp;

import org.ocpsoft.prettytime.PrettyTime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilidades para trabajar con fechas.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class DateUtils {

    public static final String DEFAULT_DATETIME_PATTERN = "dd/MM/yyyy HH:mm:ss";
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Madrid");
    private static PrettyTime prettyTime = new PrettyTime();

    /**
     * Convierte una fecha en formato TimeStamp de FireStore a LocalDateTime de Java.
     *
     * @param timestamp
     * @return
     */
    public static LocalDateTime convertTimeStampToLocalDateTime(Timestamp timestamp) {
        ZoneId zone = ZoneOffset.systemDefault();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000), zone);
    }

    /**
     * Convierte una fecha en formato String a formato LocalDateTime.
     *
     * @param date
     * @return
     */
    public static LocalDateTime convertStringToLocalDateTime(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
        return LocalDateTime.parse(date, formatter);
    }

    public static String prettyDate(String dateTimeInStampFormat) {
        prettyTime.setLocale(new Locale("es"));
        String resPrettyDate = prettyTime.format(convertStringToLocalDateTime(dateTimeInStampFormat), DEFAULT_ZONE_ID);
        return resPrettyDate.substring(0, 1).toUpperCase()
                .concat(resPrettyDate.substring(1));
    }
}
