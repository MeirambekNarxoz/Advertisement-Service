package AdvertisementService.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.url}") // например: http://localhost:9000
    private String minioUrl;

    public String uploadThumbnail(UUID advertisementId, MultipartFile file) throws Exception {
        String extension = getExtension(file.getOriginalFilename());
        String objectName = "advertisements/" + advertisementId + "/" + UUID.randomUUID() + extension;

        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .contentType(file.getContentType())
                .stream(file.getInputStream(), file.getSize(), -1)
                .build();

        minioClient.putObject(args);

        // В БД храним полный URL
        return minioUrl + "/" + bucket + "/" + objectName;
    }

    public void deleteByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            String objectName = extractObjectName(fileUrl);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    private String extractObjectName(String url) {
        return url.substring((minioUrl + "/" + bucket + "/").length());
    }

    private String getExtension(String name) {
        if (name == null || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf("."));
    }
}
