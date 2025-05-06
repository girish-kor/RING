package com.ring.Ring.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.ring.Ring.util.FilterUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilterService {
    private final SessionService sessionService;
    private final FilterUtils filterUtils;

    @Value("${ring.filter.threshold.nudity}")
    private float nudityThreshold;

    @Value("${ring.filter.threshold.violence}")
    private float violenceThreshold;

    public boolean filterTextContent(String sessionId, String text) {
        if (filterUtils.containsInappropriateText(text)) {
            sessionService.handleInappropriateContent(sessionId, "Inappropriate text detected");
            return true;
        }
        return false;
    }

    public boolean filterVideoContent(String sessionId, String base64Image) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
                ByteString imgBytes = ByteString.copyFrom(imageBytes);
                Image img = Image.newBuilder().setContent(imgBytes).build();

                List<AnnotateImageRequest> requests = new ArrayList<>();

                Feature feature = Feature.newBuilder()
                        .setType(Feature.Type.SAFE_SEARCH_DETECTION)
                        .build();

                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(feature)
                        .setImage(img)
                        .build();

                requests.add(request);

                BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        log.error("Error in Google Vision API response: {}", res.getError().getMessage());
                        return false;
                    }

                    SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();

                    // Check for inappropriate content
                    if (isInappropriateContent(annotation)) {
                        log.warn("Inappropriate content detected in image: {}", annotation);
                        sessionService.handleInappropriateContent(sessionId, "Inappropriate visual content detected");
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error processing image for content filtering", e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 image data", e);
        }

        return false;
    }

    private boolean isInappropriateContent(SafeSearchAnnotation annotation) {
        // Check for adult or violent content
        return isLikelyOrVeryLikely(annotation.getAdult()) ||
                isLikelyOrVeryLikely(annotation.getRacy()) ||
                isLikelyOrVeryLikely(annotation.getViolence());
    }

    private boolean isLikelyOrVeryLikely(Likelihood likelihood) {
        return likelihood == Likelihood.LIKELY || likelihood == Likelihood.VERY_LIKELY;
    }
}
