package searchengine.dto.appResponse;

import lombok.Value;

@Value
public class FalseResponse implements AppResponse{
    boolean result;
    String error;
}
