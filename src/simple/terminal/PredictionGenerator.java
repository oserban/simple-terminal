package simple.terminal;

import java.util.List;

public interface PredictionGenerator {
    List<String> generate(String currentBuffer);
}
