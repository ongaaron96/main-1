package csdev.couponstash.model.coupon;

import static csdev.couponstash.testutil.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ArchivedTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Archived(null));
    }

    @Test
    public void constructor_invalidArchived_throwsIllegalArgumentException() {
        String invalidArchived = "archived";
        assertThrows(IllegalArgumentException.class, () -> new Archived(invalidArchived));
    }

    @Test
    public void isValidState() {
        // null state
        assertThrows(NullPointerException.class, () -> Archived.isValidState(null));

        // invalid usage
        assertFalse(Archived.isValidState(" ")); // spaces only
        assertFalse(Archived.isValidState("")); // empty string
        assertFalse(Archived.isValidState("^")); // only non-alphanumeric characters
        assertFalse(Archived.isValidState("peter*")); // contains non-alphanumeric characters
        assertFalse(Archived.isValidState("12345")); // numbers only
        assertFalse(Archived.isValidState("-12345")); // negative numbers only

        // valid usage
        assertTrue(Archived.isValidState("true")); // true only
        assertTrue(Archived.isValidState("false")); // false only
    }
}
