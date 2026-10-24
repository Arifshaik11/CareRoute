package com.innovation_matrix.careroute.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.patient.Program;
import org.junit.jupiter.api.Test;

class RuleEvaluatorTest {

    private final RuleEvaluator evaluator = new RuleEvaluator();

    private static CareRule rule(String code, Program program, RuleCondition condition, String explanation) {
        CareRule rule = new CareRule(code);
        rule.setName(code);
        rule.setProgram(program);
        rule.setCondition(condition);
        rule.setAction(RuleAction.REFERRAL);
        rule.setPriority(Priority.HIGH);
        rule.setDueWithinHours(48);
        rule.setExplanation(explanation);
        rule.setActive(true);
        return rule;
    }

    private static final RuleCondition HIGH_BP = new RuleCondition(MatchMode.ANY, List.of(
            new RuleCondition.Clause(ConditionField.SYSTOLIC_BP, Operator.GTE, IntNode.valueOf(140)),
            new RuleCondition.Clause(ConditionField.DIASTOLIC_BP, Operator.GTE, IntNode.valueOf(90))));

    private static VisitFacts maternal(Integer systolic, Integer diastolic, Set<String> symptoms) {
        return new VisitFacts(Program.MATERNAL, 24, systolic, diastolic, new BigDecimal("36.8"),
                null, 30, null, symptoms);
    }

    @Test
    void firesWhenAnyClauseHoldsAndExplainsWithObservedValues() {
        CareRule r = rule("MAT_HIGH_BP", Program.MATERNAL, HIGH_BP, "BP {SYSTOLIC_BP}/{DIASTOLIC_BP} mmHg");

        List<RuleMatch> matches = evaluator.evaluate(List.of(r), maternal(148, 86, Set.of()));

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).explanation()).isEqualTo("BP 148/86 mmHg");
    }

    @Test
    void doesNotFireForNormalReadings() {
        CareRule r = rule("MAT_HIGH_BP", Program.MATERNAL, HIGH_BP, "x");
        assertThat(evaluator.evaluate(List.of(r), maternal(118, 76, Set.of()))).isEmpty();
    }

    @Test
    void missingObservationNeverTriggersARule() {
        CareRule r = rule("MAT_HIGH_BP", Program.MATERNAL, HIGH_BP, "x");
        assertThat(evaluator.evaluate(List.of(r), maternal(null, null, Set.of()))).isEmpty();
    }

    @Test
    void ruleForAnotherProgrammeIsIgnored() {
        CareRule r = rule("NCD_HIGH_BP", Program.NCD, HIGH_BP, "x");
        assertThat(evaluator.evaluate(List.of(r), maternal(170, 110, Set.of()))).isEmpty();
    }

    @Test
    void inactiveRuleIsIgnored() {
        CareRule r = rule("MAT_HIGH_BP", Program.MATERNAL, HIGH_BP, "x");
        r.setActive(false);
        assertThat(evaluator.evaluate(List.of(r), maternal(170, 110, Set.of()))).isEmpty();
    }

    @Test
    void allModeRequiresEveryClause() {
        RuleCondition both = new RuleCondition(MatchMode.ALL, HIGH_BP.clauses());
        CareRule r = rule("BOTH", null, both, "x");
        assertThat(evaluator.evaluate(List.of(r), maternal(150, 85, Set.of()))).isEmpty();
        assertThat(evaluator.evaluate(List.of(r), maternal(150, 95, Set.of()))).hasSize(1);
    }

    @Test
    void symptomAndBooleanClauses() {
        RuleCondition danger = new RuleCondition(MatchMode.ANY, List.of(
                new RuleCondition.Clause(ConditionField.SYMPTOMS, Operator.CONTAINS, TextNode.valueOf("BLEEDING"))));
        CareRule symptomRule = rule("DANGER", Program.MATERNAL, danger, "Danger sign: {SYMPTOMS}");

        List<RuleMatch> matches = evaluator.evaluate(List.of(symptomRule), maternal(110, 70, Set.of("BLEEDING")));
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).explanation()).isEqualTo("Danger sign: bleeding");

        RuleCondition missedDose = new RuleCondition(MatchMode.ALL, List.of(
                new RuleCondition.Clause(ConditionField.DOSE_TAKEN, Operator.EQ, BooleanNode.FALSE)));
        CareRule doseRule = rule("TB_MISSED", Program.TB, missedDose, "x");
        VisitFacts tb = new VisitFacts(Program.TB, 40, null, null, null, null, null, false, Set.of());
        assertThat(evaluator.evaluate(List.of(doseRule), tb)).hasSize(1);
    }
}
