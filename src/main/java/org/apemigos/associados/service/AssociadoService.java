package org.apemigos.associados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apemigos.associados.dto.AssociadoRequisicaoDTO;
import org.apemigos.associados.entity.Associado;
import org.apemigos.associados.entity.AssociadoFile;
import org.apemigos.associados.mapper.AssociadoMapper;
import org.apemigos.associados.repository.AssociadoFileRepository;
import org.apemigos.associados.repository.AssociadoRepository;
import org.apemigos.email.dto.EmailAttachment;
import org.apemigos.email.service.EmailService;
import org.apemigos.integrations.cloudinary.dto.CloudinaryUploadDTO;
import org.apemigos.integrations.cloudinary.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssociadoService {

    private final AssociadoRepository associadoRepository;
    private final AssociadoFileRepository associadoFileRepository;
    private final CloudinaryService cloudinaryService;
    private final AssociadoMapper associadoMapper;
    private final EmailService emailService;

    private static final String CLOUD_FOLDER = "associados";
    private static final String SUBJECT = "Solicitação de Associação";

    @Value("${default.email.url}")
    private String defaultEmail;

    public Associado saveFromMultipart(AssociadoRequisicaoDTO dto) throws RuntimeException {
        Associado associado = associadoMapper.toEntity(dto);

        List<MultipartFile> arquivos = new ArrayList<>();

        if(dto.getDocumento() != null && !dto.getDocumento().isEmpty())
            arquivos.add(dto.getDocumento());

        if(dto.getFoto3x4() != null && !dto.getFoto3x4().isEmpty())
            arquivos.add(dto.getFoto3x4());

        if(dto.getLaudo() != null && !dto.getLaudo().isEmpty())
            arquivos.add(dto.getLaudo());

        Associado associadoSalvo = associadoRepository.save(associado);

        emailService
            .sendEmail(
                defaultEmail,
                SUBJECT.concat(" - ").concat(associado.getNome() + " " + associado.getSobrenome()),
                dto.getBodyHtml(),
                true,
                null,
                    arquivos.stream().map(
                        file ->
                        {
                            try {
                                return new EmailAttachment(
                                    file.getOriginalFilename(),
                                    file.getContentType(),
                                    file.getBytes()
                                );
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .toList()
            );

        arquivos.forEach(file ->
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    CloudinaryUploadDTO uploaded = cloudinaryService.uploadFile(file, CLOUD_FOLDER);

                    if (uploaded.getUrl() != null) {
                        associadoFileRepository.save(
                                AssociadoFile.builder()
                                        .associado(associadoSalvo)
                                        .cloudPublicId(uploaded.getPublicId())
                                        .cloudUrl(uploaded.getUrl())
                                        .cloudFolder(file.getOriginalFilename())
                                        .build()
                        );
                    }
                } catch (Exception e) {
                    log.error("Erro ao fazer upload do arquivo {}: {}", file.getOriginalFilename(), e.getMessage());
                }
            })
        );

        return associadoSalvo;
    }
}


