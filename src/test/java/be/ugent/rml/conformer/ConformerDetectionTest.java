package be.ugent.rml.conformer;

import be.ugent.rml.TestCore;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ConformerDetectionTest extends TestCore {

    @Test
    public void badFile() {
        String path = "./conformer/validity_detection/bad_mapping.ttl";
        assertThrows(Exception.class, () -> this.createExecutor(path));
    }

    @Test
    public void goodFile() {
        String path = "./conformer/validity_detection/good_mapping.ttl";
        assertDoesNotThrow(() -> this.createExecutor(path));
    }
}
