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
    private final Object rawDetailValue;
    private final MaaImage rawImage;
    private final List<MaaImage> drawImages;

    public RecognitionDetail(long recoId, String name, String algorithm, boolean hit, MaaRect box, List<RecognitionResult> allResults,
            List<RecognitionResult> filteredResults, RecognitionResult bestResult, Map<String, Object> rawDetail, MaaImage rawImage,
            List<MaaImage> drawImages) {
        this(recoId, name, algorithm, hit, box, allResults, filteredResults, bestResult, (Object) rawDetail, rawDetail, rawImage,
                drawImages);
    }

    /**
     * Creates a detail whose raw JSON may be a non-object form, such as the And/Or sub-result
     * array returned by the native tasker.
     */
    public RecognitionDetail(long recoId, String name, String algorithm, boolean hit, MaaRect box, List<RecognitionResult> allResults,
            List<RecognitionResult> filteredResults, RecognitionResult bestResult, Object rawDetailValue, MaaImage rawImage,
            List<MaaImage> drawImages) {
        this(recoId, name, algorithm, hit, box, allResults, filteredResults, bestResult, rawDetailValue,
                rawDetailValue instanceof Map<?, ?> map ? MaaResultParsers.objectMap(map) : Map.of(), rawImage, drawImages);
    }

    private RecognitionDetail(long recoId, String name, String algorithm, boolean hit, MaaRect box, List<RecognitionResult> allResults,
            List<RecognitionResult> filteredResults, RecognitionResult bestResult, Object rawDetailValue, Map<String, Object> rawDetail,
            MaaImage rawImage, List<MaaImage> drawImages) {
        this.recoId = recoId;
        this.name = name;
        this.algorithm = algorithm;
        this.hit = hit;
        this.box = box;
        this.allResults = List.copyOf(allResults == null ? List.of() : allResults);
        this.filteredResults = List.copyOf(filteredResults == null ? List.of() : filteredResults);
        this.bestResult = bestResult;
        this.rawDetailValue = rawDetailValue;
        this.rawDetail = MaaResultParsers.unmodifiableMap(rawDetail);
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

    /** Returns the complete raw JSON value; this may be a list for And/Or recognitions. */
    public Object rawDetailValue() {
        return rawDetailValue;
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
