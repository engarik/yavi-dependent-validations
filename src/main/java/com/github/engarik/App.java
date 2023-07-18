package com.github.engarik;

import java.io.File;
import java.util.Objects;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CollectionConstraint;
import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.engarik.constraints.NextStepsExistConstraint;
import com.github.engarik.constraints.StepIdsAreUniqueConstraint;
import com.github.engarik.constraints.StepsContainStartStepConstraint;
import com.github.engarik.model.Graph;
import lombok.SneakyThrows;

public class App {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        NextStepsExistConstraint nextStepsExistConstraint = new NextStepsExistConstraint();
        StepIdsAreUniqueConstraint stepIdsAreUniqueConstraint = new StepIdsAreUniqueConstraint();
        StepsContainStartStepConstraint stepsContainStartStepConstraint = new StepsContainStartStepConstraint();

        Graph nullStartStepGraph = read("src/main/resources/nullStartStep_graph.json", new TypeReference<>() {});
        Graph emptyStepsGraph = read("src/main/resources/emptySteps_graph.json", new TypeReference<>() {});

        Validator<Graph> graphValidator = ValidatorBuilder.of(Graph.class)
            .constraint(Graph::getStartStep, "startStep", Constraint::notNull)
            .constraint(Graph::getSteps, "steps", CollectionConstraint::notEmpty)
            .constraintOnTarget(stepsContainStartStepConstraint, "StepsContainStartStepConstraint")
            .constraintOnTarget(stepIdsAreUniqueConstraint, "StepIdsAreUniqueConstraint")
            .constraintOnTarget(nextStepsExistConstraint, "NextStepsExistConstraint")
            .build();

        ConstraintViolations nullStartStepGraphViolations = graphValidator.validate(nullStartStepGraph);
        ConstraintViolations emptyStepsGraphViolations = graphValidator.validate(emptyStepsGraph);

        // Produces two violations, because startStep is null, which is not among other steps
        nullStartStepGraphViolations.stream()
            .map(ConstraintViolation::message)
            .forEach(System.out::println);

        // Also produces two violations, because steps is empty
        emptyStepsGraphViolations.stream()
            .map(ConstraintViolation::message)
            .forEach(System.out::println);

        Validator<Graph> dependentAwareGraphValidator = ValidatorBuilder.of(Graph.class)
            .constraint(Graph::getStartStep, "startStep", Constraint::notNull)
            .constraint(Graph::getSteps, "steps", CollectionConstraint::notEmpty)
            .constraintOnCondition((graph, constraintContext) ->
                    Objects.nonNull(graph.getSteps()) && Objects.nonNull(graph.getStartStep()),
                ValidatorBuilder.of(Graph.class)
                    .constraintOnTarget(stepsContainStartStepConstraint, "StepsContainStartStepConstraint")
                    .build())            .constraintOnTarget(stepIdsAreUniqueConstraint, "StepIdsAreUniqueConstraint")
            .constraintOnTarget(nextStepsExistConstraint, "NextStepsExistConstraint")
            .build();

        ConstraintViolations nullStartStepGraphViolations2 = dependentAwareGraphValidator.validate(nullStartStepGraph);
        ConstraintViolations emptyStepsGraphViolations2 = dependentAwareGraphValidator.validate(emptyStepsGraph);


        assert nullStartStepGraphViolations2.size() == 0;
        assert emptyStepsGraphViolations2.size() == 0;

        // Also, I would like to have some control over order of constraints being checked, e.g.
        // StepsContainStartStepConstraint -> StepIdsAreUniqueConstraint -> NextStepsExistConstraint
        // If previous validation fails, do not execute next. Tried using failFast, but with no success :(
    }

    @SneakyThrows
    public static <T> T read(String path, TypeReference<T> typeReference)  {
        return MAPPER.readValue(new File(path), typeReference);
    }
}
