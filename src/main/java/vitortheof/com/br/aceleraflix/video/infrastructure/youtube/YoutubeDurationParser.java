package vitortheof.com.br.aceleraflix.video.infrastructure.youtube;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class YoutubeDurationParser {

    private static final Pattern PADRAO = Pattern.compile("PT(?:([0-9]+)H)?(?:([0-9]+)M)?(?:([0-9]+)S)?");

    public static Integer paraSegundos(String duracaoIso8601) {
        if (duracaoIso8601 == null || duracaoIso8601.isBlank()) {
            return null;
        }
        Matcher matcher = PADRAO.matcher(duracaoIso8601);
        if (!matcher.matches()) {
            return null;
        }
        int horas = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 0;
        int minutos = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
        int segundos = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
        return horas * 3600 + minutos * 60 + segundos;
    }

}