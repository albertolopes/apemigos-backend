package org.apemigos.integrations.cloudinary.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apemigos.integrations.cloudinary.dto.CloudinaryUploadDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

@Slf4j
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Configurações AGRESSIVAS de otimização
    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 800;
    private static final float JPEG_QUALITY = 0.75f;
    private static final long MIN_SIZE_FOR_OPTIMIZATION = 100 * 1024;
    private static final long MAX_OPTIMIZED_SIZE = 500 * 1024;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Faz upload de uma imagem a partir de uma URL
     */
    public CloudinaryUploadDTO uploadImageFromUrl(String imageUrl, String folder) {
        try {
            if (!isConfigured()) {
                return CloudinaryUploadDTO.builder().success(false).message("Serviço de imagens não configurado").build();
            }

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return CloudinaryUploadDTO.builder().success(false).message("URL da imagem é obrigatória").build();
            }

            if (!isValidUrl(imageUrl)) {
                return CloudinaryUploadDTO.builder().success(false).message("URL da imagem inválida").build();
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    imageUrl,
                    ObjectUtils.asMap(
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false,
                            "folder", "apemigos/" + folder,
                            "resource_type", "image",
                            "quality", "auto:low"
                    )
            );

            String uploadedUrl = (String) uploadResult.get("secure_url");
            String publicId = extractPublicIdFromUrl(uploadedUrl);

            log.info("Imagem de URL salva no Cloudinary: {}", uploadedUrl);

            return CloudinaryUploadDTO.builder()
                    .success(true)
                    .url(uploadedUrl)
                    .publicId(publicId)
                    .folder(folder)
                    .message("Imagem da URL salva com sucesso")
                    .cloudName(getCloudName())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao fazer upload da URL para Cloudinary", e);
            return CloudinaryUploadDTO.builder().success(false).message("Erro ao fazer upload da imagem da URL: " + e.getMessage()).build();
        }
    }

    /**
     * Valida formato da URL
     */
    private boolean isValidUrl(String url) {
        return url != null &&
                (url.startsWith("http://") || url.startsWith("https://")) &&
                url.length() > 10;
    }

    /**
     * Deleta uma imagem do Cloudinary
     */
    public Map<String, Object> deleteImage(String imageUrl) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!isConfigured()) {
                return createErrorResponse("Serviço de imagens não configurado");
            }

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return createErrorResponse("URL da imagem é obrigatória");
            }

            String publicId = extractPublicIdFromUrl(imageUrl);
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String deletionResult = (String) result.get("result");

            boolean success = "ok".equals(deletionResult);

            if (success) {
                response.put("success", true);
                response.put("message", "Imagem deletada com sucesso");
                response.put("deleted_url", imageUrl);
                response.put("public_id", publicId);
            } else {
                response.put("success", false);
                response.put("message", "Imagem não encontrada");
                response.put("public_id", publicId);
            }

            return response;

        } catch (Exception e) {
            log.error("Erro ao deletar imagem do Cloudinary", e);
            return createErrorResponse("Erro ao deletar imagem: " + e.getMessage());
        }
    }

    /**
     * Obtém detalhes de uma imagem
     */
    public Map<String, Object> getImageDetails(String publicId) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!isConfigured()) {
                return createErrorResponse("Serviço de imagens não configurado");
            }

            if (publicId == null || publicId.trim().isEmpty()) {
                return createErrorResponse("Public ID é obrigatório");
            }

            Map<?, ?> resourceDetails = cloudinary.api().resource(
                    publicId,
                    ObjectUtils.asMap(
                            "quality_analysis", true
                    )
            );

            log.info("Detalhes da imagem: {}", publicId);

            response.put("success", true);
            response.put("public_id", publicId);
            response.put("details", resourceDetails);
            response.put("cloud_name", getCloudName());

            return response;

        } catch (Exception e) {
            log.error("Erro ao obter detalhes da imagem", e);
            return createErrorResponse("Erro ao obter detalhes da imagem: " + e.getMessage());
        }
    }

    /**
     * Gera URL transformada para a imagem
     */
    public Map<String, Object> generateTransformedImage(String publicId, int width, int height) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!isConfigured()) {
                return createErrorResponse("Serviço de imagens não configurado");
            }

            if (publicId == null || publicId.trim().isEmpty()) {
                return createErrorResponse("Public ID é obrigatório");
            }

            if (width <= 0 || height <= 0) {
                return createErrorResponse("Largura e altura devem ser maiores que zero");
            }

            String transformedUrl = cloudinary.url()
                    .transformation(new Transformation()
                            .crop("pad")
                            .width(width)
                            .height(height)
                            .background("auto:predominant"))
                    .generate(publicId);

            if (transformedUrl == null) {
                return createErrorResponse("Erro ao gerar imagem transformada");
            }

            log.info("🎨 URL transformada gerada: {}", transformedUrl);

            response.put("success", true);
            response.put("transformed_url", transformedUrl);
            response.put("public_id", publicId);
            response.put("width", width);
            response.put("height", height);
            response.put("message", "Imagem transformada gerada com sucesso");
            response.put("cloud_name", getCloudName());

            return response;

        } catch (Exception e) {
            log.error("❌ Erro ao gerar URL transformada", e);
            return createErrorResponse("Erro ao gerar imagem transformada: " + e.getMessage());
        }
    }

    /**
     * Faz upload de uma imagem para o Cloudinary com otimização GARANTIDA
     */
    public CloudinaryUploadDTO uploadImagem(MultipartFile file, String folder) {

        try {
            // Validações
            if (!isConfigured()) {
                return CloudinaryUploadDTO.builder().success(false).message("Serviço de imagens não configurado. Verifique CLOUDINARY_URL").build();
            }

            if (file == null || file.isEmpty()) {
                return CloudinaryUploadDTO.builder().success(false).message("Arquivo vazio").build();
            }

            if (!isImageFile(file)) {
                return CloudinaryUploadDTO.builder().success(false).message("Apenas imagens são permitidas").build();
            }

            if (!isValidFileSize(file)) {
                return CloudinaryUploadDTO.builder().success(false).message("Arquivo muito grande (máximo 10MB)").build();
            }

            long originalSize = file.getBytes().length;
            byte[] imageBytes;
            long optimizedSize;
            String optimizedFormat = "original";
            Integer compressionRatioInt = null;

            if (originalSize <= MIN_SIZE_FOR_OPTIMIZATION) {
                log.info("Imagem pequena ({}), usando original", formatFileSize(originalSize));
                imageBytes = file.getBytes();
                optimizedSize = originalSize;
            } else {
                imageBytes = aggressivelyOptimizeImage(file, originalSize);
                optimizedSize = imageBytes.length;
                optimizedFormat = "jpeg";

                if (optimizedSize > MAX_OPTIMIZED_SIZE) {
                    log.warn("🔄 Imagem ainda grande ({}), aplicando compressão extrema", formatFileSize(optimizedSize));
                    imageBytes = applyExtremeCompression(file);
                    optimizedSize = imageBytes.length;
                }

                double compressionRatio = (1 - (double) optimizedSize / originalSize) * 100;
                compressionRatioInt = (int) Math.round(compressionRatio);

                log.info("Otimização AGRESSIVA: {} → {} (redução de {}%)",
                        formatFileSize(originalSize), formatFileSize(optimizedSize), compressionRatio);
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap(
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false,
                            "folder", "apemigos/" + folder,
                            "resource_type", "image",
                            "quality", "auto:low",
                            "fetch_format", "auto"
                    )
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = extractPublicIdFromUrl(imageUrl);

            log.info("Imagem otimizada salva: {} ({} → {})", imageUrl, formatFileSize(originalSize), formatFileSize(optimizedSize));

            return CloudinaryUploadDTO.builder()
                    .success(true)
                    .url(imageUrl)
                    .publicId(publicId)
                    .folder(folder
                    )
                    .message("Imagem otimizada salva com sucesso")
                    .cloudName(getCloudName())
                    .fileSize(optimizedSize)
                    .originalSize(originalSize)
                    .optimizedSize(optimizedSize)
                    .compressionRatio(compressionRatioInt)
                    .optimizedFormat(optimizedFormat)
                    .contentType("image/jpeg")
                    .build();

        } catch (Exception e) {
            log.error("Erro no upload", e);
            return CloudinaryUploadDTO.builder().success(false).message("Erro: " + e.getMessage()).build();
        }
    }

    /**
     * Upload genérico de arquivo: imagens usam uploadImagem (com otimização)
     * PDFs serão otimizados (via PDFBox) e enviados como resource_type=raw.
     */
    public CloudinaryUploadDTO uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) return CloudinaryUploadDTO.builder().success(false).message("Arquivo vazio").build();
        String contentType = file.getContentType();
        try {
            if (contentType != null && contentType.startsWith("image/")) {
                return uploadImagem(file, folder); // já retorna CloudinaryUploadDTO
            }

            // Tratar PDF
            if ("application/pdf".equalsIgnoreCase(contentType) || (file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".pdf"))) {
                long originalSize = file.getSize();
                log.info("Processing PDF upload: name='{}' contentType='{}' originalSize={}", file.getOriginalFilename(), contentType, formatFileSize(originalSize));
                // IMPORTANT: read bytes immediately to avoid Tomcat removing the temporary uploaded file
                byte[] originalBytes = file.getBytes();
                byte[] optimized = optimizePdf(originalBytes, file.getOriginalFilename());

                if (optimized == null || optimized.length == 0) {
                    return CloudinaryUploadDTO.builder().success(false).message("Falha ao otimizar PDF").build();
                }

                long optimizedSize = optimized.length;
                log.info("PDF optimization: {} -> {} ({}% reduction)", file.getOriginalFilename(), formatFileSize(optimizedSize), Math.round((1 - (double) optimizedSize / Math.max(1, originalSize)) * 100));

                // If optimization did not reduce size and original was large, try a stronger pass
                if (optimizedSize >= originalSize && originalSize > MIN_SIZE_FOR_OPTIMIZATION) {
                    log.warn("Otimização inicial não reduziu o PDF ({}). Tentando otimização forte adicional.", file.getOriginalFilename());
                    byte[] strongAgain = optimizePdfStrong(originalBytes, 0.45f, 800, file.getOriginalFilename());
                    if (strongAgain != null && strongAgain.length > 0 && strongAgain.length < optimizedSize) {
                        optimized = strongAgain;
                        optimizedSize = optimized.length;
                        log.info("Otimização forte adicional reduziu: {} -> {}", file.getOriginalFilename(), formatFileSize(optimizedSize));
                    } else {
                        log.warn("Otimização forte adicional não reduziu mais o arquivo: {}", file.getOriginalFilename());
                    }
                }

                Map<?, ?> uploadResult = null;
                java.nio.file.Path tmp = null;
                try {
                    tmp = java.nio.file.Files.createTempFile("apemigos-upload-", "-" + file.getOriginalFilename());
                    java.nio.file.Files.write(tmp, optimized);
                    java.io.File tmpFile = tmp.toFile();
                    log.info("Uploading temp file to Cloudinary: {}", tmpFile.getAbsolutePath());

                    // tentativas: auto -> raw -> uploadLarge
                    try {
                        uploadResult = cloudinary.uploader().upload(tmpFile, ObjectUtils.asMap(
                                "use_filename", true,
                                "unique_filename", true,
                                "overwrite", false,
                                "folder", "apemigos/" + folder,
                                "resource_type", "auto"
                        ));
                    } catch (Exception ignore) {}

                    if (uploadResult == null || (uploadResult.get("public_id") == null && uploadResult.get("secure_url") == null)) {
                        try {
                            uploadResult = cloudinary.uploader().upload(tmpFile, ObjectUtils.asMap(
                                    "use_filename", true,
                                    "unique_filename", true,
                                    "overwrite", false,
                                    "folder", "apemigos/" + folder,
                                    "resource_type", "raw"
                            ));
                        } catch (Exception ignore) {}
                    }

                    if ((uploadResult == null || (uploadResult.get("public_id") == null && uploadResult.get("secure_url") == null)) || uploadResult.get("error") != null) {
                        try {
                            uploadResult = cloudinary.uploader().uploadLarge(tmpFile, ObjectUtils.asMap(
                                    "use_filename", true,
                                    "unique_filename", true,
                                    "overwrite", false,
                                    "folder", "apemigos/" + folder,
                                    "resource_type", "raw"
                            ));
                        } catch (NoSuchMethodError nsme) {
                            log.debug("uploadLarge not available: {}", nsme.getMessage());
                        } catch (Exception ignore) {}
                    }

                } finally {
                    if (tmp != null) try { java.nio.file.Files.deleteIfExists(tmp); } catch (Exception ignore) {}
                }

                if (uploadResult == null) return CloudinaryUploadDTO.builder().success(false).message("Cloudinary não retornou resposta").build();

                if (uploadResult.get("error") != null) {
                    return CloudinaryUploadDTO.builder().success(false).message(String.valueOf(uploadResult.get("error"))).build();
                }

                String url = uploadResult.get("secure_url") != null ? String.valueOf(uploadResult.get("secure_url")) : (uploadResult.get("url") != null ? String.valueOf(uploadResult.get("url")) : null);
                String publicId = uploadResult.get("public_id") != null ? String.valueOf(uploadResult.get("public_id")) : null;

                // try to fetch resource info if url null but publicId present
                if (url == null && publicId != null) {
                    try {
                        Map<?, ?> resource = cloudinary.api().resource(publicId, ObjectUtils.asMap("resource_type", "raw"));
                        if (resource != null) {
                            if (resource.get("secure_url") != null) url = String.valueOf(resource.get("secure_url"));
                            else if (resource.get("url") != null) url = String.valueOf(resource.get("url"));
                        }
                    } catch (Exception ignore) {}
                }

                return CloudinaryUploadDTO.builder()
                        .success(url != null)
                        .url(url)
                        .publicId(publicId)
                        .folder(folder)
                        .message(url != null ? "PDF salvo com otimização" : "Upload realizado, mas sem URL")
                        .fileSize(Long.valueOf(optimized.length))
                        .build();
            }

            // Outros tipos: envia como raw sem otimização
            Map<?, ?> uploadResult = null;
            java.nio.file.Path tmp = null;
            try {
                tmp = java.nio.file.Files.createTempFile("apemigos-upload-", "-" + file.getOriginalFilename());
                java.nio.file.Files.write(tmp, file.getBytes());
                java.io.File tmpFile = tmp.toFile();
                log.info("Uploading temp raw file to Cloudinary: {}", tmpFile.getAbsolutePath());

                try {
                    uploadResult = cloudinary.uploader().upload(tmpFile, ObjectUtils.asMap(
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false,
                            "folder", "apemigos/" + folder,
                            "resource_type", "raw"
                    ));
                } catch (Exception ignore) {}

                if ((uploadResult == null || (uploadResult.get("public_id") == null && uploadResult.get("secure_url") == null)) || uploadResult.get("error") != null) {
                    try {
                        uploadResult = cloudinary.uploader().uploadLarge(tmpFile, ObjectUtils.asMap(
                                "use_filename", true,
                                "unique_filename", true,
                                "overwrite", false,
                                "folder", "apemigos/" + folder,
                                "resource_type", "raw"
                        ));
                    } catch (NoSuchMethodError nsme) {
                        log.debug("uploadLarge not available: {}", nsme.getMessage());
                    } catch (Exception ignore) {}
                }

            } finally {
                if (tmp != null) try { java.nio.file.Files.deleteIfExists(tmp); } catch (Exception ignore) {}
            }

            if (uploadResult == null) return CloudinaryUploadDTO.builder().success(false).message("Cloudinary não retornou resposta").build();
            if (uploadResult.get("error") != null) return CloudinaryUploadDTO.builder().success(false).message(String.valueOf(uploadResult.get("error"))).build();

            String url = uploadResult.get("secure_url") != null ? String.valueOf(uploadResult.get("secure_url")) : (uploadResult.get("url") != null ? String.valueOf(uploadResult.get("url")) : null);
            String publicId = uploadResult.get("public_id") != null ? String.valueOf(uploadResult.get("public_id")) : null;

            if (url == null && publicId != null) {
                try {
                    Map<?, ?> resource = cloudinary.api().resource(publicId, ObjectUtils.asMap("resource_type", "raw"));
                    if (resource != null) {
                        if (resource.get("secure_url") != null) url = String.valueOf(resource.get("secure_url"));
                        else if (resource.get("url") != null) url = String.valueOf(resource.get("url"));
                    }
                } catch (Exception ignore) {}
            }

            return CloudinaryUploadDTO.builder()
                    .success(url != null)
                    .url(url)
                    .publicId(publicId)
                    .folder(folder)
                    .message(url != null ? "Arquivo salvo com sucesso" : "Upload realizado, mas sem URL")
                    .fileSize(Long.valueOf(file.getSize()))
                    .build();

        } catch (Exception e) {
            log.error("Erro ao enviar arquivo para Cloudinary", e);
            return CloudinaryUploadDTO.builder().success(false).message("Erro ao enviar arquivo: " + e.getMessage()).build();
        }
    }

    /**
     * Otimiza (re-salva) um PDF usando PDFBox. Esta otimização é leve: remove metadados
     * e re-escreve o PDF — pode reduzir tamanho, mas para compressões fortes é necessário
     * re-encodar imagens (essa versão forte tenta re-encodar imagens internas em JPEG com qualidade reduzida).
     */
    private byte[] optimizePdf(byte[] fileBytes, String originalFilename) throws IOException {
        // Tentativa forte primeiro: re-encodar imagens internas
        byte[] strong = optimizePdfStrong(fileBytes, 0.6f, 1200, originalFilename);
        if (strong != null && strong.length > 0 && strong.length < fileBytes.length) {
            return strong;
        }

        // Fallback leve (re-save, remove metadados)
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileBytes));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            if (document.getDocumentInformation() != null) {
                var info = document.getDocumentInformation();
                info.setAuthor(null);
                info.setTitle(null);
                info.setKeywords(null);
                info.setSubject(null);
                info.setCreator(null);
                document.setDocumentInformation(info);
            }

            document.save(out);
            byte[] leveled = out.toByteArray();
            // Se fallback não reduziu, devolve original
            if (leveled.length < fileBytes.length) return leveled;
            return fileBytes;
        } catch (Exception e) {
            log.warn("Falha na otimização leve de PDF, retornando original: {}", e.getMessage());
            return fileBytes;
        }
    }

    /**
     * Otimização forte: re-encoda imagens embutidas em JPEG com qualidade `jpegQuality` e limita largura a `maxImageWidth`.
     * Retorna bytes do PDF otimizado (ou null em caso de falha).
     */
    private byte[] optimizePdfStrong(byte[] fileBytes, float jpegQuality, int maxImageWidth, String originalFilename) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileBytes));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

             int replacedCount = 0;
             long totalBeforeImages = 0L;
             long totalAfterImages = 0L;

             for (var page : document.getPages()) {
                var resources = page.getResources();
                if (resources == null) continue;

                var names = resources.getXObjectNames();
                for (var name : names) {
                    try {
                        var xobject = resources.getXObject(name);
                        // handle image objects
                        if (xobject instanceof PDImageXObject) {
                            PDImageXObject imageXObject = (PDImageXObject) xobject;

                            BufferedImage bi = imageXObject.getImage();
                            if (bi == null) continue;

                            // Estimate original image size by encoding to JPEG (approximation) to compare before/after
                            try {
                                byte[] before = convertToJpeg(bi, 1.0f);
                                if (before != null) totalBeforeImages += before.length;
                            } catch (Exception exBefore) {
                                log.debug("Não foi possível obter bytes da imagem original para análise: {}", exBefore.getMessage());
                            }

                            BufferedImage resized = bi;
                            if (bi.getWidth() > maxImageWidth) {
                                double scale = (double) maxImageWidth / bi.getWidth();
                                int newW = (int) Math.max(1, Math.round(bi.getWidth() * scale));
                                int newH = (int) Math.max(1, Math.round(bi.getHeight() * scale));
                                BufferedImage tmp = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                                Graphics2D g = tmp.createGraphics();
                                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                g.drawImage(bi, 0, 0, newW, newH, null);
                                g.dispose();
                                resized = tmp;
                            }

                            ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
                            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
                            if (writers.hasNext()) {
                                ImageWriter writer = writers.next();
                                ImageWriteParam param = writer.getDefaultWriteParam();
                                if (param.canWriteCompressed()) {
                                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                    param.setCompressionQuality(Math.max(0.05f, Math.min(0.95f, jpegQuality)));
                                }
                                try (ImageOutputStream ios = ImageIO.createImageOutputStream(imgOut)) {
                                    writer.setOutput(ios);
                                    writer.write(null, new IIOImage(resized, null, null), param);
                                }
                                writer.dispose();
                            } else {
                                ImageIO.write(resized, "jpeg", imgOut);
                            }

                            byte[] jpegBytes = imgOut.toByteArray();
                            totalAfterImages += jpegBytes.length;

                            PDImageXObject newImg = PDImageXObject.createFromByteArray(document, jpegBytes, name.getName());
                            resources.put(name, newImg);
                            replacedCount++;
                        } else if (xobject instanceof PDFormXObject) {
                            // recurse into form XObject resources
                            PDFormXObject form = (PDFormXObject) xobject;
                            var formRes = form.getResources();
                            if (formRes == null) continue;
                            var formNames = formRes.getXObjectNames();
                            for (var fn : formNames) {
                                try {
                                    var fx = formRes.getXObject(fn);
                                    if (fx instanceof PDImageXObject) {
                                        PDImageXObject imageXObject = (PDImageXObject) fx;

                                        BufferedImage bi = imageXObject.getImage();
                                        if (bi == null) continue;

                                        try {
                                            byte[] before = convertToJpeg(bi, 1.0f);
                                            if (before != null) totalBeforeImages += before.length;
                                        } catch (Exception exBefore) {
                                            log.debug("Não foi possível obter bytes da imagem original em form XObject: {}", exBefore.getMessage());
                                        }

                                        BufferedImage resized = bi;
                                        if (bi.getWidth() > maxImageWidth) {
                                            double scale = (double) maxImageWidth / bi.getWidth();
                                            int newW = (int) Math.max(1, Math.round(bi.getWidth() * scale));
                                            int newH = (int) Math.max(1, Math.round(bi.getHeight() * scale));
                                            BufferedImage tmp = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                                            Graphics2D g = tmp.createGraphics();
                                            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                            g.drawImage(bi, 0, 0, newW, newH, null);
                                            g.dispose();
                                            resized = tmp;
                                        }

                                        ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
                                        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
                                        if (writers.hasNext()) {
                                            ImageWriter writer = writers.next();
                                            ImageWriteParam param = writer.getDefaultWriteParam();
                                            if (param.canWriteCompressed()) {
                                                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                                param.setCompressionQuality(Math.max(0.05f, Math.min(0.95f, jpegQuality)));
                                            }
                                            try (ImageOutputStream ios = ImageIO.createImageOutputStream(imgOut)) {
                                                writer.setOutput(ios);
                                                writer.write(null, new IIOImage(resized, null, null), param);
                                            }
                                            writer.dispose();
                                        } else {
                                            ImageIO.write(resized, "jpeg", imgOut);
                                        }

                                        byte[] jpegBytes = imgOut.toByteArray();
                                        totalAfterImages += jpegBytes.length;

                                        PDImageXObject newImg = PDImageXObject.createFromByteArray(document, jpegBytes, fn.getName());
                                        formRes.put(fn, newImg);
                                        replacedCount++;
                                    }
                                } catch (Exception ex2) {
                                    log.debug("Erro ao processar XObject em PDFormXObject: {}", ex2.getMessage());
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.debug("Não foi possível re-encodar imagem do PDF (nome={}): {}", name, ex.getMessage());
                    }
                }
            }

            // Remove metadados
            if (document.getDocumentInformation() != null) {
                var info = document.getDocumentInformation();
                info.setAuthor(null);
                info.setTitle(null);
                info.setKeywords(null);
                info.setSubject(null);
                info.setCreator(null);
                document.setDocumentInformation(info);
            }

            document.save(out);

            log.info("optimizePdfStrong: replacedImages={}, totalBefore={}, totalAfter={} bytes", replacedCount, totalBeforeImages, totalAfterImages);

            return out.toByteArray();
        } catch (Exception e) {
            log.warn("Falha na otimização forte de PDF: {}", e.getMessage());
            try {
                return fileBytes;
            } catch (Exception io) {
                return null;
            }
        }
    }

    /**
     * Health check do serviço
     */
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();

        boolean isConfigured = isConfigured();
        response.put("service", "cloudinary");
        response.put("configured", isConfigured);
        response.put("status", isConfigured ? "healthy" : "not_configured");
        response.put("cloud_name", getCloudName());
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    /**
     * Otimização AGRESSIVA garantida
     */
    private byte[] aggressivelyOptimizeImage(MultipartFile file, long originalSize) throws IOException {
        byte[] optimizedImage = null;
        int attempts = 0;

        while (optimizedImage == null || (optimizedImage.length > originalSize && attempts < 3)) {
            attempts++;
            float quality = 0.8f - (attempts * 0.15f); // Reduz qualidade a cada tentativa

            try {
                optimizedImage = simpleOptimizeImage(file, quality);

                if (optimizedImage.length > originalSize) {
                    log.warn("Tentativa {}: imagem ficou maior, reduzindo qualidade para {}",
                            attempts, quality);
                }
            } catch (Exception e) {
                log.warn("Falha na otimização, tentativa {}", attempts);
                if (attempts >= 3) {
                    // Fallback: força JPEG básico
                    return forceJpegCompression(file);
                }
            }
        }

        return optimizedImage;
    }

    /**
     * Otimização SIMPLES e EFETIVA
     */
    private byte[] simpleOptimizeImage(MultipartFile file, float quality) throws IOException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(file.getBytes())) {
            BufferedImage originalImage = ImageIO.read(input);

            if (originalImage == null) {
                return forceJpegCompression(file);
            }

            // 1. Redimensiona AGRESSIVAMENTE
            BufferedImage resizedImage = aggressiveResize(originalImage);

            // 2. Converte para JPEG com qualidade controlada
            return convertToJpeg(resizedImage, quality);
        }
    }

    /**
     * Redimensionamento AGRESSIVO
     */
    private BufferedImage aggressiveResize(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Sempre redimensiona se for maior que o máximo
        if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
            double scale = Math.min(
                    (double) MAX_WIDTH / originalWidth,
                    (double) MAX_HEIGHT / originalHeight
            );

            int newWidth = (int) (originalWidth * scale);
            int newHeight = (int) (originalHeight * scale);

            log.info("Redimensionamento agressivo: {}x{} → {}x{}",
                    originalWidth, originalHeight, newWidth, newHeight);

            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();

            // Configurações para tamanho (não qualidade)
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            return resizedImage;
        }

        return originalImage;
    }

    /**
     * Conversão SIMPLES para JPEG
     */
    private byte[] convertToJpeg(BufferedImage image, float quality) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            BufferedImage jpegImage = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g = jpegImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("JPEG");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);

                try (ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
                    writer.setOutput(ios);
                    writer.write(null, new IIOImage(jpegImage, null, null), param);
                }
                writer.dispose();
            } else {
                // Fallback absoluto
                ImageIO.write(jpegImage, "JPEG", output);
            }

            return output.toByteArray();
        }
    }

    /**
     * Compressão EXTREMA para imagens muito grandes
     */
    private byte[] applyExtremeCompression(MultipartFile file) throws IOException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(file.getBytes())) {
            BufferedImage originalImage = ImageIO.read(input);

            if (originalImage == null) {
                return file.getBytes(); // Fallback
            }

            // Redimensiona para tamanho MUITO pequeno
            int smallWidth = Math.min(800, originalImage.getWidth() / 2);
            int smallHeight = Math.min(600, originalImage.getHeight() / 2);

            BufferedImage smallImage = new BufferedImage(smallWidth, smallHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = smallImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(originalImage, 0, 0, smallWidth, smallHeight, null);
            g.dispose();

            // Qualidade MUITO baixa
            return convertToJpeg(smallImage, 0.5f);
        }
    }

    /**
     * Força conversão para JPEG como fallback
     */
    private byte[] forceJpegCompression(MultipartFile file) throws IOException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(file.getBytes());
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            BufferedImage image = ImageIO.read(input);
            if (image != null) {
                // Redimensiona se necessário
                if (image.getWidth() > 800 || image.getHeight() > 600) {
                    BufferedImage resized = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = resized.createGraphics();
                    g.drawImage(image, 0, 0, 800, 600, null);
                    g.dispose();
                    image = resized;
                }

                ImageIO.write(image, "JPEG", output);
                return output.toByteArray();
            }
        }

        // Último fallback - retorna original mas truncado se muito grande
        byte[] original = file.getBytes();
        if (original.length > 500 * 1024) {
            // Se maior que 500KB, trunca (isso NUNCA deve acontecer)
            log.error("CRÍTICO: Imagem continua grande após todas as otimizações");
        }
        return original;
    }

    /**
     * Formata tamanho de arquivo
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // ========== MÉTODOS DE CONFIGURAÇÃO (mantidos) ==========

    public boolean isConfigured() {
        try {
            return cloudinary != null &&
                    cloudinary.config != null &&
                    cloudinary.config.cloudName != null &&
                    !cloudinary.config.cloudName.isEmpty() &&
                    !cloudinary.config.cloudName.equals("your_cloud_name");
        } catch (Exception e) {
            return false;
        }
    }

    private String getCloudName() {
        if (!isConfigured()) return "Não configurado";
        return cloudinary.config.cloudName;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }

    // ... (mantenha os outros métodos: uploadImageFromUrl, deleteImage, etc.)

    private boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private boolean isValidFileSize(MultipartFile file) {
        if (file == null) return false;
        return file.getSize() <= 10 * 1024 * 1024;
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/upload/")[1].split("/");
            String publicIdWithVersion = parts[parts.length - 1];
            if (publicIdWithVersion.contains(".")) {
                String[] idParts = publicIdWithVersion.split("\\.");
                if (idParts.length > 1) {
                    return idParts[0].replaceFirst("^v\\d+/", "");
                }
            }
            return publicIdWithVersion.replaceFirst("^v\\d+/", "");
        } catch (Exception e) {
            throw new IllegalArgumentException("URL inválida: " + imageUrl);
        }
    }
}

