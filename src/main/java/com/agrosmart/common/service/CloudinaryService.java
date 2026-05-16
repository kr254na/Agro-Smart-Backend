package com.agrosmart.common.service;

import com.agrosmart.common.exception.ImageUploadException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            String publicIdWithExtension = imageUrl.substring(imageUrl.indexOf("agrosmart/"));
            String publicId = publicIdWithExtension.substring(0, publicIdWithExtension.lastIndexOf("."));

            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

        } catch (IOException e) {
            System.err.println("Failed to clean up image asset from Cloudinary storage: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during Cloudinary image extraction: " + e.getMessage());
        }
    }

    public String uploadImage(MultipartFile file, String folderName, String module, String keyword) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder","agrosmart/"+folderName,
                            "use_filename", true,
                            "unique_filename", true,
                            "tags", Arrays.asList(module, keyword)
                    )
            );
            return uploadResult.get("secure_url").toString();
        }catch (IOException e) {
            throw new ImageUploadException("File reading failed", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw new ImageUploadException("Cloudinary service error", HttpStatus.BAD_GATEWAY);
        } catch (Exception e) {
            throw new ImageUploadException("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
