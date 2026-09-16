package spicato_admin.pending;

@FunctionalInterface
public interface ConfigReloader {
	void reload(String modId);
}
