package org.apemigos.associados.enums;

public enum StatusCarteirinha {
    SOLICITADA,       // Usuário pediu, mas ninguém olhou ainda
    EM_ANALISE,       // Alguém da ONG está conferindo os documentos
    PENDENTE_DOC,     // Faltou documento ou a foto tá ruim (devolve pro usuário)
    APROVADA,         // Tudo certo, pronta para ser gerada
    EM_PRODUCAO,      // Enviada para gráfica ou gerando o PDF/Digital
    EXPEDIDA,         // Carteirinha física pronta (ou digital gerada)
    ENVIADA,          // Postada nos Correios (se for física)
    ENTREGUE,         // Usuário recebeu
    CANCELADA,        // Pedido negado ou duplicado
    VENCIDA           // Carteirinha expirou (bom pro futuro)
}
