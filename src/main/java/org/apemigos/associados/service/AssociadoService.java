package org.apemigos.associados.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apemigos.associados.dto.AssociadoRequisicaoDTO;
import org.apemigos.associados.dto.AssociadoFileResponseDTO;
import org.apemigos.associados.dto.AssociadoResponseDTO;
import org.apemigos.associados.entity.Associado;
import org.apemigos.associados.entity.AssociadoFile;
import org.apemigos.associados.enums.StatusCarteirinha;
import org.apemigos.associados.mapper.AssociadoFileMapper;
import org.apemigos.associados.mapper.AssociadoMapper;
import org.apemigos.associados.repository.AssociadoFileRepository;
import org.apemigos.associados.repository.AssociadoRepository;
import org.apemigos.exceptions.ObjectNotFoundException;
import org.apemigos.integrations.cloudinary.dto.CloudinaryUploadDTO;
import org.apemigos.integrations.cloudinary.service.CloudinaryService;
import org.apemigos.integrations.email.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final AssociadoFileMapper associadoFileMapper; // Inject the new mapper
    private final EmailService emailService;

    private static final String CLOUD_FOLDER = "associados";
    private static final String SUBJECT = "Novo formulario recebido";

    @Value("${default.email.to}")
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

        try {
            emailService.sendEmail(
                defaultEmail,
                SUBJECT.concat(" - ").concat(associado.getNome() + " " + associado.getSobrenome()),
                dto.getBodyHtml(),
                arquivos
            );
        } catch (Exception e) {
            log.error("Erro ao enviar email para admin: {}", e.getMessage());
        }

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
                                .originalName(file.getOriginalFilename())
                                .contentType(file.getContentType())
                                .size(file.getSize())
                                .cloudSuccess(uploaded.isSuccess())
                                .cloudMessage(uploaded.getMessage())
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

    public Page<AssociadoResponseDTO> findAll(String keyword, Pageable pageable) {
        String searchTerm = keyword;
        if (keyword == null || keyword.isBlank() || "null".equalsIgnoreCase(keyword) || "undefined".equalsIgnoreCase(keyword)) {
            searchTerm = null;
        }

        Page<Associado> associados = associadoRepository.findByKeyword(searchTerm, pageable);
        return associados.map(associadoMapper::toResponseDto);
    }

    public AssociadoResponseDTO updateStatus(Long id, StatusCarteirinha status) {
        Associado associado = associadoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Associado não encontrado"));
        
        associado.setStatusCarteirinha(status);
        return associadoMapper.toResponseDto(associadoRepository.save(associado));
    }

    public String confirmarEntregaCarteirinha(String cpf) {
        Associado associado = associadoRepository.findByCpf(cpf)
                .orElseThrow(() -> new ObjectNotFoundException("Associado não encontrado com o CPF informado"));

        if (associado.getStatusCarteirinha() == StatusCarteirinha.ENTREGUE) {
            return "A carteirinha já consta como entregue.";
        }

        associado.setStatusCarteirinha(StatusCarteirinha.ENTREGUE);
        associadoRepository.save(associado);
        return "Entrega da carteirinha confirmada com sucesso.";
    }

    public List<AssociadoFileResponseDTO> findFilesByAssociadoId(Long associadoId) {
        List<AssociadoFile> files = associadoFileRepository.findByAssociadoId(associadoId);
        return associadoFileMapper.toDtoList(files);
    }
}
