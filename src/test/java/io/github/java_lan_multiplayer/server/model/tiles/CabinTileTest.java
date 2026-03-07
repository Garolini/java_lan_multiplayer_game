package io.github.java_lan_multiplayer.server.model.tiles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.java_lan_multiplayer.server.model.tiles.CrewType.*;
import static org.junit.jupiter.api.Assertions.*;

class CabinTileTest {

    private CabinTile cabin;

    @BeforeEach
    void setup() {
        cabin = new CabinTile(1, new Connector[4]);
    }

    @Test
    void constructor_setsDefaultCrewTypeToDoubleHuman() {
        assertEquals(DOUBLE_HUMAN, cabin.getCrew());
    }

    @Test
    void setCrewType_validHumanTypes_success() {
        cabin.setCrewType(SINGLE_HUMAN);
        assertEquals(SINGLE_HUMAN, cabin.getCrew());

        cabin.setCrewType(NONE);
        assertEquals(NONE, cabin.getCrew());
    }

    @Test
    void setCrewType_brownAlienNotAllowed_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> cabin.setCrewType(BROWN_ALIEN));
    }

    @Test
    void setCrewType_purpleAlienNotAllowed_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> cabin.setCrewType(PURPLE_ALIEN));
    }

    @Test
    void allowAlienType_brownAlienAllowed_thenSetCrew_succeeds() {
        cabin.allowAlienType(BROWN_ALIEN, true);
        assertDoesNotThrow(() -> cabin.setCrewType(BROWN_ALIEN));
        assertEquals(BROWN_ALIEN, cabin.getCrew());
    }

    @Test
    void allowAlienType_purpleAlienAllowed_thenSetCrew_succeeds() {
        cabin.allowAlienType(PURPLE_ALIEN, true);
        assertDoesNotThrow(() -> cabin.setCrewType(PURPLE_ALIEN));
        assertEquals(PURPLE_ALIEN, cabin.getCrew());
    }

    @Test
    void allowAlienType_nonAlienType_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> cabin.allowAlienType(DOUBLE_HUMAN, true));
    }

    @Test
    void hasAlien_returnsCorrectly() {
        cabin.allowAlienType(BROWN_ALIEN, true);
        cabin.setCrewType(BROWN_ALIEN);
        assertTrue(cabin.hasAlien(BROWN_ALIEN));
        assertFalse(cabin.hasAlien(PURPLE_ALIEN));
    }

    @Test
    void hasAlien_nonAlienType_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> cabin.hasAlien(DOUBLE_HUMAN));
    }

    @Test
    void canHostAlienType_returnsCorrectly() {
        cabin.allowAlienType(BROWN_ALIEN, true);
        assertTrue(cabin.canHostAlienType(BROWN_ALIEN));
        assertFalse(cabin.canHostAlienType(PURPLE_ALIEN));
    }

    @Test
    void canHostAlienType_nonAlienType_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> cabin.canHostAlienType(SINGLE_HUMAN));
    }

    @Test
    void canHostAnAlienType_returnsCorrectly() {
        assertFalse(cabin.canHostAnAlienType());
        cabin.allowAlienType(PURPLE_ALIEN, true);
        assertTrue(cabin.canHostAnAlienType());
    }

    @Test
    void hasCrew_returnsCorrectly() {
        assertTrue(cabin.hasCrew());
        cabin.setCrewType(NONE);
        assertFalse(cabin.hasCrew());
    }

    @Test
    void getHumanCount_returnsCorrectHumanCount() {
        assertEquals(2, cabin.getHumanCount());
        cabin.setCrewType(SINGLE_HUMAN);
        assertEquals(1, cabin.getHumanCount());
        cabin.setCrewType(NONE);
        assertEquals(0, cabin.getHumanCount());
    }

    @Test
    void removeMember_fromDouble_becomesSingle() {
        cabin.setCrewType(DOUBLE_HUMAN);
        CrewType removed = cabin.removeMember();

        assertEquals(DOUBLE_HUMAN, removed);
        assertEquals(SINGLE_HUMAN, cabin.getCrew());
    }

    @Test
    void removeMember_fromSingle_becomesNone() {
        cabin.setCrewType(SINGLE_HUMAN);
        CrewType removed = cabin.removeMember();

        assertEquals(SINGLE_HUMAN, removed);
        assertEquals(NONE, cabin.getCrew());
    }

    @Test
    void removeMember_fromAlien_becomesNone() {
        cabin.allowAlienType(BROWN_ALIEN, true);
        cabin.setCrewType(BROWN_ALIEN);
        CrewType removed = cabin.removeMember();

        assertEquals(BROWN_ALIEN, removed);
        assertEquals(NONE, cabin.getCrew());
    }

    @Test
    void allowAlienType_revokesHosting_resetsCrewIfMatchingAlien() {
        cabin.allowAlienType(BROWN_ALIEN, true);
        cabin.setCrewType(BROWN_ALIEN);
        assertEquals(BROWN_ALIEN, cabin.getCrew());

        cabin.allowAlienType(BROWN_ALIEN, false);  // revoke
        assertEquals(NONE, cabin.getCrew());
    }

    @Test
    void getValidCrewTypesForCycle_returnsAvailableOptions() {
        List<CrewType> options = cabin.getValidCrewTypesForCycle();
        assertEquals(List.of(DOUBLE_HUMAN), options);

        cabin.allowAlienType(BROWN_ALIEN, true);
        cabin.allowAlienType(PURPLE_ALIEN, true);
        options = cabin.getValidCrewTypesForCycle();
        assertTrue(options.contains(DOUBLE_HUMAN));
        assertTrue(options.contains(BROWN_ALIEN));
        assertTrue(options.contains(PURPLE_ALIEN));
    }

    @Test
    void getTileType_returnsCabin() {
        assertEquals("Cabin", cabin.getTileType());
    }
}