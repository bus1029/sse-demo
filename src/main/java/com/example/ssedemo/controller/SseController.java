package com.example.ssedemo.controller;

import com.example.ssedemo.emitter.SseEmitters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {
  private final SseEmitters sseEmitters;

  @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter connect() {
    SseEmitter emitter = new SseEmitter();
    sseEmitters.add(emitter);
    sendDummyDataWhenConnected(emitter);
    sendLoadingData(emitter);
    return emitter;
  }

  private void sendDummyDataWhenConnected(SseEmitter emitter) {
    sendDataTo(emitter, "sse", "connected");
  }

  private void sendLoadingData(SseEmitter emitter) {
    new Thread(() -> {
      for (int i = 0; i < 11; i++) {
        log.info("Send loading data. {}", i + "/10");
        sendDataTo(emitter, "sse", i + "/10");
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }).start();
  }

  private void sendDataTo(SseEmitter emitter, String name, String data) {
    try {
      SseEmitter.SseEventBuilder event = SseEmitter.event()
              .name(name)
              .data(data);
      emitter.send(event);
      log.info("Send data to emitter: {}", data);
    } catch (IOException e) {
      log.error("Fail to send dummy data.", e);
    }
  }
}
