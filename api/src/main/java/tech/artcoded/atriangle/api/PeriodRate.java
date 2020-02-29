package tech.artcoded.atriangle.api;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

public enum PeriodRate {
  FIVE_SECONDS(5, TimeUnit.SECONDS),
  THIRTY_SECONDS(30, TimeUnit.SECONDS),
  ONE_MIN(1, TimeUnit.MINUTES),
  FIVE_MIN(5, TimeUnit.MINUTES),
  FIFTEEN_MIN(15, TimeUnit.MINUTES),
  THIRTHY_MIN(30, TimeUnit.MINUTES),
  SIXTY_MIN(60, TimeUnit.MINUTES),
  DISABLED(0, null);

  @Getter
  private final int rate;
  @Getter
  private final TimeUnit timeUnit;

  PeriodRate(int rate, TimeUnit timeUnit) {
    this.rate = rate;
    this.timeUnit = timeUnit;
  }

  @SneakyThrows
  public void sleep() {
    this.timeUnit.sleep(this.rate);
  }
}
