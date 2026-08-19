package vitortheof.com.br.aceleraflix.video.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vitortheof.com.br.aceleraflix.video.domain.Tag;
import vitortheof.com.br.aceleraflix.video.infrastructure.TagRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class TagResolver {

    private final TagRepository tagRepository;

    public Set<Tag> resolver(List<String> nomesTags) {
        if (nomesTags.isEmpty()) {
            return new HashSet<>();
        }

        return nomesTags.stream()
                .map(String::trim)
                .filter(nome -> !nome.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .map(this::searchOrCreate)
                .collect(Collectors.toSet());
    }

    //Se já exisiter so vai buscar, se não vai salvar uma nova tag
    private Tag searchOrCreate(String tag) {
        return tagRepository.findByNome(tag)
                .orElseGet(() -> tagRepository.save(Tag.builder().nome(tag).build()));
    }
}
