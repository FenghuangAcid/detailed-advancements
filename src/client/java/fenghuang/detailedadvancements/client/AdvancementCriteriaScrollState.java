package fenghuang.detailedadvancements.client;

import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class AdvancementCriteriaScrollState {

    private static final int MAX_VISIBLE_LINES = 6;

    private static final Map<AdvancementWidget, ScrollState> STATES =
            new WeakHashMap<>();

    private AdvancementCriteriaScrollState() {
    }

    public static synchronized List<FormattedCharSequence> getVisibleLines(
            AdvancementWidget widget,
            List<FormattedCharSequence> lines
    ) {
        ScrollState state = STATES.computeIfAbsent(
                widget,
                key -> new ScrollState()
        );

        state.totalLines = lines.size();

        int maxOffset = Math.max(
                0,
                state.totalLines - MAX_VISIBLE_LINES
        );

        state.offset = Math.max(
                0,
                Math.min(state.offset, maxOffset)
        );

        int end = Math.min(
                state.offset + MAX_VISIBLE_LINES,
                state.totalLines
        );

        return new ArrayList<>(
                lines.subList(state.offset, end)
        );
    }

    public static synchronized boolean scroll(
            AdvancementWidget widget,
            double scrollY
    ) {
        ScrollState state = STATES.get(widget);

        if (state == null || state.totalLines <= MAX_VISIBLE_LINES || scrollY == 0.0) {
            return false;
        }

        int maxOffset = state.totalLines - MAX_VISIBLE_LINES;

        // 滾輪向下：往後閱讀
        // 滾輪向上：回到前面的文字
        int direction = scrollY < 0.0 ? 1 : -1;

        state.offset = Math.max(
                0,
                Math.min(state.offset + direction, maxOffset)
        );

        // 即使已經到達頂端或底端，也不讓進度樹跟著移動
        return true;
    }

    private static class ScrollState {
        private int offset;
        private int totalLines;
    }
}