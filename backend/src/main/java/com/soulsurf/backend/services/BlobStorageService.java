package com.soulsurf.backend.services;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BlobStorageService {

    private final BlobContainerClient containerClient;

    public BlobStorageService(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName) {

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    public String uploadFile(MultipartFile file) throws IOException {

        String fileName = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + file.getOriginalFilename();

        BlobClient blobClient = containerClient.getBlobClient(fileName);


        blobClient.upload(file.getInputStream(), file.getSize(), true);

        return blobClient.getBlobUrl();
    }
    public List<String> listFiles() {
        List<String> urls = new ArrayList<>();

        for (BlobItem blobItem : containerClient.listBlobs()) {
            String blobUrl = containerClient.getBlobClient(blobItem.getName()).getBlobUrl();
            urls.add(blobUrl);
        }

        return urls;
    }

}
