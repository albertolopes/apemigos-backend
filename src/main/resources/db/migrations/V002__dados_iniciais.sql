-- Primeiro insere todas as notícias na tabela noticia
INSERT INTO noticia (title, short_description, image, date, slug) VALUES
(
  'Campanha de Conscientização sobre Esclerose Múltipla',
  'Estamos promovendo uma campanha para aumentar a conscientização sobre os desafios da esclerose múltipla.',
  'https://picsum.photos/600/400?random=1',
  '2025-09-01 00:00:00+00',
  'campanha-consciencia'
),
(
  'Novos Programas de Apoio a Pacientes',
  'Conheça os novos programas de suporte que oferecemos para pessoas com esclerose múltipla e suas famílias.',
  'https://picsum.photos/600/400?random=2',
  '2025-09-05 00:00:00+00',
  'programas-apoio'
),
(
  'Evento de Sensibilização e Arrecadação de Fundos',
  'Participe do nosso evento especial para aprender mais sobre a esclerose múltipla e ajudar na arrecadação de fundos.',
  'https://picsum.photos/600/400?random=3',
  '2025-09-10 00:00:00+00',
  'evento-arrecadacao'
),
(
  'Palestra sobre Inclusão e Acessibilidade',
  'Uma palestra inspiradora sobre inclusão e acessibilidade no dia a dia de pessoas com esclerose múltipla.',
  'https://picsum.photos/600/400?random=4',
  '2025-09-12 00:00:00+00',
  'palestra-inclusao'
),
(
  'Maratona Solidária pela Esclerose Múltipla',
  'Corra conosco e ajude a arrecadar fundos para programas de apoio a pacientes e pesquisas.',
  'https://picsum.photos/600/400?random=5',
  '2025-09-15 00:00:00+00',
  'maratona-solidaria'
),
(
  'Oficina de Cuidados e Bem-Estar',
  'Uma tarde dedicada à troca de experiências sobre autocuidado e qualidade de vida.',
  'https://picsum.photos/600/400?random=6',
  '2025-09-18 00:00:00+00',
  'oficina-bem-estar'
),
(
  'Workshop: Tecnologia e Acessibilidade',
  'Descubra como as novas tecnologias estão tornando a vida mais acessível para pessoas com limitações motoras.',
  'https://picsum.photos/600/400?random=7',
  '2025-09-20 00:00:00+00',
  'workshop-tecnologia'
),
(
  'Seminário Nacional sobre Esclerose Múltipla',
  'Reunimos especialistas de todo o país para debater os novos caminhos no tratamento da doença.',
  'https://picsum.photos/600/400?random=8',
  '2025-09-25 00:00:00+00',
  'seminario-nacional'
),
(
  'Histórias de Superação',
  'Conheça pessoas que enfrentam a esclerose múltipla com coragem e inspiram toda a comunidade.',
  'https://picsum.photos/600/400?random=9',
  '2025-09-28 00:00:00+00',
  'historias-superacao'
),
(
  'Campanha Outubro Laranja',
  'Durante o mês de outubro, participe das ações que destacam a importância da conscientização sobre a esclerose múltipla.',
  'https://picsum.photos/600/400?random=10',
  '2025-10-01 00:00:00+00',
  'outubro-laranja'
),
(
  'Doação e Solidariedade',
  'Sua contribuição transforma vidas! Saiba como apoiar nossos projetos e eventos.',
  'https://picsum.photos/600/400?random=11',
  '2025-10-05 00:00:00+00',
  'doacao-solidariedade'
                                                                      );

