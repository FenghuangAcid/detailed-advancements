package fenghuang.detailedadvancements.client;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;

import java.util.ArrayList;
import java.util.HashSet;

public class AdvancementCriteriaReader {
    public record CriterionStatus(String criterion, boolean completed) {
    }

    public static ArrayList<CriterionStatus> read(AdvancementHolder advancement, AdvancementProgress progress) {
        HashSet<String> completed_criteria = new HashSet<>();  //忽略重複內容，不管順序的 ArrayList

        (progress.getCompletedCriteria()).forEach(completed_criteria::add);

        ArrayList<CriterionStatus> results = new ArrayList<>();

        for (String criterion : ((advancement.value()).requirements()).names()) {
            boolean completed = completed_criteria.contains(criterion);

            results.add(new CriterionStatus(criterion, completed));
        }

        return results;
    }
}

