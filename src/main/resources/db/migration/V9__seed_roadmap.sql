-- Tags canônicas usadas pelos tópicos das trilhas (não sobrescreve tags existentes)
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000001', 'html')        ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000002', 'css')         ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000003', 'javascript')  ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000004', 'react')       ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000005', 'api-rest')    ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000006', 'typescript')  ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000007', 'git')         ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000008', 'testes')      ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000009', 'java')        ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000010', 'sql')         ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000011', 'spring-boot') ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000012', 'jpa')         ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000013', 'seguranca')   ON CONFLICT (nome) DO NOTHING;
INSERT INTO tag (id, nome) VALUES ('c0000000-0000-0000-0000-000000000014', 'devops')      ON CONFLICT (nome) DO NOTHING;

-- Trilha: Desenvolvimento Frontend
INSERT INTO trilha (id, slug, titulo, descricao) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'frontend-dev', 'Desenvolvimento Frontend',
     'Do zero ao profissional no desenvolvimento de interfaces web.');

-- Trilha: Backend Java & Spring
INSERT INTO trilha (id, slug, titulo, descricao) VALUES
    ('a0000000-0000-0000-0000-000000000002', 'backend-java', 'Backend Java & Spring',
     'Dos fundamentos de Java até APIs Spring em produção.');

-- Tópicos Frontend
INSERT INTO topico_trilha (id, trilha_id, titulo, descricao, fase, ordem, tag_nome) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'HTML semântico',              'Estruture páginas acessíveis e bem organizadas.',            1, 1, 'html'),
    ('b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'CSS moderno',                 'Flexbox, Grid, responsividade e design system.',             1, 2, 'css'),
    ('b0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000001', 'Lógica com JavaScript',       'Variáveis, funções, arrays e manipulação do DOM.',           1, 3, 'javascript'),
    ('b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000001', 'Primeiros passos no React',   'Componentes, props, estado e renderização.',                 2, 1, 'react'),
    ('b0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000001', 'Consumindo APIs REST',        'Fetch, axios, estados de carregamento e erro.',              2, 2, 'api-rest'),
    ('b0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000001', 'TypeScript no frontend',      'Tipos, interfaces e tipagem de componentes.',                2, 3, 'typescript'),
    ('b0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000001', 'Git e versionamento',         'Commits, branches, pull requests e colaboração.',            3, 1, 'git'),
    ('b0000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000001', 'Testes automatizados',        'Testes unitários e de integração no frontend.',              3, 2, 'testes');

-- Tópicos Backend
INSERT INTO topico_trilha (id, trilha_id, titulo, descricao, fase, ordem, tag_nome) VALUES
    ('b0000000-0000-0000-0000-000000000101', 'a0000000-0000-0000-0000-000000000002', 'Orientação a objetos com Java', 'Classes, herança, polimorfismo e boas práticas.',     1, 1, 'java'),
    ('b0000000-0000-0000-0000-000000000102', 'a0000000-0000-0000-0000-000000000002', 'Banco de dados e SQL',          'Modelagem, consultas, joins e índices.',               1, 2, 'sql'),
    ('b0000000-0000-0000-0000-000000000103', 'a0000000-0000-0000-0000-000000000002', 'Git e versionamento',           'Commits, branches, pull requests e colaboração.',      1, 3, 'git'),
    ('b0000000-0000-0000-0000-000000000104', 'a0000000-0000-0000-0000-000000000002', 'Primeiros passos no Spring Boot', 'Projeto, dependências, beans e profiles.',          2, 1, 'spring-boot'),
    ('b0000000-0000-0000-0000-000000000105', 'a0000000-0000-0000-0000-000000000002', 'APIs REST com Spring',          'Controllers, DTOs, validação e tratamento de erros.',  2, 2, 'api-rest'),
    ('b0000000-0000-0000-0000-000000000106', 'a0000000-0000-0000-0000-000000000002', 'Persistência com JPA',          'Entidades, repositórios, relacionamentos e queries.',  2, 3, 'jpa'),
    ('b0000000-0000-0000-0000-000000000107', 'a0000000-0000-0000-0000-000000000002', 'Segurança e JWT',               'Autenticação, autorização e boas práticas.',           3, 1, 'seguranca'),
    ('b0000000-0000-0000-0000-000000000108', 'a0000000-0000-0000-0000-000000000002', 'Deploy e cloud',                'Containers, CI/CD e publicação da aplicação.',         3, 2, 'devops');
