package spicato_admin.knobs;

/**
 * One Play or Debug section. Title is the grouping header; {@link #modId()} must
 * match the gameplay JAR's fabric id and its {@code config/<modId>.json} file.
 */
public record AdminSection(String modId, String title) {
}
