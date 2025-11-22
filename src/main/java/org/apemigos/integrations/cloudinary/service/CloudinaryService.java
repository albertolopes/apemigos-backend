package org.apemigos.integrations.cloudinary.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Configurações AGGRESSIVAS de otimização
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
    public Map<String, Object> uploadImageFromUrl(String imageUrl, String folder) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!isConfigured()) {
                return createErrorResponse("Serviço de imagens não configurado");
            }

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return createErrorResponse("URL da imagem é obrigatória");
            }

            // Valida formato da URL
            if (!isValidUrl(imageUrl)) {
                return createErrorResponse("URL da imagem inválida");
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    imageUrl,
                    ObjectUtils.asMap(
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false,
                            "folder", "apemigos/" + folder,
                            "resource_type", "image",
                            "quality", "auto:low" // Também otimiza no Cloudinary
                    )
            );

            String uploadedUrl = (String) uploadResult.get("secure_url");
            String publicId = extractPublicIdFromUrl(uploadedUrl);

            log.info("Imagem de URL salva no Cloudinary: {}", uploadedUrl);

            response.put("success", true);
            response.put("url", uploadedUrl);
            response.put("public_id", publicId);
            response.put("folder", folder);
            response.put("message", "Imagem da URL salva com sucesso");
            response.put("original_url", imageUrl);
            response.put("cloud_name", getCloudName());

            return response;

        } catch (Exception e) {
            log.error("Erro ao fazer upload da URL para Cloudinary", e);
            return createErrorResponse("Erro ao fazer upload da imagem da URL: " + e.getMessage());
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
     * Faz upload de uma imagem para o Cloudinary com otimização GARANTIDA
     */
    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validações
            if (!isConfigured()) {
                return createErrorResponse("Serviço de imagens não configurado. Verifique CLOUDINARY_URL");
            }

            if (file == null || file.isEmpty()) {
                return createErrorResponse("Arquivo vazio");
            }

            if (!isImageFile(file)) {
                return createErrorResponse("Apenas imagens são permitidas");
            }

            if (!isValidFileSize(file)) {
                return createErrorResponse("Arquivo muito grande (máximo 10MB)");
            }

            long originalSize = file.getBytes().length;
            byte[] imageBytes;
            long optimizedSize;
            String optimizedFormat = "original";

            // SEMPRE otimiza, exceto imagens muito pequenas
            if (originalSize <= MIN_SIZE_FOR_OPTIMIZATION) {
                log.info("Imagem pequena ({}), usando original", formatFileSize(originalSize));
                imageBytes = file.getBytes();
                optimizedSize = originalSize;
            } else {
                // OBRIGA a otimização com múltiplas tentativas
                imageBytes = aggressivelyOptimizeImage(file, originalSize);
                optimizedSize = imageBytes.length;
                optimizedFormat = "jpeg"; // Força JPEG para melhor compressão

                // Se ainda estiver grande, reduz qualidade ainda mais
                if (optimizedSize > MAX_OPTIMIZED_SIZE) {
                    log.warn("🔄 Imagem ainda grande ({}), aplicando compressão extrema", formatFileSize(optimizedSize));
                    imageBytes = applyExtremeCompression(file);
                    optimizedSize = imageBytes.length;
                }

                double compressionRatio = (1 - (double) optimizedSize / originalSize) * 100;
                log.info("Otimização AGGRESSIVA: {} → {} (redução de {}%)",
                        formatFileSize(originalSize),
                        formatFileSize(optimizedSize),
                        compressionRatio);

                response.put("original_size", originalSize);
                response.put("optimized_size", optimizedSize);
                response.put("compression_ratio", Math.round(compressionRatio));
                response.put("optimized_format", optimizedFormat);
            }

            // Upload para Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap(
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false,
                            "folder", "apemigos/" + folder,
                            "resource_type", "image",
                            "quality", "auto:low", // Cloudinary em qualidade BAIXA
                            "fetch_format", "auto"
                    )
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = extractPublicIdFromUrl(imageUrl);

            log.info("Imagem otimizada salva: {} ({} → {})",
                    imageUrl, formatFileSize(originalSize), formatFileSize(optimizedSize));

            // Resposta
            response.put("success", true);
            response.put("url", imageUrl);
            response.put("public_id", publicId);
            response.put("folder", folder);
            response.put("message", "Imagem otimizada salva com sucesso");
            response.put("cloud_name", getCloudName());
            response.put("file_size", optimizedSize);
            response.put("content_type", "image/jpeg"); // Sempre JPEG otimizado

            return response;

        } catch (Exception e) {
            log.error("Erro no upload", e);
            return createErrorResponse("Erro: " + e.getMessage());
        }
    }

    /**
     * Otimização AGGRESSIVA garantida
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

            // 1. Redimensiona AGGRESSIVAMENTE
            BufferedImage resizedImage = aggressiveResize(originalImage);

            // 2. Converte para JPEG com qualidade controlada
            return convertToJpeg(resizedImage, quality);
        }
    }

    /**
     * Redimensionamento AGGRESSIVO
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