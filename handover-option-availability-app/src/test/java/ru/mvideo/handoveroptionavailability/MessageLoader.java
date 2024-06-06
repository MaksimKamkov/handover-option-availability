package ru.mvideo.handoveroptionavailability;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
@RequiredArgsConstructor
public class MessageLoader {

  private final ResourceLoader resourceLoader;
  private final ObjectMapper objectMapper;

  public <T> T loadObject(String location, Class<T> clazz) throws IOException {
    final var content = loadFileContent(location);
    return objectMapper.readValue(content, clazz);
  }

  public String loadFileContent(String location) throws IOException {
    final Resource resource = resourceLoader.getResource("classpath:" + location);
    try (InputStream inputStream = resource.getInputStream()) {
      return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
    }
  }
}
