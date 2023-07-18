package com.github.engarik.constraints;

import java.util.HashSet;

import am.ik.yavi.core.CustomConstraint;
import com.github.engarik.model.Graph;
import com.github.engarik.model.StepModel;

public class StepIdsAreUniqueConstraint implements CustomConstraint<Graph> {

    @Override
    public String defaultMessageFormat() {
        return "stepIds must be unique";
    }

    @Override
    public String messageKey() {
        return "graph.stepIdsAreUnique";
    }

    @Override
    public boolean test(Graph graph) {
        return graph.getSteps().stream()
            .map(StepModel::getStepId)
            .allMatch(new HashSet<>()::add);
    }
}
