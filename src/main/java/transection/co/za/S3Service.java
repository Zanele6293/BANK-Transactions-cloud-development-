package transection.co.za;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${aws.s3.bucket-name:zionex-mock-bucket}")
    private String bucketName;

    @Value("${aws.region:us-east-1}")
    private String region;

    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadProfilePicture(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Profile picture is mandatory.");
        }

        try {
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar.jpg";
            String safeName = originalName.replaceAll("[^a-zA-Z0-9.-]", "_");
            String fileName = "profiles/" + UUID.randomUUID() + "_" + safeName;

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            // Attempt cloud upload
            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);

        } catch (Exception e) {
            // PRINT THE ACTUAL ERROR TO YOUR SERVER TERMINAL INSTEAD OF FREEZING
            System.err.println("CRITICAL S3 UPLOAD ERROR: " + e.getMessage());
            e.printStackTrace();

            // Fallback safe URL so registration still succeeds during local development/demos
            return "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png";
        }
    }
}