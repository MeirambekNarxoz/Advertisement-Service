package AdvertisementService.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
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

    @Value("${minio.url}")       // http://localhost:9000 или nginx-прокси
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

        // В БД будешь хранить полный URL, либо относительный путь — на твой выбор
        return minioUrl + "/" + bucket + "/" + objectName;
        // или просто return objectName;
    }

    private String getExtension(String name) {
        if (name == null || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf("."));
    }
}
