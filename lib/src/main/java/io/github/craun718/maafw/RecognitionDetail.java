package io.github.craun718.maafw;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Detailed result of a recognition operation. */
public final class RecognitionDetail {

    private final long recoId;
    private final String name;
    private final String algorithm;
    private final boolean hit;
    private final MaaRect box;
    private final List<RecognitionResult> allResults;
    private final List<RecognitionResult> filteredResults;
    private final RecognitionResult bestResult;
    private final Map<String, Object> rawDetail;
    private final MaaImage rawImage;
    private final List<MaaImage> drawImages;

    public RecognitionDetail(
            long recoId,
            String name,
            String algorithm,
            boolean hit,
            MaaRect box,
            List<RecognitionResult> allResults,
            List<RecognitionResult> filteredResults,
            RecognitionResult bestResult,
            Map<String, Object> rawDetail,
            MaaImage rawImage,
            List<MaaImage> drawImages) {
        this.recoId = recoId;
        this.name = name;
        this.algorithm = algorithm;
        this.hit = hit;
        this.box = box;
        this.allResults = List.copyOf(allResults == null ? List.of() : allResults);
        this.filteredResults = List.copyOf(filteredResults == null ? List.of() : filteredResults);
        this.bestResult = bestResult;
        this.rawDetail = rawDetail == null ? Map.of() : Map.copyOf(rawDetail);
        this.rawImage = rawImage == null ? MaaImage.empty() : rawImage;
        this.drawImages = List.copyOf(drawImages == null ? List.of() : drawImages);
    }

    public long recoId() {
        return recoId;
    }

    public String name() {
        return name;
    }

    public String algorithm() {
        return algorithm;
    }

    public boolean hit() {
        return hit;
    }

    public MaaRect box() {
        return box;
    }

    public List<RecognitionResult> allResults() {
        return allResults;
    }

    public List<RecognitionResult> filteredResults() {
        return filteredResults;
    }

    public RecognitionResult bestResult() {
        return bestResult;
    }

    public Map<String, Object> rawDetail() {
        return rawDetail;
    }

    public MaaImage rawImage() {
        return rawImage;
    }

    public List<MaaImage> drawImages() {
        return drawImages;
    }

    @Override
    public String toString() {
        return "RecognitionDetail(id=" + recoId + ", name=" + name + ", algorithm=" + algorithm + ", hit=" + hit + ")";
    }
}
