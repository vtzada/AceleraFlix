package vitortheof.com.br.aceleraflix.video.application.dto;

/**
 * Resumo das avaliações de um vídeo. Os mesmos três campos são repetidos em {@link VideoResponse}.
 *
 * @param mediaAvaliacoes média das notas arredondada a 1 casa decimal (HALF_UP); null se não há avaliações
 * @param totalAvaliacoes quantidade de avaliações; 0 se não há avaliações
 * @param notaUsuario     nota (1 a 5) do usuário logado; null para anônimo ou para quem não avaliou
 */
public record AvaliacaoResumoResponse(
        Double mediaAvaliacoes,
        long totalAvaliacoes,
        Integer notaUsuario) {

    public static final AvaliacaoResumoResponse SEM_AVALIACOES = new AvaliacaoResumoResponse(null, 0, null);
}
