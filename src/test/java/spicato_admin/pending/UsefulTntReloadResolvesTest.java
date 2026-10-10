package spicato_admin.pending;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The reloader looks up {@code useful_tnt.UsefulTntConfig.reload} and invokes it.
 * A missing method must fail this test. The reloader logs and swallows that error,
 * so this test performs the same lookup without catching it.
 */
class UsefulTntReloadResolvesTest {

	@Test
	void usefulTntReloadResolves() throws Exception {
		String className = reloadClass("useful_tnt");
		assertEquals("useful_tnt.UsefulTntConfig", className);

		Class<?> configClass;
		try {
			configClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			fail("useful_tnt.UsefulTntConfig is not on the test classpath", e);
			return;
		}

		Method reload;
		try {
			reload = configClass.getMethod("reload");
		} catch (NoSuchMethodException e) {
			fail("ReflectiveConfigReloader cannot call " + className + ".reload because that method does not exist", e);
			return;
		}
		if (!Modifier.isStatic(reload.getModifiers()) || reload.getParameterCount() != 0) {
			fail(className + ".reload must be a public static no-arg method so the reloader can invoke it");
		}
		reload.invoke(null);
	}

	@SuppressWarnings("unchecked")
	private static String reloadClass(String modId) throws ReflectiveOperationException {
		Field field = ReflectiveConfigReloader.class.getDeclaredField("RELOAD_CLASSES");
		field.setAccessible(true);
		Map<String, String> classes = (Map<String, String>) field.get(null);
		String className = classes.get(modId);
		if (className == null) {
			fail("ReflectiveConfigReloader has no reload class for " + modId);
		}
		return className;
	}
}