-- Depois insere os conteúdos completos na tabela noticia_conteudo
INSERT INTO noticia_conteudo (noticia_id, long_description) VALUES
(
(SELECT id FROM noticia WHERE slug = 'campanha-consciencia'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Estamos promovendo uma campanha para aumentar a conscientização sobre os desafios da esclerose múltipla. A iniciativa visa educar a população sobre os sintomas, tratamentos e formas de apoio aos pacientes.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">A campanha inclui materiais educativos, palestras em escolas e empresas, e ações nas redes sociais para alcançar o maior número possível de pessoas.</p>'
),
(
(SELECT id FROM noticia WHERE slug = 'programas-apoio'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Conheça os novos programas de suporte que oferecemos para pessoas com esclerose múltipla e suas famílias. Desenvolvemos iniciativas específicas para atender às diferentes necessidades dos pacientes.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Entre os novos programas estão: grupos de apoio psicológico, acompanhamento nutricional especializado, sessões de fisioterapia e workshops sobre direitos dos pacientes.</p>'
),
(
(SELECT id FROM noticia WHERE slug = 'evento-arrecadacao'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Participe do nosso evento especial para aprender mais sobre a esclerose múltipla e ajudar na arrecadação de fundos. Será uma noite de palestras, depoimentos e atividades interativas.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Todo o valor arrecadado será destinado à manutenção dos nossos programas de apoio e à aquisição de equipamentos para reabilitação.</p>'
),
(
(SELECT id FROM noticia WHERE slug = 'palestra-inclusao'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Uma palestra inspiradora sobre inclusão e acessibilidade no dia a dia de pessoas com esclerose múltipla. Vamos discutir como pequenas mudanças no ambiente podem fazer uma grande diferença.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Especialistas em acessibilidade e pacientes compartilharão experiências práticas sobre adaptações no trabalho, em casa e nos espaços públicos.</p>'
),
(
(SELECT id FROM noticia WHERE slug = 'maratona-solidaria'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Corra conosco e ajude a arrecadar fundos para programas de apoio a pacientes e pesquisas. A maratona é aberta a pessoas de todas as idades e condições físicas.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Haverá percursos de 5km, 10km e uma caminhada solidária de 2km. Inscrições abertas no site oficial do evento.</p>'
),
(
(SELECT id FROM noticia WHERE slug = 'oficina-bem-estar'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Uma tarde dedicada à troca de experiências sobre autocuidado e qualidade de vida. A oficina abordará técnicas de relaxamento, alimentação saudável e exercícios adaptados.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Participantes aprenderão estratégias práticas para melhorar o bem-estar físico e emocional no dia a dia.</p>'
),
(
(SELECT id FROM noticia WHERE slug = 'workshop-tecnologia'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Descubra como as novas tecnologias estão tornando a vida mais acessível para pessoas com limitações motoras. Apresentaremos dispositivos e aplicativos que facilitam a comunicação e a mobilidade.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Especialistas em tecnologia assistiva demonstrarão equipamentos que podem ser adquiridos através dos nossos programas de apoio.</p>'
),
(
(SELECT id FROM noticia WHERE slug = 'seminario-nacional'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Reunimos especialistas de todo o país para debater os novos caminhos no tratamento da doença. O seminário abordará desde avanços na pesquisa até políticas públicas para pacientes.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Serão apresentados casos de sucesso e discutidas estratégias para melhorar o acesso aos tratamentos em todas as regiões do Brasil.</p>'
),
(
(SELECT id FROM noticia WHERE slug = 'historias-superacao'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Conheça pessoas que enfrentam a esclerose múltipla com coragem e inspiram toda a comunidade. Nesta edição especial, compartilhamos histórias reais de superação e resiliência.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Cada história é um testemunho de força e esperança, mostrando que é possível viver plenamente mesmo com os desafios da doença.</p>'
),
(
(SELECT id FROM noticia WHERE slug = 'outubro-laranja'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Durante o mês de outubro, participe das ações que destacam a importância da conscientização sobre a esclerose múltipla. A cor laranja simboliza a luta e a esperança dos pacientes.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Programação inclui iluminação de monumentos, distribuição de laços laranjas e uma série de lives com especialistas.</p>'
),
(
(SELECT id FROM noticia WHERE slug = 'doacao-solidariedade'),
'<p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Sua contribuição transforma vidas! Saiba como apoiar nossos projetos e eventos. Existem diversas formas de ajudar: doações pontuais, contribuições mensais ou voluntariado.</p><p class="text-slate-500 pt-6 max-w-3xl text-sm text-center mx-auto">Todo recurso é auditado e aplicado diretamente nos programas de apoio aos pacientes e suas famílias.</p>'
);