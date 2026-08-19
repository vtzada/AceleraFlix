package vitortheof.com.br.aceleraflix.video.infrastructure.youtube;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class YoutubeUrlParser {

    //regex para mapear os formatos comuns do ytb, isola o id de 11 caracteres no gp de captura 1
    private static final Pattern PADRAO = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?v=|embed/|shorts/)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
    );

    //extrai o id de um video do ytb
    public static Optional<String> extrairVideoId(String url) {
        if (url == null || url.isEmpty()) {
            return Optional.empty();
        }
        Matcher matcher = PADRAO.matcher(url);
        //group(1) captura apenas oq esta dentro dos parênteses da regex
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

}
