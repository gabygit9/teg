package ar.edu.utn.frc.tup.piii.model.enums;

public enum StateGameEnum {
    PREPARATION,
    FIRST_ROUND,
    SECOND_ROUND,
    HOSTILITIES,
    FINISHED;

    public static StateGameEnum fromDescription(String description) {
        return StateGameEnum.valueOf(description.toUpperCase());
    }

}
