package com.github.engarik.constraints;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import am.ik.yavi.core.CustomConstraint;
import com.github.engarik.model.Graph;
import com.github.engarik.model.StepModel;
import com.github.engarik.model.StepTerminal;

public class NextStepsExistConstraint implements CustomConstraint<Graph> {
    @Override
    public String defaultMessageFormat() {
        return "nextSteps: must be among steps";
    }

    @Override
    public String messageKey() {
        return "graph.nextStepExists";
    }

    @Override
    public boolean test(Graph graph) {
        HashSet<Integer> uniqueStepIds = graph.getSteps().stream()
            .map(StepModel::getStepId)
            .collect(Collectors.toCollection(HashSet::new));
        Set<Integer> nextStepIds =
            graph.getSteps().stream()
                .flatMap(step -> step.getStepTerminals().stream()
                    .map(StepTerminal::getNextStep))
                .collect(Collectors.toSet());


        return uniqueStepIds.containsAll(nextStepIds);
    }
}
