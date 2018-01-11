package de.themoep.idconverter;

import java.util.Optional;

public class ReturnState {
    private ReturnType type;
    private Optional<String> message;
    
    public ReturnState() {
        this(ReturnType.UNKNOWN_ERROR);
    }
    
    public ReturnState(ReturnType type) {
        this.type = type;
        this.message = Optional.empty();
    }
    
    public ReturnState(ReturnType type, String message) {
        this.type = type;
        this.message = Optional.of(message);
    }
    
    public ReturnType getType() {
        return type;
    }
    
    public void setType(ReturnType type) {
        this.type = type;
    }
    
    public Optional<String> getMessage() {
        return message;
    }
    
    public void setMessage(Optional<String> message) {
        this.message = message;
    }
}
