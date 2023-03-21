package searchengine.dto.appResponse;

import lombok.Value;

@Value
public class TrueResponse implements AppResponse{
    boolean result;
}
