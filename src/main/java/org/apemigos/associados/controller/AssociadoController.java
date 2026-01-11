package org.apemigos.associados.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apemigos.associados.dto.AssociadoRequisicaoDTO;
import org.apemigos.associados.entity.Associado;
import org.apemigos.associados.service.AssociadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/associados")
@CrossOrigin(origins = "*")
@Tag(name = "Associados", description = "Gerenciamento de associados")
@RequiredArgsConstructor
public class AssociadoController {

    private final AssociadoService associadoService;

    private static final Logger log = LoggerFactory.getLogger(AssociadoController.class);

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Associado> createAssociadoMulti(
            @ModelAttribute AssociadoRequisicaoDTO dto
    ) {
            return ResponseEntity
                    .status(201)
                    .body(associadoService.saveFromMultipart(dto));
    }
}

