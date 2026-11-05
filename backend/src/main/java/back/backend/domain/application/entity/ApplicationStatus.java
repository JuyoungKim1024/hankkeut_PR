package back.backend.domain.application.entity;

public enum ApplicationStatus {
    INTERESTED,
    PREPARING,
    APPLIED,
    INTERVIEWING,
    ACCEPTED,
    REJECTED,
    WITHDRAWN;

    public boolean canTransitionTo(ApplicationStatus next) {
        if (this == next) {
            return true;
        }
        return switch (this) {
            case INTERESTED -> next == PREPARING || next == WITHDRAWN;
            case PREPARING -> next == INTERESTED || next == APPLIED || next == WITHDRAWN;
            case APPLIED -> next == INTERVIEWING || next == REJECTED || next == WITHDRAWN;
            case INTERVIEWING -> next == ACCEPTED || next == REJECTED || next == WITHDRAWN;
            case ACCEPTED, REJECTED, WITHDRAWN -> false;
        };
    }
}
