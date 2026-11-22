package vn.geo.server.sample;

import java.time.Instant;
import java.util.UUID;

public record SampleItem(UUID id, String title, Instant createdAt) {}
