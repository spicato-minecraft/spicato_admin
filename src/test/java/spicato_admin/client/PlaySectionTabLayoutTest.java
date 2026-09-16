package spicato_admin.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaySectionTabLayoutTest {

	@Test
	void knobViewportLeavesRoomForPinnedActions() {
		assertEquals(172, PlaySectionTab.knobViewportHeight(200));
		assertEquals(0, PlaySectionTab.knobViewportHeight(10));
		assertEquals(0, PlaySectionTab.knobViewportHeight(PlaySectionTab.ACTION_ROW_HEIGHT));
	}
}
