package io.github.craun718.maafw;

import io.github.craun718.maafw.pipeline.JNodeAttr;
import java.util.ArrayList;
import java.util.List;

/** Internal helper for normalizing raw and typed next-list entries. */
final class MaaNextItems {

    private MaaNextItems() {
    }

    static List<String> names(List<?> values) {
        if (values == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(values.size());
        for (Object value : values) {
            if (value instanceof String name) {
                names.add(name);
            } else if (value instanceof JNodeAttr attr) {
                names.add(attr.formatName());
            } else {
                throw new IllegalArgumentException("Next list items must be String or JNodeAttr");
            }
        }
        return List.copyOf(names);
    }
}
