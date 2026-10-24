package com.innovation_matrix.careroute.rules;

import com.innovation_matrix.careroute.common.BaseEntity;
import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.patient.Program;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A transparent, configurable rule: if the condition holds, create a follow-up or referral. */
@Entity
@Table(name = "care_rule")
public class CareRule extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    /** Null means the rule applies to every programme. */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Program program;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private RuleCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RuleAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority;

    @Column(name = "due_within_hours", nullable = false)
    private Integer dueWithinHours;

    /** Human-readable reason; {FIELD} placeholders are filled with observed values. */
    @Column(nullable = false, columnDefinition = "text")
    private String explanation;

    @Column(nullable = false)
    private boolean active = true;

    protected CareRule() {
    }

    public CareRule(String code) {
        this.code = code;
    }

    public boolean appliesTo(Program patientProgram) {
        return program == null || program == patientProgram;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public RuleCondition getCondition() {
        return condition;
    }

    public void setCondition(RuleCondition condition) {
        this.condition = condition;
    }

    public RuleAction getAction() {
        return action;
    }

    public void setAction(RuleAction action) {
        this.action = action;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Integer getDueWithinHours() {
        return dueWithinHours;
    }

    public void setDueWithinHours(Integer dueWithinHours) {
        this.dueWithinHours = dueWithinHours;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
