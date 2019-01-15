package com.sebastian_daschner.barista.boundary;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Health
@ApplicationScoped
public class HealthResource implements HealthCheck {
	
  public boolean isHealthy() {
      return true;
  }

  @Override
  public HealthCheckResponse call() {
    if (!isHealthy()) {
      return HealthCheckResponse.named(this.getClass().getSimpleName())
                                .down()
                                .build();
    }
    return HealthCheckResponse.named(this.getClass().getSimpleName())
                              .up().build();
  }

}
