package com.github.engarik.constraints;

import am.ik.yavi.core.CustomConstraint;
import com.github.engarik.model.Graph;
import com.github.engarik.model.StepModel;

public class StepsContainStartStepConstraint implements CustomConstraint<Graph> {
    @Override
    public String defaultMessageFormat() {
        return "steps: must contain startStep";
    }

    @Override
    public String messageKey() {
        return "graph.stepsContainStartStep";
    }

    @Override
    public boolean test(Graph graph) {
        return graph.getSteps().stream()
            .map(StepModel::getStepId)
            .anyMatch(stepCode -> stepCode.equals(graph.getStartStep()));
    }
}
