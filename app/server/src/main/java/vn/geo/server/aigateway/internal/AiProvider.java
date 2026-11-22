package vn.geo.server.aigateway.internal;

import vn.geo.server.aigateway.api.AiRequest;

/**
 * Điểm gắn nhà cung cấp AI. Thêm nhà cung cấp thật bằng cách cài giao diện này và đặt
 * geo.ai.provider.
 */
public interface AiProvider {
  String name();

  String complete(AiRequest request) throws Exception;
}
