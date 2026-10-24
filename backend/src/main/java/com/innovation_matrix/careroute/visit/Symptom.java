package com.innovation_matrix.careroute.visit;

/** Fixed screening checklist an ASHA can tick during a visit. Labels in English and Telugu. */
public enum Symptom {
    SEVERE_HEADACHE("Severe headache", "తీవ్రమైన తలనొప్పి"),
    BLURRED_VISION("Blurred vision", "మసకగా కనిపించడం"),
    BLEEDING("Bleeding", "రక్తస్రావం"),
    SWELLING("Swelling of face/hands", "ముఖం/చేతుల వాపు"),
    REDUCED_FETAL_MOVEMENT("Reduced baby movement", "శిశువు కదలికలు తగ్గడం"),
    FEVER("Fever", "జ్వరం"),
    COUGH_2_WEEKS("Cough > 2 weeks", "రెండు వారాలకు పైగా దగ్గు"),
    HEMOPTYSIS("Blood in cough", "దగ్గులో రక్తం"),
    BREATHLESSNESS("Breathlessness", "ఆయాసం"),
    WEIGHT_LOSS("Weight loss", "బరువు తగ్గడం"),
    DIARRHOEA("Diarrhoea", "విరేచనాలు"),
    RASH("Rash", "దద్దుర్లు");

    private final String label;
    private final String labelTe;

    Symptom(String label, String labelTe) {
        this.label = label;
        this.labelTe = labelTe;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelTe() {
        return labelTe;
    }
}
