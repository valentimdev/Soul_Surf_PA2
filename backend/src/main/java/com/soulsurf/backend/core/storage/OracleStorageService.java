package com.soulsurf.backend.core.storage;

import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@ConditionalOnProperty(name = "feature.blob.enabled", havingValue = "true")
public class OracleStorageService {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ObjectStorageClient objectStorageClient;
    private final String namespace;
    private final String bucketName;
    private final String region;

    public OracleStorageService(
            @Value("${oci.storage.region}") String region,
            @Value("${oci.storage.bucket-name}") String bucketName,
            @Value("${oci.storage.namespace}") String namespace,
            @Value("${oci.storage.user-id}") String userId,
            @Value("${oci.storage.tenancy-id}") String tenancyId,
            @Value("${oci.storage.fingerprint}") String fingerprint,
            @Value("${oci.storage.private-key}") String privateKey) {

        this.bucketName = bucketName;
        this.namespace = namespace;
        this.region = region;

        SimpleAuthenticationDetailsProvider provider = SimpleAuthenticationDetailsProvider.builder()
                .userId(userId)
                .tenantId(tenancyId)
                .fingerprint(fingerprint)
                .privateKeySupplier(() -> readPrivateKey(privateKey))
                .region(com.oracle.bmc.Region.fromRegionCodeOrId(region))
                .build();

        this.objectStorageClient = ObjectStorageClient.builder().build(provider);
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = FILE_TIMESTAMP.format(LocalDateTime.now()) + "-" + sanitizeFilename(file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(fileName)
                    .contentLength(file.getSize())
                    .contentType(file.getContentType())
                    .putObjectBody(inputStream)
                    .build();

            objectStorageClient.putObject(putRequest);
        }

        return String.format(
                "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                region, namespace, bucketName, fileName);
    }

    public List<String> listFiles() {
        List<String> urls = new ArrayList<>();

        ListObjectsRequest listRequest = ListObjectsRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .build();

        ListObjectsResponse response = objectStorageClient.listObjects(listRequest);

        for (ObjectSummary obj : response.getListObjects().getObjects()) {
            String url = String.format(
                    "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                    region, namespace, bucketName, obj.getName());
            urls.add(url);
        }

        return urls;
    }

    private InputStream readPrivateKey(String privateKey) {
        try {
            if (looksLikeFilePath(privateKey)) {
                return new FileInputStream(privateKey);
            }

            String normalized = privateKey.replace("\\n", "\n");
            return new ByteArrayInputStream(normalized.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler chave privada OCI", e);
        }
    }

    private boolean looksLikeFilePath(String privateKey) {
        if (privateKey == null || privateKey.isBlank()) {
            return false;
        }
        return privateKey.startsWith("/")
                || privateKey.startsWith("\\")
                || privateKey.matches("^[A-Za-z]:\\\\.*");
    }

    private String sanitizeFilename(String originalFilename) {
        String candidate = originalFilename == null ? "upload.bin" : originalFilename.trim();
        candidate = candidate.replace("\\", "/");
        int lastSlash = candidate.lastIndexOf('/');
        if (lastSlash >= 0) {
            candidate = candidate.substring(lastSlash + 1);
        }

        candidate = candidate.replaceAll("[^a-zA-Z0-9._-]", "_");
        candidate = candidate.replaceAll("_+", "_");

        if (candidate.isBlank()) {
            candidate = "upload.bin";
        }

        if (candidate.length() > 120) {
            String extension = "";
            int dotIndex = candidate.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < candidate.length() - 1) {
                extension = candidate.substring(dotIndex).toLowerCase(Locale.ROOT);
                candidate = candidate.substring(0, dotIndex);
            }
            candidate = candidate.substring(0, Math.min(candidate.length(), 110)) + extension;
        }

        return candidate;
    }
}
