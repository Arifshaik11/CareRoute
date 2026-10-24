package com.innovation_matrix.careroute.meta;

import java.util.Arrays;
import java.util.List;

import com.innovation_matrix.careroute.common.Priority;
import com.innovation_matrix.careroute.patient.Program;
import com.innovation_matrix.careroute.rules.ConditionField;
import com.innovation_matrix.careroute.visit.Symptom;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reference vocabularies so the UI never hard-codes codes that the backend owns. */
@RestController
@RequestMapping("/api/meta")
public class MetaController {

    @GetMapping
    public MetaDto get() {
        return new MetaDto(
                Arrays.asList(Program.values()),
                Arrays.asList(Priority.values()),
                Arrays.stream(Symptom.values())
                        .map(s -> new SymptomDto(s.name(), s.getLabel(), s.getLabelTe()))
                        .toList(),
                Arrays.stream(ConditionField.values())
                        .map(f -> new FieldDto(f.name(), f.getLabel(), f.getKind().name(),
                                f.getKind().getOperators().stream()
                                        .map(op -> new OperatorDto(op.name(), op.getSymbol()))
                                        .toList()))
                        .toList());
    }

    public record MetaDto(List<Program> programs, List<Priority> priorities, List<SymptomDto> symptoms,
                          List<FieldDto> conditionFields) {
    }

    public record SymptomDto(String code, String label, String labelTe) {
    }

    public record FieldDto(String code, String label, String kind, List<OperatorDto> operators) {
    }

    public record OperatorDto(String code, String symbol) {
    }
}
