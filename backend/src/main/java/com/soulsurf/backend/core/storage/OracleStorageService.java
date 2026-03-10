package com.soulsurf.backend.core.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;

import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "feature.blob.enabled", havingValue = "true")
public class OracleStorageService {

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
                .privateKeySupplier(() -> {
                    try {
                        return new java.io.ByteArrayInputStream(privateKey.getBytes());
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao ler chave privada OCI", e);
                    }
                })
                .region(com.oracle.bmc.Region.fromRegionCodeOrId(region))
                .build();

        this.objectStorageClient = ObjectStorageClient.builder()
                .build(provider);
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + file.getOriginalFilename();

        InputStream inputStream = file.getInputStream();

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(fileName)
                .contentLength(file.getSize())
                .contentType(file.getContentType())
                .putObjectBody(inputStream)
                .build();

        objectStorageClient.putObject(putRequest);

        // Retorna a URL pública do objeto
        return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
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
            String url = String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                    region, namespace, bucketName, obj.getName());
            urls.add(url);
        }

        return urls;
    }
}
