package com.tobe.healthy.workout.application;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.tobe.healthy.common.Utils;
import com.tobe.healthy.common.error.CustomException;
import com.tobe.healthy.common.error.OAuthError;
import com.tobe.healthy.common.error.OAuthException;
import com.tobe.healthy.common.redis.RedisService;
import com.tobe.healthy.diet.repository.DietFileRepository;
import com.tobe.healthy.member.domain.dto.in.OAuthInfo;
import com.tobe.healthy.member.domain.entity.Member;
import com.tobe.healthy.workout.domain.dto.in.RegisterFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.tobe.healthy.common.Utils.*;
import static com.tobe.healthy.common.error.ErrorCode.FILE_REMOVE_ERROR;
import static com.tobe.healthy.common.error.ErrorCode.PROFILE_ACCESS_FAILED;
import static com.tobe.healthy.common.redis.RedisKeyPrefix.TEMP_FILE_URI;
import static java.util.UUID.randomUUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final AmazonS3 amazonS3;
    private final RedisService redisService;
    private final WebClient webClient;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    // 1. 파일을 AWS S3에 업로드 후 업로드 주소 반환
    public List<RegisterFile> uploadFiles(String folder, List<MultipartFile> uploadFiles, Member member) {
        List<RegisterFile> uploadFile = new ArrayList<>();
        int fileOrder = 0;
        for (MultipartFile file : uploadFiles) {
            if (!file.isEmpty()) {
                try (InputStream inputStream = file.getInputStream()) {

                    ObjectMetadata objectMetadata = Utils.createObjectMetadata(file.getSize(), file.getContentType());
                    String fileName = System.currentTimeMillis() + "-" + randomUUID();
                    String savedFileName ="origin/" + folder + "/" + fileName + ".jpg";
                    amazonS3.putObject(
                            bucketName,
                            savedFileName,
                            inputStream,
                            objectMetadata
                    );
                    String fileUrl = amazonS3.getUrl(bucketName, savedFileName).toString()
                            .replace(S3_DOMAIN, CDN_DOMAIN);
                    redisService.setValuesWithTimeout(TEMP_FILE_URI.getDescription() + fileUrl, member.getId().toString(), FILE_TEMP_UPLOAD_TIMEOUT); // 30분
                    uploadFile.add(new RegisterFile(fileUrl, ++fileOrder));
                } catch (Exception e) {
                    log.error("error => {}", e.getStackTrace()[0]);
                }
            }
        }
        return uploadFile;
    }

    // 1. 파일을 AWS S3 temp -> 대상폴더로 업로드 후 CDN 주소 반환
    public RegisterFile moveDirTempToOrigin(String dir, String oldSavedFileName) {
        String newSavedFileName = "origin/" + dir + oldSavedFileName.replaceFirst("temp/", "");
        CopyObjectRequest copyObjRequest = new CopyObjectRequest(
                bucketName,
                oldSavedFileName,
                bucketName,
                newSavedFileName
        );
        amazonS3.copyObject(copyObjRequest);
        String fileUrl = amazonS3.getUrl(bucketName, newSavedFileName).toString()
                .replace(S3_DOMAIN, CDN_DOMAIN);
        makeThumb(fileUrl);
        return new RegisterFile(fileUrl);
    }

    private void makeThumb(String fileUrl){
        String w_400 = "?w=400&q=90";
        String w_800 = "?w=800&q=90";
        String w_1200 = "?w=1200&q=90";
        List<String> sizes = List.of(w_400, w_800, w_1200);

        for (String size : sizes) {
            webClient.get()
                    .uri(fileUrl + size)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    ;
        }
    }

    public void deleteDietFile(String fileName) {
        try {
            amazonS3.deleteObject(bucketName, "origin/diet/" + fileName);
        } catch (Exception e) {
            log.error("error => {}", e.getStackTrace()[0]);
            throw new CustomException(FILE_REMOVE_ERROR);
        }
    }

    public void deleteHistoryFile(String fileName) {
        try {
            amazonS3.deleteObject(bucketName, "origin/workout-history/" + fileName);
        } catch (Exception e) {
            log.error("error => {}", e.getStackTrace()[0]);
            throw new CustomException(FILE_REMOVE_ERROR);
        }
    }

}
