package com.example.ssedemo.emitter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class SseEmitters {
  private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
  private final AtomicLong id = new AtomicLong(0);

  public long add(SseEmitter sseEmitter) {
    long emitterId = id.getAndAdd(1);
    this.emitters.put(emitterId, sseEmitter);
    log.info("New emitter added: id: {}, [{}]", emitterId, sseEmitter);
    log.info("Emitter list size: {}", emitters.size());

    removeWhenEmitterExpire(emitterId);
    completeWhenEmitterTimeout(sseEmitter);
    return emitterId;
  }

  private void removeWhenEmitterExpire(Long emitterId) {
    SseEmitter sseEmitter = emitters.get(emitterId);
    sseEmitter.onCompletion(() -> {
      log.info("SseEmitter onCompletion callback");
      this.emitters.remove(emitterId);
    });
  }

  private void completeWhenEmitterTimeout(SseEmitter sseEmitter) {
    sseEmitter.onTimeout(() -> {
      log.info("SseEmitter onTimeout callback");
      sseEmitter.complete();
    });
  }

  public SseEmitter get(Long id) {
    SseEmitter emitter = this.emitters.get(id);
    if (emitter == null) {
      throw new NoSuchElementException("Emitter is not found. id: " + id);
    }

    return emitter;
  }
}
