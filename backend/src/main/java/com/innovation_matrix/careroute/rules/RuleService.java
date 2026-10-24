package com.innovation_matrix.careroute.rules;

import java.util.List;

import com.innovation_matrix.careroute.audit.AuditService;
import com.innovation_matrix.careroute.common.error.InvalidStateException;
import com.innovation_matrix.careroute.common.error.NotFoundException;
import com.innovation_matrix.careroute.rules.dto.RuleDto;
import com.innovation_matrix.careroute.rules.dto.RuleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuleService {

    private final CareRuleRepository repository;
    private final RuleConditionValidator validator;
    private final AuditService audit;

    public RuleService(CareRuleRepository repository, RuleConditionValidator validator, AuditService audit) {
        this.repository = repository;
        this.validator = validator;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<RuleDto> list() {
        return repository.findAllByOrderByCodeAsc().stream().map(RuleDto::from).toList();
    }

    @Transactional(readOnly = true)
    public RuleDto get(Long id) {
        return RuleDto.from(find(id));
    }

    @Transactional
    public RuleDto create(RuleRequest request, Long actorId) {
        validator.validate(request.condition());
        if (repository.existsByCode(request.code())) {
            throw new InvalidStateException("A rule with code " + request.code() + " already exists");
        }
        CareRule rule = new CareRule(request.code());
        apply(rule, request);
        CareRule saved = repository.save(rule);
        audit.record(actorId, "RULE_CREATED", "RULE", saved.getId(), saved.getCode());
        return RuleDto.from(saved);
    }

    @Transactional
    public RuleDto update(Long id, RuleRequest request, Long actorId) {
        validator.validate(request.condition());
        if (repository.existsByCodeAndIdNot(request.code(), id)) {
            throw new InvalidStateException("A rule with code " + request.code() + " already exists");
        }
        CareRule rule = find(id);
        rule.setCode(request.code());
        apply(rule, request);
        audit.record(actorId, "RULE_UPDATED", "RULE", id, rule.getCode());
        return RuleDto.from(rule);
    }

    @Transactional
    public RuleDto setActive(Long id, boolean active, Long actorId) {
        CareRule rule = find(id);
        rule.setActive(active);
        audit.record(actorId, active ? "RULE_ENABLED" : "RULE_DISABLED", "RULE", id, rule.getCode());
        return RuleDto.from(rule);
    }

    private CareRule find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Rule", id));
    }

    private static void apply(CareRule rule, RuleRequest r) {
        rule.setName(r.name().trim());
        rule.setProgram(r.program());
        rule.setCondition(r.condition());
        rule.setAction(r.action());
        rule.setPriority(r.priority());
        rule.setDueWithinHours(r.dueWithinHours());
        rule.setExplanation(r.explanation().trim());
        rule.setActive(r.active());
    }
}
